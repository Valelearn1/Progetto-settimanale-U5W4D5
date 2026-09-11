import { useCamera } from '../../hooks/useCamera'
import { Icon } from '../ui/Icon'
import { Alert } from '../ui/Alert'

/*
  Scatto dalla fotocamera integrata, riutilizzabile: lo usano sia il
  form del post (per le foto) sia il caricamento documenti (per
  fotografare un foglio e passarlo all'OCR).

  Lo scatto produce un File JPEG identico a uno scelto dal disco, quindi
  da qui in poi segue lo stesso percorso di validazione e invio. La
  validazione la fa chi usa il componente, perche' cambia a seconda del
  contesto (immagine di un post o documento da leggere).
*/
export function CameraCapture({
  onCapture,
  disabled,
  hint = 'Richiede una connessione sicura: funziona su localhost.',
  captureLabel = 'Scatta',
  // L'anteprima specchiata e' naturale per inquadrare se stessi, ma
  // rende illeggibile il testo di un documento. Lo scatto salvato non
  // e' mai specchiato: lo specchio riguarda solo l'anteprima.
  mirrored = true,
}) {
  const { videoRef, active, starting, error, start, stop, capture } = useCamera()

  async function handleShutter() {
    const file = await capture()
    if (!file) return
    onCapture(file)
    stop()
  }

  return (
    <div className="camera">
      <div className="camera__stage">
        <video
          ref={videoRef}
          className={[
            'camera__video',
            mirrored ? 'camera__video--mirrored' : '',
            active ? '' : 'camera__video--hidden',
          ]
            .filter(Boolean)
            .join(' ')}
          playsInline
          muted
        />

        {!active && (
          <div className="camera__placeholder">
            <Icon name="camera" size={34} />
            <p>{starting ? 'Apertura fotocamera…' : 'Accendi la fotocamera per scattare'}</p>
          </div>
        )}

        {active && (
          <button
            type="button"
            className="camera__shutter"
            onClick={handleShutter}
            disabled={disabled}
            aria-label={captureLabel}
          >
            <Icon name="camera" size={24} strokeWidth={2} />
          </button>
        )}
      </div>

      {error && <Alert variant="error">{error}</Alert>}

      {!active ? (
        <button
          type="button"
          className="btn btn--ghost btn--block"
          onClick={start}
          disabled={starting || disabled}
        >
          <Icon name="camera" size={18} />
          {starting ? 'Apertura…' : 'Accendi fotocamera'}
        </button>
      ) : (
        <button type="button" className="btn btn--ghost btn--block" onClick={stop}>
          <Icon name="close" size={18} />
          Spegni fotocamera
        </button>
      )}

      <p className="camera__hint">{hint}</p>
    </div>
  )
}
