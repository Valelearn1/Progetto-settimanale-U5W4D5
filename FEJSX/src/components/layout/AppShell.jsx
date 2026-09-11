import { NavLink } from 'react-router-dom'
import { Icon } from '../ui/Icon'
import { useTheme } from '../../hooks/useTheme'
import './AppShell.css'

const LINKS = [
  { to: '/', label: 'Feed', icon: 'feed', end: true },
  { to: '/nuovo', label: 'Pubblica', icon: 'plus' },
  { to: '/profilo', label: 'Profilo', icon: 'user' },
]

function navClass({ isActive }) {
  return isActive ? 'nav__link nav__link--active' : 'nav__link'
}

export function AppShell({ children }) {
  const { theme, toggle } = useTheme()

  return (
    <div className="shell">
      <header className="header">
        <div className="brand">
          <span className="brand__mark">
            <Icon name="wave" size={20} strokeWidth={2} color="#fff" />
          </span>
          <span>
            <span className="brand__name">Golden Hour</span>
            <span className="brand__tag">momenti al tramonto</span>
          </span>
        </div>

        <nav className="nav">
          {LINKS.map((link) => (
            <NavLink key={link.to} to={link.to} end={link.end} className={navClass}>
              <Icon name={link.icon} size={18} />
              <span className="nav__label">{link.label}</span>
            </NavLink>
          ))}
        </nav>

        <button
          type="button"
          className="theme-toggle"
          onClick={toggle}
          aria-label={theme === 'dark' ? 'Passa al tema chiaro' : 'Passa al tema scuro'}
          title={theme === 'dark' ? 'Tema chiaro' : 'Tema scuro'}
        >
          <Icon name={theme === 'dark' ? 'sun' : 'moon'} size={18} />
        </button>
      </header>

      <main className="main">{children}</main>

      <footer className="footer">
        Golden Hour · progetto didattico · React + Spring Boot
      </footer>
    </div>
  )
}
