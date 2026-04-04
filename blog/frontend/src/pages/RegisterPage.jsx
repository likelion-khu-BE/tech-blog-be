import { useState } from 'react'
import { Link } from 'react-router-dom'
import './RegisterPage.css'

function RegisterPage() {
  const [step, setStep] = useState(2)
  const [showPending, setShowPending] = useState(false)
  const [formData, setFormData] = useState({
    name: '',
    generation: '13기',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    agreeAll: false,
    agreeTerms: false,
    agreePrivacy: false
  })

  const handleSubmit = () => {
    setShowPending(true)
  }

  if (showPending) {
    return (
      <div className="auth-wrap">
        <div className="pending-card">
          <div className="pending-icon">📬</div>
          <div className="pending-title">가입 신청 완료!</div>
          <div className="pending-badge">
            <div className="pending-dot"></div>
            관리자 승인 대기 중
          </div>
          <div className="pending-desc">
            가입 신청이 접수되었습니다.<br />
            관리자 확인 후 이메일로 승인 결과를 알려드립니다.<br />
            보통 <strong>1~2일 내</strong>에 처리됩니다.
          </div>
          <div className="pending-info">
            <div className="pending-info-row">
              <span>이름</span>
              <span>{formData.name || '홍길동'}</span>
            </div>
            <div className="pending-info-row">
              <span>기수</span>
              <span>{formData.generation}</span>
            </div>
            <div className="pending-info-row">
              <span>이메일</span>
              <span>{formData.email || 'example@khu.ac.kr'}</span>
            </div>
            <div className="pending-info-row">
              <span>신청일</span>
              <span>{new Date().toLocaleDateString('ko-KR')}</span>
            </div>
          </div>
          <Link to="/" className="btn-home">홈으로 돌아가기</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card register-card">
        <div className="auth-header">
          <div className="auth-logo">LL</div>
          <div className="auth-title">회원가입</div>
          <div className="auth-sub">멋사 경희대 백엔드 팀에 합류하세요</div>
        </div>

        {/* STEP INDICATOR */}
        <div className="steps">
          <div className={`step ${step > 1 ? 'done' : step === 1 ? 'active' : ''}`}>
            <div className="step-circle">{step > 1 ? '✓' : '1'}</div>
            <div className="step-label">기본 정보</div>
          </div>
          <div className="step-line"></div>
          <div className={`step ${step > 2 ? 'done' : step === 2 ? 'active' : ''}`}>
            <div className="step-circle">{step > 2 ? '✓' : '2'}</div>
            <div className="step-label">계정 설정</div>
          </div>
          <div className="step-line"></div>
          <div className={`step ${step === 3 ? 'active' : ''}`}>
            <div className="step-circle">3</div>
            <div className="step-label">약관 동의</div>
          </div>
        </div>

        {/* FORM */}
        <div className="form-row">
          <div>
            <label>이름</label>
            <input
              className="input-field"
              type="text"
              placeholder="홍길동"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            />
          </div>
          <div>
            <label>기수</label>
            <input
              className="input-field"
              type="text"
              placeholder="13기"
              value={formData.generation}
              onChange={(e) => setFormData({ ...formData, generation: e.target.value })}
            />
          </div>
        </div>

        <div className="form-group">
          <label>아이디</label>
          <div className="input-with-btn">
            <input
              className="input-field"
              type="text"
              placeholder="사용할 아이디 입력"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
            />
            <button className="btn-check">중복 확인</button>
          </div>
          <div className="input-ok">✓ 사용 가능한 아이디입니다.</div>
        </div>

        <div className="form-group">
          <label>이메일</label>
          <div className="input-with-btn">
            <input
              className="input-field"
              type="email"
              placeholder="example@khu.ac.kr"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            />
            <button className="btn-check">인증 발송</button>
          </div>
          <div className="input-hint">경희대학교 이메일을 사용하면 자동 인증됩니다.</div>
        </div>

        <div className="form-group">
          <label>비밀번호</label>
          <input
            className="input-field"
            type="password"
            placeholder="8자 이상, 영문+숫자 조합"
            value={formData.password}
            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
          />
          <div className="pw-strength">
            <div className="pw-bar"><div className="pw-fill"></div></div>
            <div className="pw-label">보통 강도</div>
          </div>
        </div>

        <div className="form-group">
          <label>비밀번호 확인</label>
          <input
            className="input-field"
            type="password"
            placeholder="비밀번호를 다시 입력하세요"
            value={formData.confirmPassword}
            onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
          />
        </div>

        <div className="agree-box">
          <div className="agree-item">
            <input
              type="checkbox"
              id="all"
              checked={formData.agreeAll}
              onChange={(e) => setFormData({
                ...formData,
                agreeAll: e.target.checked,
                agreeTerms: e.target.checked,
                agreePrivacy: e.target.checked
              })}
            />
            <label htmlFor="all" className="agree-all-label">전체 동의</label>
          </div>
          <hr className="agree-divider" />
          <div className="agree-item">
            <input
              type="checkbox"
              checked={formData.agreeTerms}
              onChange={(e) => setFormData({ ...formData, agreeTerms: e.target.checked })}
            />
            <span>[필수] <a href="#">이용약관</a>에 동의합니다.</span>
            <span className="agree-required">필수</span>
          </div>
          <div className="agree-item">
            <input
              type="checkbox"
              checked={formData.agreePrivacy}
              onChange={(e) => setFormData({ ...formData, agreePrivacy: e.target.checked })}
            />
            <span>[필수] <a href="#">개인정보 처리방침</a>에 동의합니다.</span>
            <span className="agree-required">필수</span>
          </div>
        </div>

        <button className="btn-submit" onClick={handleSubmit}>가입 완료</button>

        <div className="login-link">
          이미 계정이 있으신가요? <Link to="/login">로그인</Link>
        </div>
      </div>
    </div>
  )
}

export default RegisterPage
