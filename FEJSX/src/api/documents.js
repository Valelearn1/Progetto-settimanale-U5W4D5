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

export function deleteDocument(id) {
  return apiSend(`/api/documents/${id}`, { method: 'DELETE' })
}
