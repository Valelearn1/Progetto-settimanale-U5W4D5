import { useState } from 'react'
import { MapContainer, Marker, TileLayer, useMap, useMapEvents } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { reverseGeocode, searchAddress } from '../../api/geo'
import { Icon } from '../ui/Icon'
import { Alert } from '../ui/Alert'
import './map.css'

// Vista iniziale: l'Italia intera, finche' non si sceglie un punto.
const DEFAULT_CENTER = [42.5, 12.5]
const DEFAULT_ZOOM = 5

// Leaflet userebbe un'immagine caricata da un CDN: qui il marcatore e'
// un elemento HTML stilizzato col CSS del tema, cosi' non servono
// risorse esterne.
const pinIcon = L.divIcon({
  className: '',
  html: '<div class="pin-marker"></div>',
  iconSize: [32, 32],
  iconAnchor: [16, 32],
})

// Intercetta i click sulla mappa: e' il modo "scegli un punto".
function ClickHandler({ onPick }) {
  useMapEvents({
    click(event) {
      onPick(event.latlng.lat, event.latlng.lng)
    },
  })
  return null
}

// Sposta la mappa quando la posizione cambia da ricerca o da GPS.
function Recenter({ position }) {
  const map = useMap()
  if (position) map.flyTo(position, Math.max(map.getZoom(), 14), { duration: 0.8 })
  return null
}

export function LocationPicker({ value, onChange }) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  const position = value ? [Number(value.latitude), Number(value.longitude)] : null

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

  // Click sulla mappa: dalle coordinate si risale all'indirizzo.
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
        dentro il form di creazione del post, e l'HTML non ammette
        form annidati. Con un form interno, premere Invio o la lente
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

      <MapContainer
        center={position ?? DEFAULT_CENTER}
        zoom={position ? 14 : DEFAULT_ZOOM}
        className="picker__map"
        scrollWheelZoom
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <ClickHandler onPick={handleMapPick} />
        <Recenter position={position} />
        {position && <Marker position={position} icon={pinIcon} />}
      </MapContainer>

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
