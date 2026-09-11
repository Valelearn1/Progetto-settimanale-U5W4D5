import { apiGet } from './client'

// Entrambe passano dal nostro backend e non direttamente dal servizio
// di geocoding: cosi' un'eventuale chiave resta sul server e il limite
// di richieste si controlla in un punto solo.
export function searchAddress(query) {
  return apiGet(`/api/geo/search?q=${encodeURIComponent(query)}`)
}

export function reverseGeocode(lat, lng) {
  return apiGet(`/api/geo/reverse?lat=${lat}&lng=${lng}`)
}
