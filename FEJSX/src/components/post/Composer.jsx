import { useState } from 'react'
import { createPost } from '../../api/posts'
import { validateImage } from '../../utils/fileValidation'
import { CameraCapture } from './CameraCapture'
import { PhotoUploader } from './PhotoUploader'
import { PhotoPreviewList } from './PhotoPreviewList'
import { DocumentPicker } from './DocumentPicker'
import { LocationPicker } from '../map/LocationPicker'
import { Icon } from '../ui/Icon'
import { Alert } from '../ui/Alert'
import './post.css'
import './composer.css'

const MAX_PHOTOS = 10
const MAX_DOCUMENTS = 5
const MAX_TEXT = 500

/*
  Il composer in cima alla bacheca, in stile social: si apre cliccando
  e tiene i tre strumenti (foto, documenti, posizione) sempre visibili
  nella barra, con un pallino che indica quanti elementi sono stati
  aggiunti a ciascuno.
*/
export function Composer({ onPublished, alwaysOpen = false }) {
  const [open, setOpen] = useState(alwaysOpen)
  // Quale pannello e' aperto: 'photo' | 'document' | 'location' | null
  const [tool, setTool] = useState(null)

  const [text, setText] = useState('')
  const [captureMode, setCaptureMode] = useState('UPLOAD')
  const [photos, setPhotos] = useState([])
  const [documents, setDocuments] = useState([])
  const [location, setLocation] = useState(null)

  const [rejected, setRejected] = useState([])
  const [error, setError] = useState(null)
  const [sending, setSending] = useState(false)

  const maxPhotos = captureMode === 'CAMERA' ? 1 : MAX_PHOTOS
  const textTooLong = text.length > MAX_TEXT
  const canPublish = text.trim().length > 0 && !textTooLong && !sending

  function toggleTool(next) {
    setOpen(true)
    setTool((current) => (current === next ? null : next))
  }

  function switchCaptureMode(mode) {
    setCaptureMode(mode)
    setPhotos([])
    setRejected([])
  }

  async function handleCameraCapture(file) {
    const check = await validateImage(file)
    if (!check.ok) {
      setRejected([{ name: file.name, error: check.error }])
      return
    }
    setRejected([])
    setPhotos([file])
  }

  function reset() {
    setText('')
    setPhotos([])
    setDocuments([])
    setLocation(null)
    setRejected([])
    setError(null)
    setTool(null)
    setOpen(alwaysOpen)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSending(true)
    try {
      await createPost({ text: text.trim(), captureMode, location, photos, documents })
      reset()
      onPublished?.()
    } catch (err) {
      setError(err)
    } finally {
      setSending(false)
    }
  }

  return (
    <form className={`composer ${open ? 'composer--open' : ''}`} onSubmit={handleSubmit}>
      <div className="composer__prompt">
        <span className="composer__avatar">DE</span>
        {open ? (
          <textarea
            className="composer__textarea"
            placeholder="Che cosa vuoi raccontare?"
            value={text}
            onChange={(event) => setText(event.target.value)}
            autoFocus={!alwaysOpen}
            required
          />
        ) : (
          <button type="button" className="composer__trigger" onClick={() => setOpen(true)}>
            Che cosa vuoi raccontare?
          </button>
        )}
      </div>

      {/* Riepilogo di cosa e' gia' stato allegato */}
      {(photos.length > 0 || documents.length > 0 || location) && (
        <div className="summary">
          {photos.length > 0 && (
            <span className="chip">
              <Icon name="camera" size={12} />
              {photos.length} {photos.length === 1 ? 'foto' : 'foto'}
            </span>
          )}
          {documents.length > 0 && (
            <span className="chip chip--secondary">
              <Icon name="doc" size={12} />
              {documents.length} {documents.length === 1 ? 'documento' : 'documenti'}
            </span>
          )}
          {location && (
            <span className="chip chip--secondary">
              <Icon name="pin" size={12} />
              {location.address?.split(',')[0] ?? 'posizione'}
            </span>
          )}
        </div>
      )}

      {/* Barra strumenti: sempre visibile, con i nomi delle funzioni */}
      <div className="toolbar">
        <button
          type="button"
          className={`tool ${tool === 'photo' ? 'tool--active' : ''}`}
          onClick={() => toggleTool('photo')}
        >
          <Icon name="camera" size={18} className="tool__icon" />
          <span>Foto</span>
          {photos.length > 0 && <span className="tool__count">{photos.length}</span>}
        </button>

        <button
          type="button"
          className={`tool ${tool === 'document' ? 'tool--active' : ''}`}
          onClick={() => toggleTool('document')}
        >
          <Icon name="doc" size={18} className="tool__icon" />
          <span>Documento</span>
          {documents.length > 0 && (
            <span className="tool__count tool__count--secondary">{documents.length}</span>
          )}
        </button>

        <button
          type="button"
          className={`tool ${tool === 'location' ? 'tool--active' : ''}`}
          onClick={() => toggleTool('location')}
        >
          <Icon name="pin" size={18} className="tool__icon" />
          <span>Luogo</span>
          {location && <span className="tool__count tool__count--secondary">1</span>}
        </button>
      </div>

      {tool === 'photo' && (
        <section className="panel">
          <h3 className="panel__head">
            <Icon name="camera" size={16} />
            Scatta o carica una foto
            <button
              type="button"
              className="panel__close"
              onClick={() => setTool(null)}
              aria-label="Chiudi"
            >
              <Icon name="close" size={14} />
            </button>
          </h3>

          <div className="segmented" role="tablist">
            <button
              type="button"
              role="tab"
              aria-selected={captureMode === 'CAMERA'}
              className={`segmented__option ${captureMode === 'CAMERA' ? 'segmented__option--active' : ''}`}
              onClick={() => switchCaptureMode('CAMERA')}
            >
              <Icon name="camera" size={16} />
              Scatta
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={captureMode === 'UPLOAD'}
              className={`segmented__option ${captureMode === 'UPLOAD' ? 'segmented__option--active' : ''}`}
              onClick={() => switchCaptureMode('UPLOAD')}
            >
              <Icon name="upload" size={16} />
              Carica
            </button>
          </div>

          <div style={{ marginTop: 'var(--space-4)' }}>
            {captureMode === 'CAMERA' ? (
              photos.length === 0 && (
                <CameraCapture
                  onCapture={handleCameraCapture}
                  disabled={sending}
                  hint="Con la fotocamera puoi allegare una sola foto. Funziona su localhost."
                />
              )
            ) : (
              <PhotoUploader
                remainingSlots={maxPhotos - photos.length}
                onAccepted={(files) => setPhotos((current) => [...current, ...files])}
                onRejected={setRejected}
              />
            )}
            <PhotoPreviewList
              photos={photos}
              onRemove={(index) => setPhotos((c) => c.filter((_, i) => i !== index))}
            />
          </div>
        </section>
      )}

      {tool === 'document' && (
        <section className="panel">
          <h3 className="panel__head">
            <Icon name="doc" size={16} />
            Documento con testo estratto
            <span className="panel__hint">l&apos;OCR legge il testo</span>
            <button
              type="button"
              className="panel__close"
              onClick={() => setTool(null)}
              aria-label="Chiudi"
            >
              <Icon name="close" size={14} />
            </button>
          </h3>
          <DocumentPicker
            documents={documents}
            onChange={setDocuments}
            onRejected={setRejected}
            maxDocuments={MAX_DOCUMENTS}
          />
        </section>
      )}

      {tool === 'location' && (
        <section className="panel">
          <h3 className="panel__head">
            <Icon name="pin" size={16} />
            Dove ti trovi
            <span className="panel__hint">cerca o tocca la mappa</span>
            <button
              type="button"
              className="panel__close"
              onClick={() => setTool(null)}
              aria-label="Chiudi"
            >
              <Icon name="close" size={14} />
            </button>
          </h3>
          <LocationPicker value={location} onChange={setLocation} />
        </section>
      )}

      {rejected.length > 0 && (
        <div className="panel">
          <Alert
            variant="error"
            title="File scartati prima dell'invio"
            details={rejected.map((r) => `${r.name}: ${r.error}`)}
          />
        </div>
      )}

      {error && (
        <div className="panel">
          <Alert variant="error" title={error.message} details={error.details ?? []} />
        </div>
      )}

      {open && (
        <div className="composer__footer">
          <span className={`composer__counter ${textTooLong ? 'composer__counter--over' : ''}`}>
            {text.length}/{MAX_TEXT}
          </span>
          {documents.length > 0 && (
            <span className="composer__counter">· l&apos;OCR richiede qualche secondo</span>
          )}
          <div className="composer__footer-actions">
            {!alwaysOpen && (
              <button type="button" className="btn btn--ghost" onClick={reset} disabled={sending}>
                Annulla
              </button>
            )}
            <button type="submit" className="btn btn--primary" disabled={!canPublish}>
              {sending ? <span className="spinner" /> : <Icon name="check" size={16} />}
              {sending ? 'Pubblico…' : 'Pubblica'}
            </button>
          </div>
        </div>
      )}
    </form>
  )
}
