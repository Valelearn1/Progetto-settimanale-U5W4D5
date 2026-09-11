import { useState } from 'react'
import { createPost } from '../../api/posts'
import { CameraCapture } from './CameraCapture'
import { PhotoUploader } from './PhotoUploader'
import { PhotoPreviewList } from './PhotoPreviewList'
import { LocationPicker } from '../map/LocationPicker'
import { Icon } from '../ui/Icon'
import { Alert } from '../ui/Alert'
import './post.css'

const MAX_PHOTOS = 10
const MAX_TEXT = 500

export function PostForm({ onPublished }) {
  const [text, setText] = useState('')
  // CAMERA ammette una sola foto, UPLOAD fino a dieci: e' la stessa
  // regola che il backend rifa' in PostService.
  const [captureMode, setCaptureMode] = useState('UPLOAD')
  const [photos, setPhotos] = useState([])
  const [location, setLocation] = useState(null)
  const [rejected, setRejected] = useState([])
  const [error, setError] = useState(null)
  const [sending, setSending] = useState(false)

  const maxPhotos = captureMode === 'CAMERA' ? 1 : MAX_PHOTOS
  const remainingSlots = maxPhotos - photos.length
  const textTooLong = text.length > MAX_TEXT
  const canPublish = text.trim().length > 0 && !textTooLong && !sending

  function switchMode(mode) {
    setCaptureMode(mode)
    setPhotos([])
    setRejected([])
  }

  function removePhoto(index) {
    setPhotos((current) => current.filter((_, i) => i !== index))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSending(true)
    try {
      await createPost({ text: text.trim(), captureMode, location, photos })
      setText('')
      setPhotos([])
      setLocation(null)
      setRejected([])
      onPublished?.()
    } catch (err) {
      setError(err)
    } finally {
      setSending(false)
    }
  }

  return (
    <form className="card card--pad" onSubmit={handleSubmit}>
      <label className="field">
        <span className="field__label">Racconta il momento</span>
        <textarea
          className="textarea"
          placeholder="Il sole sta scendendo dietro gli scogli…"
          value={text}
          onChange={(event) => setText(event.target.value)}
          required
        />
        <span className={`composer__counter ${textTooLong ? 'composer__counter--over' : ''}`}>
          {text.length} / {MAX_TEXT}
        </span>
      </label>

      <section className="composer__section">
        <h3 className="composer__section-title">
          <Icon name="camera" size={18} />
          Foto
          <span className="chip">facoltative</span>
        </h3>

        <div className="segmented" role="tablist">
          <button
            type="button"
            role="tab"
            aria-selected={captureMode === 'CAMERA'}
            className={`segmented__option ${captureMode === 'CAMERA' ? 'segmented__option--active' : ''}`}
            onClick={() => switchMode('CAMERA')}
          >
            <Icon name="camera" size={16} />
            Scatta
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={captureMode === 'UPLOAD'}
            className={`segmented__option ${captureMode === 'UPLOAD' ? 'segmented__option--active' : ''}`}
            onClick={() => switchMode('UPLOAD')}
          >
            <Icon name="upload" size={16} />
            Carica
          </button>
        </div>

        <div style={{ marginTop: 'var(--space-4)' }}>
          {captureMode === 'CAMERA' ? (
            photos.length === 0 && (
              <CameraCapture onCapture={(file) => setPhotos([file])} disabled={sending} />
            )
          ) : (
            <PhotoUploader
              remainingSlots={remainingSlots}
              onAccepted={(files) => setPhotos((current) => [...current, ...files])}
              onRejected={setRejected}
            />
          )}

          <PhotoPreviewList photos={photos} onRemove={removePhoto} />

          {rejected.length > 0 && (
            <div style={{ marginTop: 'var(--space-3)' }}>
              <Alert
                variant="error"
                title="File scartati prima dell'invio"
                details={rejected.map((r) => `${r.name}: ${r.error}`)}
              />
            </div>
          )}
        </div>
      </section>

      <section className="composer__section">
        <h3 className="composer__section-title">
          <Icon name="pin" size={18} />
          Dove eri
          <span className="chip chip--sea">facoltativa</span>
        </h3>
        <LocationPicker value={location} onChange={setLocation} />
      </section>

      {error && (
        <div style={{ marginTop: 'var(--space-4)' }}>
          <Alert variant="error" title={error.message} details={error.details ?? []} />
        </div>
      )}

      <div className="composer__actions">
        <button type="submit" className="btn btn--primary btn--lg btn--block" disabled={!canPublish}>
          {sending ? <span className="spinner" /> : <Icon name="check" size={18} />}
          {sending ? 'Pubblicazione…' : 'Pubblica'}
        </button>
      </div>
    </form>
  )
}
