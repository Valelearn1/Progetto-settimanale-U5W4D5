const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

// Il backend risponde agli errori con { message, details[] } (vedi
// ErrorResponse lato Java): qui li trasformiamo in un Error con un
// messaggio gia' leggibile, cosi' i componenti lo mostrano e basta.
class ApiError extends Error {
  constructor(message, status, details = []) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

async function handleResponse(response) {
  if (response.status === 204) return null

  const isJson = response.headers.get('content-type')?.includes('application/json')
  const payload = isJson ? await response.json() : null

  if (!response.ok) {
    throw new ApiError(
      payload?.message ?? `Errore ${response.status}`,
      response.status,
      payload?.details ?? [],
    )
  }
  return payload
}

export function apiUrl(path) {
  return `${BASE_URL}${path}`
}

export async function apiGet(path) {
  const response = await fetch(apiUrl(path))
  return handleResponse(response)
}

export async function apiSend(path, { method = 'POST', json, formData } = {}) {
  const response = await fetch(apiUrl(path), {
    method,
    // Con FormData NON si imposta Content-Type a mano: il browser deve
    // aggiungere da solo il boundary che separa le parti del multipart.
    headers: json ? { 'Content-Type': 'application/json' } : undefined,
    body: json ? JSON.stringify(json) : formData,
  })
  return handleResponse(response)
}

export { ApiError }
