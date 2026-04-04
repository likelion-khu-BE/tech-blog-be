import { Link } from 'react-router-dom'
import Avatar from '../components/Avatar'
import Chip from '../components/Chip'
import './AdminPage.css'

const PENDING_USERS = [
  { name: '김민준', generation: '13기', email: 'minjun@khu.ac.kr', date: '2026.03.30' },
  { name: '이서연', generation: '13기', email: 'seoyeon@khu.ac.kr', date: '2026.03.29' },
  { name: '박지훈', generation: '12기', email: 'jihun@khu.ac.kr', date: '2026.03.28' }
]

const MEMBERS = [
  { name: '장찬욱', email: 'chanwook@khu.ac.kr', generation: '13기', role: 'ADMIN', posts: 12, joinDate: '2026.03.03' },
  { name: '노희윤', email: 'heeyun@khu.ac.kr', generation: '13기', role: 'MEMBER', posts: 8, joinDate: '2026.03.03' },
  { name: '김주연', email: 'juyeon@khu.ac.kr', generation: '13기', role: 'MEMBER', posts: 6, joinDate: '2026.03.03' }
]

const RECENT_POSTS = [
  { title: 'Spring Boot EC2 GitHub Actions 자동 배포하기', author: '장찬욱', board: '백엔드 · CI/CD', status: 'published', date: '2026.03.28' },
  { title: 'LLM 파인튜닝 실험 기록 (초안)', author: '노희윤', board: 'AI · LLM', status: 'draft', date: '2026.03.25' }
]

function AdminPage() {
  return (
    <div className="admin-layout">
      {/* SIDEBAR */}
      <aside className="sidebar">
        <div className="sidebar-section">
          <div className="sidebar-label">회원 관리</div>
          <a className="sidebar-item active" href="#">
            가입 승인 대기 <span className="sidebar-count">3</span>
          </a>
          <a className="sidebar-item" href="#">전체 회원</a>
          <a className="sidebar-item" href="#">Role 관리</a>
        </div>
        <div className="sidebar-section">
          <div className="sidebar-label">게시글 관리</div>
          <a className="sidebar-item" href="#">전체 게시글</a>
          <a className="sidebar-item" href="#">Draft 글</a>
          <a className="sidebar-item" href="#">신고된 글</a>
        </div>
        <div className="sidebar-section">
          <div className="sidebar-label">게시판 관리</div>
          <a className="sidebar-item" href="#">🤖 AI 게시판</a>
          <a className="sidebar-item" href="#">⚙️ 백엔드 게시판</a>
          <a className="sidebar-item" href="#">🏆 해커톤 게시판</a>
          <a className="sidebar-item" href="#">태그 관리</a>
        </div>
      </aside>

      {/* MAIN */}
      <main className="admin-main">
        <div className="page-title">관리자 대시보드</div>

        {/* STATS */}
        <div className="stat-grid">
          <div className="stat-card">
            <div className="stat-card-label">전체 회원</div>
            <div className="stat-card-num">24</div>
            <div className="stat-card-sub">이번 달 +3명</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-label">승인 대기</div>
            <div className="stat-card-num orange">3</div>
            <div className="stat-card-sub">검토 필요</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-label">전체 게시글</div>
            <div className="stat-card-num green">47</div>
            <div className="stat-card-sub">Draft 5개 포함</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-label">이번 달 신규글</div>
            <div className="stat-card-num">12</div>
            <div className="stat-card-sub">전월 대비 +4</div>
          </div>
        </div>

        {/* PENDING APPROVAL */}
        <div className="section">
          <div className="section-header">
            <div className="section-title-sm">가입 승인 대기</div>
            <Chip variant="pending">3명 대기 중</Chip>
          </div>
          <table>
            <thead>
              <tr>
                <th>회원</th>
                <th>기수</th>
                <th>이메일</th>
                <th>신청일</th>
                <th>상태</th>
                <th>액션</th>
              </tr>
            </thead>
            <tbody>
              {PENDING_USERS.map(user => (
                <tr key={user.email}>
                  <td>
                    <div className="user-cell">
                      <Avatar name={user.name} size="sm" />
                      <div className="user-name">{user.name}</div>
                    </div>
                  </td>
                  <td className="mono-text">{user.generation}</td>
                  <td className="muted-text">{user.email}</td>
                  <td className="mono-text muted-text">{user.date}</td>
                  <td><Chip variant="pending">대기</Chip></td>
                  <td>
                    <div className="row-actions">
                      <button className="btn-sm approve">승인</button>
                      <button className="btn-sm reject">거절</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* ALL MEMBERS */}
        <div className="section">
          <div className="section-header">
            <div className="section-title-sm">전체 회원</div>
            <input className="search-input" type="text" placeholder="이름·이메일 검색" />
          </div>
          <table>
            <thead>
              <tr>
                <th>회원</th>
                <th>기수</th>
                <th>Role</th>
                <th>게시글</th>
                <th>가입일</th>
                <th>액션</th>
              </tr>
            </thead>
            <tbody>
              {MEMBERS.map(member => (
                <tr key={member.email}>
                  <td>
                    <div className="user-cell">
                      <Avatar name={member.name} size="sm" />
                      <div>
                        <div className="user-name">{member.name}</div>
                        <div className="user-email">{member.email}</div>
                      </div>
                    </div>
                  </td>
                  <td className="mono-text">{member.generation}</td>
                  <td><Chip variant={member.role === 'ADMIN' ? 'admin' : 'member'}>{member.role}</Chip></td>
                  <td className="mono-text">{member.posts}</td>
                  <td className="mono-text muted-text">{member.joinDate}</td>
                  <td>
                    <div className="row-actions">
                      {member.role !== 'ADMIN' && <button className="btn-sm promote">Admin 승격</button>}
                      <button className="btn-sm">프로필</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* RECENT POSTS */}
        <div className="section">
          <div className="section-header">
            <div className="section-title-sm">최근 게시글</div>
            <input className="search-input" type="text" placeholder="제목 검색" />
          </div>
          <table>
            <thead>
              <tr>
                <th>제목</th>
                <th>저자</th>
                <th>게시판</th>
                <th>상태</th>
                <th>작성일</th>
                <th>액션</th>
              </tr>
            </thead>
            <tbody>
              {RECENT_POSTS.map((post, idx) => (
                <tr key={idx}>
                  <td className="post-title-cell">{post.title}</td>
                  <td className="muted-text">{post.author}</td>
                  <td><Chip variant={post.board.includes('AI') ? 'draft' : 'member'}>{post.board}</Chip></td>
                  <td><Chip variant={post.status === 'published' ? 'published' : 'draft'}>{post.status === 'published' ? 'Published' : 'Draft'}</Chip></td>
                  <td className="mono-text muted-text">{post.date}</td>
                  <td>
                    <div className="row-actions">
                      <button className="btn-sm">보기</button>
                      <button className="btn-sm reject">삭제</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  )
}

export default AdminPage
