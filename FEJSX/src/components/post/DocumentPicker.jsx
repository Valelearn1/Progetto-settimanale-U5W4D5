import { useCallback, useState } from 'react'
import {
  DOCUMENT_ACCEPT,
  MAX_DOCUMENT_BYTES,
  formatBytes,
  validateDocument,
} from '../../utils/fileValidation'
import { useDropzone } from '../../hooks/useDropzone'
import { CameraCapture } from './CameraCapture'
import { Icon } from '../ui/Icon'

/*
  Raccoglie i documenti da allegare al post, scegliendoli dal disco
  oppure fotografandoli con la fotocamera del computer.

  A differenza di DocumentUpload (nel profilo) qui i file NON vengono
  caricati subito: restano in attesa e partono insieme al post, con
  un'unica richiesta. Anche l'OCR quindi avviene alla pubblicazione.
*/
export function DocumentPicker({ documents, onChange, onRejected, maxDocuments }) {
  const [source, setSource] = useState('FILE')
  const remainingSlots = maxDocuments - documents.length
  const full = remainingSlots <= 0

  const handleFiles = useCallback(
    async (chosen) => {
      if (chosen.length === 0) return

      const checked = await Promise.all(
        chosen.map(async (file) => ({ file, ...(await validateDocument(file)) })),
      )
      const accepted = checked.filter((c) => c.ok).map((c) => c.file)
      const rejected = checked
        .filter((c) => !c.ok)
        .map((c) => ({ name: c.file.name, error: c.error }))

      const overflow = accepted.slice(remainingSlots).map((file) => ({
        name: file.name,
        error: 'superato il numero massimo di documenti',
      }))

      onChange([...documents, ...accepted.slice(0, remainingSlots)])
      onRejected([...rejected, ...overflow])
    },
    [documents, onChange, onRejected, remainingSlots],
  )

  // Lo scatto produce un JPEG: passa dalla stessa validazione dei file
  // scelti dal disco, quindi il percorso resta uno solo.
  const handleCapture = useCallback((file) => handleFiles([file]), [handleFiles])

  const { dragging, dropzoneProps } = useDropzone({ onFiles: handleFiles, disabled: full })

  async function handleChange(event) {
    await handleFiles(Array.from(event.target.files ?? []))
    event.target.value = ''
  }

  function remove(index) {
    onChange(documents.filter((_, i) => i !== index))
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
        >
          <Icon name="camera" size={16} />
          Fotografa
        </button>
      </div>

      {source === 'CAMERA' ? (
        <CameraCapture
          onCapture={handleCapture}
          disabled={full}
          captureLabel="Fotografa il documento"
          mirrored={false}
          hint={
            full
              ? 'Hai raggiunto il numero massimo di documenti.'
              : "Inquadra il foglio dall'alto, con luce uniforme e senza ombre. Il testo stampato si legge bene, la scrittura a mano molto meno."
          }
        />
      ) : (
        <label
          className={`dropzone ${dragging ? 'dropzone--dragging' : ''} ${full ? 'dropzone--full' : ''}`}
          {...dropzoneProps}
        >
          <input
            type="file"
            accept={DOCUMENT_ACCEPT}
            multiple
            hidden
            onChange={handleChange}
            disabled={full}
          />
          <span className="dropzone__icon">
            <Icon name={dragging ? 'plus' : 'doc'} size={22} strokeWidth={2} />
          </span>
          <span className="dropzone__title">
            {full
              ? 'Hai raggiunto il massimo'
              : dragging
                ? 'Lascia qui i documenti'
                : 'Trascina i documenti oppure scegli dal dispositivo'}
          </span>
          <span className="dropzone__hint">
            PDF, JPEG, PNG o TIFF · max {formatBytes(MAX_DOCUMENT_BYTES)} l&apos;uno ·{' '}
            {remainingSlots} {remainingSlots === 1 ? 'posto libero' : 'posti liberi'}
          </span>
        </label>
      )}

      {documents.length > 0 && (
        <ul className="attachments">
          {documents.map((file, index) => (
            <li key={`${file.name}-${file.lastModified}`} className="attachment">
              <Icon name="doc" size={16} className="attachment__icon" />
              <span className="attachment__name">{file.name}</span>
              <span className="attachment__size">{formatBytes(file.size)}</span>
              <button
                type="button"
                className="attachment__remove"
                onClick={() => remove(index)}
                aria-label={`Rimuovi ${file.name}`}
              >
                <Icon name="close" size={14} strokeWidth={2.4} />
              </button>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
