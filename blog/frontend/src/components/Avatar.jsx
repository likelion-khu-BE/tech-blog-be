import './Avatar.css'

function Avatar({ name, size = 'md' }) {
  const initial = name ? name.charAt(0) : '?'

  return (
    <div className={`avatar avatar-${size}`}>
      {initial}
    </div>
  )
}

export default Avatar
