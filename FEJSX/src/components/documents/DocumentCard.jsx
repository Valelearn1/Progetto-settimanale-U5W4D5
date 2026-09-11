import { apiUrl } from '../../api/client'
import { formatBytes } from '../../utils/fileValidation'
import { Icon } from '../ui/Icon'
import './documents.css'

const TYPE_LABELS = {
  'application/pdf': 'PDF',
  'image/jpeg': 'JPEG',
  'image/png': 'PNG',
  'image/tiff': 'TIFF',
}

export function DocumentCard({ document, onDelete }) {
  const hasText = document.extractedText?.trim().length > 0

  return (
    <article className="doc">
      <header className="doc__head">
        <span className="doc__icon">
          <Icon name="doc" size={20} />
        </span>
        <div>
          <a
            className="doc__name"
            href={apiUrl(document.url)}
            target="_blank"
            rel="noreferrer"
          >
            {TYPE_LABELS[document.contentType] ?? document.contentType}
          </a>
          <div className="doc__meta">
            {formatBytes(document.sizeBytes)} ·{' '}
            {new Date(document.createdAt).toLocaleDateString('it-IT', {
              day: 'numeric',
              month: 'long',
              hour: '2-digit',
              minute: '2-digit',
            })}
          </div>
          {document.postId && (
            <span className="chip chip--secondary doc__badge">
              <Icon name="board" size={12} />
              allegato a un post
            </span>
          )}
        </div>
        <button
          type="button"
          className="doc__delete"
          onClick={() => onDelete(document.id)}
          aria-label="Elimina documento"
        >
          <Icon name="trash" size={16} />
        </button>
      </header>

      <div className="doc__label">
        <Icon name="text" size={14} />
        Testo estratto
      </div>

      <pre className={`doc__text ${hasText ? '' : 'doc__text--empty'}`}>
        {hasText
          ? document.extractedText
          : 'Nessun testo riconosciuto in questo documento.'}
      </pre>
    </article>
  )
}
