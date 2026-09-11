import { useCallback, useEffect, useState } from 'react'

const STORAGE_KEY = 'bacheca-theme'

function initialTheme() {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved === 'light' || saved === 'dark') return saved
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function useTheme() {
  const [theme, setTheme] = useState(initialTheme)

  // L'attributo data-theme sulla radice del documento e' quello che
  // il CSS legge per scegliere la palette (vedi theme.css).
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem(STORAGE_KEY, theme)
  }, [theme])

  const toggle = useCallback(
    () => setTheme((current) => (current === 'dark' ? 'light' : 'dark')),
    [],
  )

  return { theme, toggle }
}
