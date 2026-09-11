import { useCallback, useState } from 'react'
import { uploadDocument } from '../../api/documents'
import {
  DOCUMENT_ACCEPT,
  MAX_DOCUMENT_BYTES,
  formatBytes,
  validateDocument,
} from '../../utils/fileValidation'
import { useDropzone } from '../../hooks/useDropzone'
import { CameraCapture } from '../post/CameraCapture'
import { Icon } from '../ui/Icon'
import { Alert } from '../ui/Alert'
import '../post/post.css'

export function DocumentUpload({ onUploaded }) {
  // 'FILE' = scegli o trascina un file gia' salvato
  // 'CAMERA' = fotografa un foglio con la fotocamera del computer
  const [source, setSource] = useState('FILE')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  const upload = useCallback(
    async (file) => {
      setError(null)

      const check = await validateDocument(file)
      if (!check.ok) {
        setError({ message: `${file.name}: ${check.error}` })
        return
      }

      setBusy(true)
      try {
        // L'OCR e' sincrono: questa attesa comprende anche
        // l'estrazione del testo.
        const created = await uploadDocument(file)
        onUploaded(created)
      } catch (err) {
        setError(err)
      } finally {
        setBusy(false)
      }
    },
    [onUploaded],
  )

  const handleFiles = useCallback(
    (files) => {
      // Un documento alla volta: l'OCR occuperebbe la richiesta troppo
      // a lungo con piu' file insieme.
      if (files[0]) upload(files[0])
    },
    [upload],
  )

  const { dragging, dropzoneProps } = useDropzone({ onFiles: handleFiles, disabled: busy })

  async function handleChange(event) {
    const files = Array.from(event.target.files ?? [])
    event.target.value = ''
    handleFiles(files)
  }

  return (
    <>
      <div className="segmented" role="tablist" style={{ marginBottom: 'var(--space-4)' }}>
        <button
          type="button"
          role="tab"
          aria-selected={source === 'FILE'}
          className={`segmented__option ${source === 'FILE' ? 'segmented__option--active' : ''}`}
          onClick={() => setSource('FILE')}
          disabled={busy}
        >
          <Icon name="upload" size={16} />
          Carica file
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={source === 'CAMERA'}
          className={`segmented__option ${source === 'CAMERA' ? 'segmented__option--active' : ''}`}
          onClick={() => setSource('CAMERA')}
          disabled={busy}
        >
          <Icon name="camera" size={16} />
          Fotografa
        </button>
      </div>

      {source === 'CAMERA' ? (
        <>
          <CameraCapture
            onCapture={upload}
            disabled={busy}
            captureLabel="Fotografa il documento"
            mirrored={false}
            hint="Inquadra il foglio dall'alto, con luce uniforme e senza ombre. Il testo stampato si legge bene, la scrittura a mano molto meno."
          />
          {busy && (
            <div style={{ marginTop: 'var(--space-3)' }}>
              <Alert variant="info">
                Lettura del testo in corso… l&apos;OCR può richiedere qualche secondo.
              </Alert>
            </div>
          )}
        </>
      ) : (
        <label
          className={`dropzone ${dragging ? 'dropzone--dragging' : ''}`}
          {...dropzoneProps}
        >
          <input
            type="file"
            accept={DOCUMENT_ACCEPT}
            hidden
            onChange={handleChange}
            disabled={busy}
          />
          <span className="dropzone__icon">
            {busy ? <span className="spinner" /> : <Icon name="doc" size={22} strokeWidth={2} />}
          </span>
          <span className="dropzone__title">
            {busy
              ? 'Lettura del testo in corso…'
              : dragging
                ? 'Lascia qui il documento'
                : 'Trascina un documento oppure scegli dal dispositivo'}
          </span>
          <span className="dropzone__hint">
            {busy
              ? "l'OCR può richiedere qualche secondo"
              : `PDF, JPEG, PNG o TIFF · max ${formatBytes(MAX_DOCUMENT_BYTES)}`}
          </span>
        </label>
      )}

      {error && (
        <div style={{ marginTop: 'var(--space-3)' }}>
          <Alert variant="error" title={error.message} details={error.details ?? []} />
        </div>
      )}
    </>
  )
}
