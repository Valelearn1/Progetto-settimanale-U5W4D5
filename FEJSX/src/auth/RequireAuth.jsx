import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './authContext'

/*
  Protegge le pagine che richiedono l'accesso. Se non si e' collegati
  rimanda alla pagina di accesso, ricordando dove si stava andando
  (state.from) cosi' dopo il login si torna li'.
*/
export function RequireAuth({ children }) {
  const { user, loading } = useAuth()
  const location = useLocation()

  // Finche' si sta verificando il token salvato non si decide nulla,
  // altrimenti chi e' collegato vedrebbe un lampo della pagina di
  // accesso a ogni ricaricamento.
  if (loading) {
    return (
      <div className="empty">
        <span className="spinner" />
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/accedi" replace state={{ from: location.pathname }} />
  }

  return children
}
