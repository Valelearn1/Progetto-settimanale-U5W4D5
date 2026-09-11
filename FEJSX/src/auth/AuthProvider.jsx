import { useCallback, useEffect, useMemo, useState } from 'react'
import * as authApi from '../api/auth'
import { AuthContext } from './authContext'

/*
  Tiene l'utente collegato e lo rende disponibile a tutta
  l'applicazione, cosi' nessun componente deve occuparsi del token.

  Al primo caricamento, se c'e' un token salvato, chiede al backend
  chi sia: se il token e' scaduto la chiamata fallisce e il token
  viene buttato via.
*/
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  /*
    "loading" parte true solo se c'e' davvero un token da verificare.
    Calcolarlo qui invece che dentro l'effetto evita un aggiornamento
    di stato immediato al primo render, e soprattutto evita che chi e'
    collegato veda per un attimo l'interfaccia da ospite.
  */
  const [loading, setLoading] = useState(() => Boolean(authApi.getToken()))

  useEffect(() => {
    if (!authApi.getToken()) return

    let cancelled = false
    authApi
      .fetchMe()
      .then((me) => {
        if (!cancelled) setUser(me)
      })
      .catch(() => {
        // Token scaduto o non piu' valido: si riparte da ospiti.
        authApi.clearToken()
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const login = useCallback(async (credentials) => {
    const result = await authApi.login(credentials)
    authApi.saveToken(result.token)
    setUser(result.user)
    return result.user
  }, [])

  const register = useCallback(async (data) => {
    const result = await authApi.register(data)
    authApi.saveToken(result.token)
    setUser(result.user)
    return result.user
  }, [])

  const logout = useCallback(() => {
    authApi.clearToken()
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({ user, loading, login, register, logout }),
    [user, loading, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
