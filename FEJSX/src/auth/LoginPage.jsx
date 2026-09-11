import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from './authContext'
import { Icon } from '../components/ui/Icon'
import { Alert } from '../components/ui/Alert'
import './auth.css'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', password: '' })
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
      await login(form)
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
          <Icon name="board" size={22} strokeWidth={2} />
        </span>
        <h1 className="auth__title">Bentornata</h1>
        <p className="auth__subtitle">Accedi per pubblicare sulla bacheca.</p>

        <label className="field">
          <span className="field__label">Username</span>
          <input
            className="input"
            value={form.username}
            onChange={update('username')}
            autoComplete="username"
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
            autoComplete="current-password"
            required
          />
        </label>

        {error && <Alert variant="error" title={error.message} details={error.details ?? []} />}

        <button
          type="submit"
          className="btn btn--primary btn--lg btn--block"
          disabled={sending}
          style={{ marginTop: 'var(--space-4)' }}
        >
          {sending ? <span className="spinner" /> : <Icon name="check" size={18} />}
          {sending ? 'Accesso…' : 'Accedi'}
        </button>

        <p className="auth__switch">
          Non hai un account? <Link to="/registrati">Registrati</Link>
        </p>

        <p className="auth__demo">
          Per provare l&apos;app puoi usare l&apos;utente di prova:
          <br />
          <code>demo</code> / <code>demo1234</code>
        </p>
      </form>
    </div>
  )
}
