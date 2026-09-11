import { useEffect, useState } from 'react'
import { Icon } from '../ui/Icon'

/*
  Le anteprime usano URL.createObjectURL, che crea un riferimento
  temporaneo al file in memoria. Vanno rilasciati con revokeObjectURL
  quando non servono piu', altrimenti restano occupati finche' la
  pagina non viene ricaricata.
*/
export function PhotoPreviewList({ photos, onRemove }) {
  const [urls, setUrls] = useState([])

  useEffect(() => {
    const created = photos.map((photo) => URL.createObjectURL(photo))
    setUrls(created)
    return () => created.forEach((url) => URL.revokeObjectURL(url))
  }, [photos])

  if (photos.length === 0) return null

  return (
    <div className="previews">
      {photos.map((photo, index) => (
        <figure key={`${photo.name}-${photo.lastModified}`} className="preview">
          {urls[index] && (
            <img src={urls[index]} alt={photo.name} className="preview__img" />
          )}
          <button
            type="button"
            className="preview__remove"
            onClick={() => onRemove(index)}
            aria-label={`Rimuovi ${photo.name}`}
          >
            <Icon name="close" size={14} strokeWidth={2.4} />
          </button>
          {photos.length > 1 && <span className="preview__order">{index + 1}</span>}
        </figure>
      ))}
    </div>
  )
}
