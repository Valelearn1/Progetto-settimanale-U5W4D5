import { apiGet, apiSend } from './client'

const TOKEN_KEY = 'bacheca-token'

// Il token sta in localStorage: sopravvive alla chiusura della scheda,
// cosi' non serve rifare l'accesso a ogni ricaricamento.
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function saveToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function register(data) {
  return apiSend('/api/auth/register', { json: data })
}

export function login(data) {
  return apiSend('/api/auth/login', { json: data })
}

// Ricostruisce la sessione al caricamento della pagina: se il token
// salvato e' ancora valido il backend restituisce l'utente.
export function fetchMe() {
  return apiGet('/api/auth/me')
}
