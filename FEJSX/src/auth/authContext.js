import { createContext, useContext } from 'react'

/*
  Il contesto e l'hook stanno in un file separato dal componente
  AuthProvider: React Fast Refresh (il ricaricamento a caldo durante
  lo sviluppo) funziona solo se un file esporta soltanto componenti,
  quindi mescolare hook e componenti nello stesso file lo rompe.
*/
export const AuthContext = createContext(null)

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth va usato dentro <AuthProvider>')
  }
  return context
}
