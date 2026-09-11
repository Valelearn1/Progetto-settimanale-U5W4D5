import { useCamera } from '../../hooks/useCamera'
import { validateImage } from '../../utils/fileValidation'
import { Icon } from '../ui/Icon'
import { Alert } from '../ui/Alert'

/*
  Scatto dalla fotocamera integrata.

  Lo scatto produce un File JPEG identico a uno scelto dal disco,
  quindi passa dalla stessa validazione delle foto caricate.
*/
export function CameraCapture({ onCapture, disabled }) {
  const { videoRef, active, starting, error, start, stop, capture } = useCamera()

  async function handleShutter() {
    const file = await capture()
    if (!file) return

    const result = await validateImage(file)
    if (!result.ok) return

    onCapture(file)
    stop()
  }

  return (
    <div className="camera">
      <div className="camera__stage">
        <video
          ref={videoRef}
          className={`camera__video ${active ? '' : 'camera__video--hidden'}`}
          playsInline
          muted
        />

        {!active && (
          <div className="camera__placeholder">
            <Icon name="camera" size={34} />
            <p>
              {starting
                ? 'Apertura fotocamera…'
                : 'Accendi la fotocamera per scattare una foto al tramonto'}
            </p>
          </div>
        )}

        {active && (
          <button
            type="button"
            className="camera__shutter"
            onClick={handleShutter}
            disabled={disabled}
            aria-label="Scatta"
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

      <p className="camera__hint">
        Con la fotocamera puoi allegare una sola foto. Richiede una connessione
        sicura: funziona su localhost.
      </p>
    </div>
  )
}
