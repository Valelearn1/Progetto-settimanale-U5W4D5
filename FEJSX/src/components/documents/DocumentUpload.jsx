import { useCallback, useState } from 'react'
import { uploadDocument } from '../../api/documents'
import {
  DOCUMENT_ACCEPT,
  MAX_DOCUMENT_BYTES,
  formatBytes,
  validateDocument,
} from '../../utils/fileValidation'
import { useDropzone } from '../../hooks/useDropzone'
import { Icon } from '../ui/Icon'
import { Alert } from '../ui/Alert'

export function DocumentUpload({ onUploaded }) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  const handleFiles = useCallback(
    async (files) => {
      // Un documento alla volta: l'OCR e' sincrono e occuperebbe la
      // richiesta troppo a lungo con piu' file insieme.
      const file = files[0]
      if (!file) return

      setError(null)

      const check = await validateDocument(file)
      if (!check.ok) {
        setError({ message: `${file.name}: ${check.error}` })
        return
      }

      setBusy(true)
      try {
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

  const { dragging, dropzoneProps } = useDropzone({ onFiles: handleFiles, disabled: busy })

  async function handleChange(event) {
    const files = Array.from(event.target.files ?? [])
    event.target.value = ''
    await handleFiles(files)
  }

  return (
    <>
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

      {error && (
        <div style={{ marginTop: 'var(--space-3)' }}>
          <Alert variant="error" title={error.message} details={error.details ?? []} />
        </div>
      )}
    </>
  )
}
