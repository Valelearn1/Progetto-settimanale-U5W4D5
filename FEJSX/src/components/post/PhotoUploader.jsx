import { useCallback } from 'react'
import {
  IMAGE_ACCEPT,
  MAX_IMAGE_BYTES,
  formatBytes,
  validateImages,
} from '../../utils/fileValidation'
import { useDropzone } from '../../hooks/useDropzone'
import { Icon } from '../ui/Icon'

export function PhotoUploader({ onAccepted, onRejected, remainingSlots }) {
  const full = remainingSlots <= 0

  // Stesso percorso sia per i file scelti col dialogo sia per quelli
  // trascinati: cambia solo da dove arriva la lista.
  const handleFiles = useCallback(
    async (chosen) => {
      if (chosen.length === 0) return

      const { accepted, rejected } = await validateImages(chosen)
      onAccepted(accepted.slice(0, remainingSlots))

      const overflow = accepted.slice(remainingSlots).map((file) => ({
        name: file.name,
        error: 'superato il numero massimo di foto',
      }))
      onRejected([...rejected, ...overflow])
    },
    [onAccepted, onRejected, remainingSlots],
  )

  const { dragging, dropzoneProps } = useDropzone({ onFiles: handleFiles, disabled: full })

  async function handleChange(event) {
    await handleFiles(Array.from(event.target.files ?? []))
    // Azzera l'input, altrimenti riselezionare lo stesso file non fa
    // scattare di nuovo l'evento change.
    event.target.value = ''
  }

  return (
    <label
      className={`dropzone ${dragging ? 'dropzone--dragging' : ''} ${full ? 'dropzone--full' : ''}`}
      {...dropzoneProps}
    >
      <input
        type="file"
        accept={IMAGE_ACCEPT}
        multiple
        hidden
        onChange={handleChange}
        disabled={full}
      />
      <span className="dropzone__icon">
        <Icon name={dragging ? 'plus' : 'upload'} size={22} strokeWidth={2} />
      </span>
      <span className="dropzone__title">
        {full
          ? 'Hai raggiunto il massimo'
          : dragging
            ? 'Lascia qui le foto'
            : 'Trascina le foto oppure scegli dal dispositivo'}
      </span>
      <span className="dropzone__hint">
        JPEG, PNG o WebP · max {formatBytes(MAX_IMAGE_BYTES)} l&apos;una ·{' '}
        {remainingSlots} {remainingSlots === 1 ? 'posto libero' : 'posti liberi'}
      </span>
    </label>
  )
}
