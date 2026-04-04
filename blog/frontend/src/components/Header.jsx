import { Link, useLocation } from 'react-router-dom'
import './Header.css'

function Header() {
  const location = useLocation()

  const isActive = (path) => {
    if (path === '/') return location.pathname === '/'
    return location.pathname.startsWith(path)
  }

  return (
    <header className="header">
      <Link to="/" className="logo">
        <span className="logo-badge">LIKELION</span>
        <span className="logo-text">경희대 백엔드</span>
        <div className="logo-divider"></div>
        <span className="logo-sub">Tech Blog</span>
      </Link>
      <nav>
        <Link to="/" className={isActive('/') && location.pathname === '/' ? 'active' : ''}>홈</Link>
        <Link to="/board" className={isActive('/board') ? 'active' : ''}>게시판</Link>
        <Link to="/members">멤버</Link>
        <Link to="/login">로그인</Link>
        <Link to="/write" className="btn-primary">+ 글쓰기</Link>
      </nav>
    </header>
  )
}

export default Header
