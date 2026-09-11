import { useState } from 'react'
import { apiUrl } from '../../api/client'
import { updateDocumentText } from '../../api/documents'
import { formatBytes } from '../../utils/fileValidation'
import { Icon } from '../ui/Icon'
import { Alert } from '../ui/Alert'
import './documents.css'

const TYPE_LABELS = {
  'application/pdf': 'PDF',
  'image/jpeg': 'JPEG',
  'image/png': 'PNG',
  'image/tiff': 'TIFF',
}

export function DocumentCard({ document, onDelete, onUpdated }) {
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState(document.extractedText ?? '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  const hasText = document.extractedText?.trim().length > 0

  function startEditing() {
    // Si riparte sempre dal testo salvato: se si annulla e si riapre,
    // le modifiche scartate non devono ricomparire.
    setDraft(document.extractedText ?? '')
    setError(null)
    setEditing(true)
  }

  async function save() {
    setSaving(true)
    setError(null)
    try {
      const updated = await updateDocumentText(document.id, draft)
      onUpdated?.(updated)
      setEditing(false)
    } catch (err) {
      setError(err)
    } finally {
      setSaving(false)
    }
  }

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
        {!editing && (
          <button type="button" className="doc__edit" onClick={startEditing}>
            <Icon name="pencil" size={13} />
            {hasText ? 'Correggi' : 'Scrivi a mano'}
          </button>
        )}
      </div>

      {editing ? (
        <>
          <textarea
            className="textarea doc__textarea"
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            placeholder="Testo del documento…"
            autoFocus
          />
          {error && <Alert variant="error" title={error.message} details={error.details ?? []} />}
          <div className="doc__actions">
            <button
              type="button"
              className="btn btn--ghost"
              onClick={() => setEditing(false)}
              disabled={saving}
            >
              Annulla
            </button>
            <button type="button" className="btn btn--primary" onClick={save} disabled={saving}>
              {saving ? <span className="spinner" /> : <Icon name="check" size={16} />}
              {saving ? 'Salvataggio…' : 'Salva'}
            </button>
          </div>
        </>
      ) : (
        <pre className={`doc__text ${hasText ? '' : 'doc__text--empty'}`}>
          {hasText
            ? document.extractedText
            : 'Nessun testo riconosciuto in questo documento.'}
        </pre>
      )}
    </article>
  )
}
