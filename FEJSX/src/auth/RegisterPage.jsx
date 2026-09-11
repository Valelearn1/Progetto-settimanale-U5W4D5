import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from './authContext'
import { Icon } from '../components/ui/Icon'
import { Alert } from '../components/ui/Alert'
import './auth.css'

const EMPTY = { nomeCompleto: '', username: '', email: '', password: '' }

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState(null)
  const [sending, setSending] = useState(false)

  function update(field) {
    return (event) => setForm((current) => ({ ...current, [field]: event.target.value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSending(true)
    try {
      await register(form)
      navigate('/')
    } catch (err) {
      setError(err)
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="auth">
      <form className="auth__card" onSubmit={handleSubmit}>
        <span className="auth__mark">
          <Icon name="user" size={22} strokeWidth={2} />
        </span>
        <h1 className="auth__title">Crea un account</h1>
        <p className="auth__subtitle">Bastano pochi secondi.</p>

        <label className="field">
          <span className="field__label">Nome completo</span>
          <input
            className="input"
            value={form.nomeCompleto}
            onChange={update('nomeCompleto')}
            autoComplete="name"
            required
          />
        </label>

        <label className="field">
          <span className="field__label">Username</span>
          <input
            className="input"
            value={form.username}
            onChange={update('username')}
            autoComplete="username"
            minLength={3}
            maxLength={30}
            required
          />
        </label>

        <label className="field">
          <span className="field__label">Email</span>
          <input
            className="input"
            type="email"
            value={form.email}
            onChange={update('email')}
            autoComplete="email"
            required
          />
        </label>

        <label className="field">
          <span className="field__label">Password</span>
          <input
            className="input"
            type="password"
            value={form.password}
            onChange={update('password')}
            autoComplete="new-password"
            minLength={8}
            required
          />
          <span className="field__hint">Almeno 8 caratteri.</span>
        </label>

        {error && <Alert variant="error" title={error.message} details={error.details ?? []} />}

        <button
          type="submit"
          className="btn btn--primary btn--lg btn--block"
          disabled={sending}
          style={{ marginTop: 'var(--space-4)' }}
        >
          {sending ? <span className="spinner" /> : <Icon name="check" size={18} />}
          {sending ? 'Creazione…' : 'Registrati'}
        </button>

        <p className="auth__switch">
          Hai già un account? <Link to="/accedi">Accedi</Link>
        </p>
      </form>
    </div>
  )
}
