import { useState } from 'react'
import { Link } from 'react-router-dom'
import './LoginPage.css'

function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [rememberMe, setRememberMe] = useState(false)

  const handleSubmit = (e) => {
    e.preventDefault()
    // TODO: 로그인 로직
    alert('로그인 기능은 백엔드 연동 후 구현됩니다.')
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-logo">LL</div>
          <div className="auth-title">로그인</div>
          <div className="auth-sub">멋사 경희대 백엔드 기술 블로그</div>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>이메일</label>
            <input
              className="input-field"
              type="email"
              placeholder="이메일을 입력하세요"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>비밀번호</label>
            <input
              className="input-field"
              type="password"
              placeholder="비밀번호를 입력하세요"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <div className="form-options">
            <label className="checkbox-wrap">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
              />
              <span className="checkbox-label">로그인 상태 유지</span>
            </label>
            <a href="#" className="forgot-link">비밀번호 찾기</a>
          </div>

          <button type="submit" className="btn-submit">로그인</button>
        </form>

        <div className="divider">또는</div>

        <div className="register-link">
          계정이 없으신가요? <Link to="/register">회원가입</Link>
        </div>
      </div>
    </div>
  )
}

export default LoginPage
