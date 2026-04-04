import './Chip.css'

function Chip({ children, variant = 'neutral' }) {
  return (
    <span className={`chip chip-${variant}`}>
      {children}
    </span>
  )
}

export default Chip
