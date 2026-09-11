import { useCallback, useRef, useState } from 'react'

/*
  Gestisce il trascinamento di file su un elemento.

  Due dettagli che sembrano strani ma servono:

  1. preventDefault() su dragOver e dragEnter e' obbligatorio. Senza,
     il browser applica il comportamento predefinito (aprire il file
     in una nuova scheda) e l'evento drop non arriva mai.

  2. dragEnter/dragLeave scattano anche passando sopra gli elementi
     figli, quindi un semplice true/false farebbe lampeggiare
     l'evidenziazione. Per questo si conta quante volte si entra e si
     esce, e si considera "fuori" solo quando il contatore torna a 0.
*/
export function useDropzone({ onFiles, disabled = false }) {
  const [dragging, setDragging] = useState(false)
  const depth = useRef(0)

  const handleDragEnter = useCallback(
    (event) => {
      event.preventDefault()
      if (disabled) return
      depth.current += 1
      setDragging(true)
    },
    [disabled],
  )

  const handleDragOver = useCallback(
    (event) => {
      event.preventDefault()
      if (!disabled) event.dataTransfer.dropEffect = 'copy'
    },
    [disabled],
  )

  const handleDragLeave = useCallback((event) => {
    event.preventDefault()
    depth.current -= 1
    if (depth.current <= 0) {
      depth.current = 0
      setDragging(false)
    }
  }, [])

  const handleDrop = useCallback(
    (event) => {
      event.preventDefault()
      depth.current = 0
      setDragging(false)
      if (disabled) return

      const files = Array.from(event.dataTransfer.files ?? [])
      if (files.length > 0) onFiles(files)
    },
    [disabled, onFiles],
  )

  return {
    dragging,
    dropzoneProps: {
      onDragEnter: handleDragEnter,
      onDragOver: handleDragOver,
      onDragLeave: handleDragLeave,
      onDrop: handleDrop,
    },
  }
}
