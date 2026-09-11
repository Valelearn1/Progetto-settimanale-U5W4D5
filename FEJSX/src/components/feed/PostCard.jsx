import { apiUrl } from '../../api/client'
import { formatBytes } from '../../utils/fileValidation'
import { Icon } from '../ui/Icon'
import './feed.css'

const TYPE_LABELS = {
  'application/pdf': 'PDF',
  'image/jpeg': 'JPEG',
  'image/png': 'PNG',
  'image/tiff': 'TIFF',
}

function initials(name = '') {
  return name.slice(0, 2).toUpperCase()
}

function relativeTime(isoDate) {
  const seconds = Math.round((Date.now() - new Date(isoDate)) / 1000)
  if (seconds < 60) return 'adesso'
  const minutes = Math.round(seconds / 60)
  if (minutes < 60) return `${minutes} min fa`
  const hours = Math.round(minutes / 60)
  if (hours < 24) return `${hours} h fa`
  return new Date(isoDate).toLocaleDateString('it-IT', { day: 'numeric', month: 'short' })
}

function galleryClass(count) {
  if (count === 1) return 'post__gallery--1'
  if (count === 2) return 'post__gallery--2'
  return 'post__gallery--many'
}

export function PostCard({ post, onDelete }) {
  const { photos = [], documents = [], location } = post

  return (
    <article className="post">
      <header className="post__head">
        <span className="post__avatar">{initials(post.username)}</span>
        <div>
          <div className="post__author">@{post.username}</div>
          <div className="post__meta">
            <span>{relativeTime(post.createdAt)}</span>
            {post.captureMode === 'CAMERA' && (
              <>
                <span>·</span>
                <span title="Scattata con la fotocamera">
                  <Icon name="camera" size={12} /> scatto
                </span>
              </>
            )}
          </div>
        </div>
        <button
          type="button"
          className="post__delete"
          onClick={() => onDelete(post.id)}
          aria-label="Elimina post"
        >
          <Icon name="trash" size={16} />
        </button>
      </header>

      {post.text && <p className="post__text">{post.text}</p>}

      {photos.length > 0 && (
        <div className={`post__gallery ${galleryClass(photos.length)}`}>
          {photos.map((photo) => (
            <img
              key={photo.id}
              className="post__photo"
              src={apiUrl(photo.url)}
              alt=""
              loading="lazy"
            />
          ))}
        </div>
      )}

      {documents.length > 0 && (
        <ul className="post__docs">
          {documents.map((document) => (
            <li key={document.id} className="post__doc">
              <a
                className="post__doc-head"
                href={apiUrl(document.url)}
                target="_blank"
                rel="noreferrer"
              >
                <Icon name="doc" size={16} className="post__doc-icon" />
                <span className="post__doc-name">
                  {TYPE_LABELS[document.contentType] ?? document.contentType}
                </span>
                <span className="post__doc-size">{formatBytes(document.sizeBytes)}</span>
              </a>
              {document.extractedText?.trim() && (
                <details className="post__doc-text">
                  <summary>Testo estratto</summary>
                  <pre>{document.extractedText}</pre>
                </details>
              )}
            </li>
          ))}
        </ul>
      )}

      {location && (
        <p className="post__place">
          <Icon name="pin" size={15} className="post__place-icon" />
          {location.address ?? `${location.latitude}, ${location.longitude}`}
        </p>
      )}
    </article>
  )
}
