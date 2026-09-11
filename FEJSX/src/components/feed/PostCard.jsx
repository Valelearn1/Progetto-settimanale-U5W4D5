import { useState } from 'react'
import { apiUrl } from '../../api/client'
import { formatBytes } from '../../utils/fileValidation'
import { Icon } from '../ui/Icon'
import './feed.css'

const TYPE_LABELS = {
  'application/pdf': 'PDF',
  'image/jpeg': 'Immagine JPEG',
  'image/png': 'Immagine PNG',
  'image/tiff': 'Immagine TIFF',
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
  if (count === 1) return 'gallery--1'
  if (count === 2) return 'gallery--2'
  if (count === 3) return 'gallery--3'
  return 'gallery--many'
}

export function PostCard({ post, onDelete }) {
  const { photos = [], documents = [], location } = post
  const [confirming, setConfirming] = useState(false)

  return (
    <article className="post">
      <header className="post__head">
        <span className="post__avatar">{initials(post.username)}</span>
        <div className="post__who">
          <span className="post__author">@{post.username}</span>
          <span className="post__meta">
            {relativeTime(post.createdAt)}
            {post.captureMode === 'CAMERA' && (
              <>
                <span className="post__dot">·</span>
                <Icon name="camera" size={12} />
                scatto
              </>
            )}
          </span>
        </div>

        {confirming ? (
          <span className="post__confirm">
            <button type="button" className="btn btn--danger" onClick={() => onDelete(post.id)}>
              Elimina
            </button>
            <button type="button" className="btn btn--ghost" onClick={() => setConfirming(false)}>
              Annulla
            </button>
          </span>
        ) : (
          <button
            type="button"
            className="post__delete"
            onClick={() => setConfirming(true)}
            aria-label="Elimina post"
          >
            <Icon name="trash" size={16} />
          </button>
        )}
      </header>

      {post.text && <p className="post__text">{post.text}</p>}

      {photos.length > 0 && (
        <div className={`gallery ${galleryClass(photos.length)}`}>
          {photos.map((photo) => (
            <img
              key={photo.id}
              className="gallery__img"
              src={apiUrl(photo.url)}
              alt=""
              loading="lazy"
            />
          ))}
        </div>
      )}

      {(location || documents.length > 0) && (
        <div className="post__extras">
          {location && (
            <a
              className="post__place"
              href={`https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}`}
              target="_blank"
              rel="noreferrer"
            >
              <Icon name="pin" size={15} />
              {location.address ?? `${location.latitude}, ${location.longitude}`}
            </a>
          )}

          {documents.map((document) => (
            <div key={document.id} className="doc-chip">
              <a
                className="doc-chip__head"
                href={apiUrl(document.url)}
                target="_blank"
                rel="noreferrer"
              >
                <span className="doc-chip__icon">
                  <Icon name="doc" size={16} />
                </span>
                <span className="doc-chip__name">
                  {TYPE_LABELS[document.contentType] ?? document.contentType}
                </span>
                <span className="doc-chip__size">{formatBytes(document.sizeBytes)}</span>
              </a>
              {document.extractedText?.trim() && (
                <details className="doc-chip__text">
                  <summary>
                    <Icon name="text" size={13} />
                    Testo estratto dall&apos;OCR
                  </summary>
                  <pre>{document.extractedText}</pre>
                </details>
              )}
            </div>
          ))}
        </div>
      )}
    </article>
  )
}
