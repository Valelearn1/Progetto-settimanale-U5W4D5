import { apiGet, apiSend } from './client'

export function fetchDocuments() {
  return apiGet('/api/documents')
}

export function uploadDocument(file) {
  const formData = new FormData()
  formData.append('file', file)
  // L'OCR e' sincrono lato backend: questa promise si risolve solo
  // quando il testo e' gia' stato estratto.
  return apiSend('/api/documents', { formData })
}

// L'OCR sbaglia spesso su scansioni storte, caratteri decorativi o
// sfondi colorati: questo permette di correggere il testo a mano.
export function updateDocumentText(id, extractedText) {
  return apiSend(`/api/documents/${id}/text`, {
    method: 'PATCH',
    json: { extractedText },
  })
}

export function deleteDocument(id) {
  return apiSend(`/api/documents/${id}`, { method: 'DELETE' })
}
