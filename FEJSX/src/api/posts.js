import { apiGet, apiSend } from './client'

export function fetchPosts({ page = 0, size = 10 } = {}) {
  return apiGet(`/api/posts?page=${page}&size=${size}`)
}

export function fetchPost(id) {
  return apiGet(`/api/posts/${id}`)
}

export function createPost({ text, captureMode, location, photos, documents = [] }) {
  const formData = new FormData()

  // La parte "post" e' JSON dentro una richiesta multipart: va
  // dichiarata come Blob con type application/json, altrimenti Spring
  // la riceve come testo semplice e non riesce a deserializzarla.
  const payload = { text }
  if (photos.length > 0) payload.captureMode = captureMode
  if (location) payload.location = location

  formData.append(
    'post',
    new Blob([JSON.stringify(payload)], { type: 'application/json' }),
  )
  photos.forEach((photo) => formData.append('photos', photo))
  // Ogni documento passa dall'OCR lato server: con allegati pesanti
  // questa richiesta puo' durare qualche secondo.
  documents.forEach((document) => formData.append('documents', document))

  return apiSend('/api/posts', { formData })
}

export function deletePost(id) {
  return apiSend(`/api/posts/${id}`, { method: 'DELETE' })
}
