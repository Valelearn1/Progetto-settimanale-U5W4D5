import { Icon } from './Icon'

export function Alert({ variant = 'error', title, details = [], children }) {
  const icon = variant === 'success' ? 'check' : 'alert'

  return (
    <div className={`alert alert--${variant}`} role={variant === 'error' ? 'alert' : 'status'}>
      <Icon name={icon} size={18} className="alert__icon" />
      <div>
        {title && <strong>{title}</strong>}
        {children}
        {details.length > 0 && (
          <ul className="alert__list">
            {details.map((detail) => (
              <li key={detail}>{detail}</li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
