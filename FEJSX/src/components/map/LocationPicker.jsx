import { useState } from 'react'
import { APIProvider, AdvancedMarker, Map, useMap } from '@vis.gl/react-google-maps'
import { reverseGeocode, searchAddress } from '../../api/geo'
import { Icon } from '../ui/Icon'
import { Alert } from '../ui/Alert'
import './map.css'

// Vista iniziale: l'Italia intera, finche' non si sceglie un punto.
const DEFAULT_CENTER = { lat: 42.5, lng: 12.5 }
const DEFAULT_ZOOM = 5
const PICKED_ZOOM = 15

const API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY
// Serve a Google per gli "Advanced Marker" (i marcatori personalizzati).
// "DEMO_MAP_ID" e' l'identificativo di prova messo a disposizione da Google.
const MAP_ID = import.meta.env.VITE_GOOGLE_MAPS_MAP_ID ?? 'DEMO_MAP_ID'

// Sposta la mappa quando la posizione cambia da ricerca o da GPS.
// Deve stare dentro <Map> per poter accedere all'istanza.
function Recenter({ position }) {
  const map = useMap()
  const [last, setLast] = useState(null)

  const key = position ? `${position.lat},${position.lng}` : null
  if (map && key && key !== last) {
    setLast(key)
    map.panTo(position)
    if ((map.getZoom() ?? 0) < PICKED_ZOOM) map.setZoom(PICKED_ZOOM)
  }
  return null
}

function PickerMap({ position, onPick }) {
  return (
    <Map
      className="picker__map"
      mapId={MAP_ID}
      defaultCenter={position ?? DEFAULT_CENTER}
      defaultZoom={position ? PICKED_ZOOM : DEFAULT_ZOOM}
      gestureHandling="greedy"
      disableDefaultUI={false}
      streetViewControl={false}
      mapTypeControl={false}
      fullscreenControl={false}
      // Click sulla mappa: e' il modo "scegli un punto".
      onClick={(event) => {
        const latLng = event.detail?.latLng
        if (latLng) onPick(latLng.lat, latLng.lng)
      }}
    >
      <Recenter position={position} />
      {position && (
        <AdvancedMarker position={position} title="Posizione scelta">
          <div className="pin-marker" />
        </AdvancedMarker>
      )}
    </Map>
  )
}

export function LocationPicker({ value, onChange }) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  const position = value
    ? { lat: Number(value.latitude), lng: Number(value.longitude) }
    : null

  /*
    La ricerca e il geocoding inverso passano dal NOSTRO backend, non
    dalle API di Google: la Geocoding API di Google richiede un account
    di fatturazione attivo, mentre la mappa qui sopra no. Il backend usa
    Nominatim (OpenStreetMap), che e' gratuito.
  */
  async function handleSearch() {
    if (!query.trim()) return

    setBusy(true)
    setError(null)
    setResults([])
    try {
      const found = await searchAddress(query)
      setResults(found)
      if (found.length === 0) setError('Nessun indirizzo trovato per questa ricerca.')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function handleMapPick(lat, lng) {
    setBusy(true)
    setError(null)
    setResults([])
    try {
      const place = await reverseGeocode(lat.toFixed(6), lng.toFixed(6))
      onChange(place)
    } catch (err) {
      // Un punto in mezzo al mare non ha un indirizzo: teniamo comunque
      // le coordinate, che sono il dato che conta.
      onChange({ latitude: lat.toFixed(6), longitude: lng.toFixed(6), address: null })
      if (err.status !== 404) setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  function handleGeolocate() {
    if (!navigator.geolocation) {
      setError('Questo browser non espone la posizione del dispositivo.')
      return
    }
    setBusy(true)
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => handleMapPick(coords.latitude, coords.longitude),
      () => {
        setBusy(false)
        setError('Permesso di geolocalizzazione negato.')
      },
    )
  }

  return (
    <div>
      {/*
        Volutamente un <div> e non un <form>: questo componente vive
        dentro il form di creazione del post, e l'HTML non ammette form
        annidati. Con un form interno, premere Invio o la lente
        invierebbe il post invece di cercare l'indirizzo.
      */}
      <div className="picker__search">
        <input
          type="search"
          className="input"
          placeholder="Cerca un indirizzo o un luogo…"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              event.preventDefault()
              handleSearch()
            }
          }}
        />
        <button
          type="button"
          className="btn btn--subtle"
          onClick={handleSearch}
          disabled={busy || !query.trim()}
          title="Cerca"
        >
          {busy ? <span className="spinner" /> : <Icon name="search" size={18} />}
        </button>
        <button
          type="button"
          className="btn btn--ghost"
          onClick={handleGeolocate}
          disabled={busy}
          title="Usa la mia posizione"
        >
          <Icon name="crosshair" size={18} />
        </button>
      </div>

      {results.length > 0 && (
        <ul className="picker__results">
          {results.map((place) => (
            <li key={`${place.latitude}-${place.longitude}`}>
              <button
                type="button"
                className="picker__result"
                onClick={() => {
                  onChange(place)
                  setResults([])
                  setQuery('')
                }}
              >
                {place.address}
              </button>
            </li>
          ))}
        </ul>
      )}

      {error && <Alert variant="error">{error}</Alert>}

      {API_KEY ? (
        <APIProvider apiKey={API_KEY}>
          <PickerMap position={position} onPick={handleMapPick} />
        </APIProvider>
      ) : (
        <Alert variant="error" title="Mappa non disponibile">
          Manca <code>VITE_GOOGLE_MAPS_API_KEY</code> in <code>.env.local</code>.
          Puoi comunque cercare un indirizzo o usare la tua posizione.
        </Alert>
      )}

      {value ? (
        <div className="picker__selected">
          <Icon name="pin" size={18} className="picker__selected-icon" />
          <span>
            {value.address ?? 'Punto senza indirizzo noto'}
            <span className="picker__coords">
              {Number(value.latitude).toFixed(6)}, {Number(value.longitude).toFixed(6)}
            </span>
          </span>
          <button
            type="button"
            className="picker__clear"
            onClick={() => onChange(null)}
            aria-label="Rimuovi posizione"
          >
            <Icon name="close" size={16} />
          </button>
        </div>
      ) : (
        <p className="field__hint">
          Cerca un indirizzo, tocca un punto sulla mappa, oppure usa la tua posizione.
        </p>
      )}
    </div>
  )
}
