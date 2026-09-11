import { useEffect, useState } from 'react'
import { deleteDocument, fetchDocuments } from '../api/documents'
import { DocumentUpload } from '../components/documents/DocumentUpload'
import { DocumentCard } from '../components/documents/DocumentCard'
import { Icon } from '../components/ui/Icon'
import { Alert } from '../components/ui/Alert'
import '../components/documents/documents.css'

export function ProfilePage() {
  const [documents, setDocuments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetchDocuments()
      .then(setDocuments)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  async function handleDelete(id) {
    const previous = documents
    setDocuments((current) => current.filter((doc) => doc.id !== id))
    try {
      await deleteDocument(id)
    } catch (err) {
      setDocuments(previous)
      setError(err.message)
    }
  }

  return (
    <>
      <section className="profile__hero">
        <span className="profile__avatar">UD</span>
        <div>
          <div className="profile__name">Utente Demo</div>
          <div className="profile__handle">@demo</div>
        </div>
      </section>

      <h2 className="page-title">Documenti</h2>
      <p className="page-subtitle">
        Carica un PDF o una scansione: il testo viene estratto automaticamente e
        reso ricercabile.
      </p>

      <DocumentUpload
        onUploaded={(created) => setDocuments((current) => [created, ...current])}
      />

      {error && (
        <div style={{ marginTop: 'var(--space-4)' }}>
          <Alert variant="error">{error}</Alert>
        </div>
      )}

      <div className="stack" style={{ marginTop: 'var(--space-5)' }}>
        {loading ? (
          <div className="empty">
            <span className="spinner" />
          </div>
        ) : documents.length === 0 ? (
          <div className="empty">
            <span className="empty__icon">
              <Icon name="doc" size={24} />
            </span>
            <p>Nessun documento caricato.</p>
          </div>
        ) : (
          documents.map((doc) => (
            <DocumentCard key={doc.id} document={doc} onDelete={handleDelete} />
          ))
        )}
      </div>
    </>
  )
}
