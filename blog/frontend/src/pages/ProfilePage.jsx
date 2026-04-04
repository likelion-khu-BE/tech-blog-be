import { useState } from 'react'
import { Link } from 'react-router-dom'
import Avatar from '../components/Avatar'
import Chip from '../components/Chip'
import './ProfilePage.css'

const MOCK_POSTS = [
  {
    id: 1,
    title: 'Spring Boot 애플리케이션을 EC2에 GitHub Actions로 자동 배포하기',
    preview: 'Docker + GitHub Actions로 CI/CD 파이프라인을 구성한 과정. 직접 겪은 트러블슈팅을 함께 정리했습니다.',
    tags: ['백엔드', 'CI/CD'],
    date: '2026.03.28',
    likes: 14,
    bookmarks: 7,
    comments: 3,
    status: 'published'
  },
  {
    id: 2,
    title: 'k3s에서 Nginx로 리버스 프록시 직접 구성하기',
    preview: '기본 Traefik 인그레스 대신 Nginx를 직접 붙인 이유와 설정 방법을 정리합니다.',
    tags: ['백엔드', 'DevOps'],
    date: '2026.03.15',
    likes: 9,
    bookmarks: 4,
    comments: 1,
    status: 'published'
  },
  {
    id: 3,
    title: 'Kubernetes 보안 설정 정리 (초안)',
    preview: 'RBAC, NetworkPolicy, PodSecurityPolicy까지 실무에서 자주 쓰는 k8s 보안 설정을 정리하는 중입니다.',
    tags: ['Security'],
    date: '2026.03.31',
    status: 'draft'
  }
]

function ProfilePage() {
  const [activeTab, setActiveTab] = useState('전체 글 (12)')

  return (
    <div className="profile-layout">
      {/* LEFT: 프로필 카드 */}
      <aside>
        <div className="profile-card">
          <Avatar name="장찬욱" size="xl" />
          <div className="profile-name">장찬욱</div>
          <div className="profile-role">
            <Chip variant="admin">ADMIN</Chip>
            <Chip variant="orange">13기</Chip>
          </div>
          <div className="profile-bio">
            경희대 컴퓨터공학과 3학년. 백엔드 개발과 인프라에 관심이 많습니다.
            Spring, AWS, k8s를 공부하고 있어요.
          </div>

          <div className="profile-stats">
            <div className="profile-stat">
              <div className="profile-stat-num">12</div>
              <div className="profile-stat-label">게시글</div>
            </div>
            <div className="profile-stat">
              <div className="profile-stat-num">3<span>기</span></div>
              <div className="profile-stat-label">활동 기수</div>
            </div>
          </div>

          <div className="profile-links">
            <a href="https://github.com" target="_blank" rel="noreferrer" className="profile-link">
              <div className="profile-link-icon">🐙</div>
              <div className="profile-link-info">
                <div className="profile-link-label">GitHub</div>
                <div className="profile-link-url">github.com/chanwook</div>
              </div>
              <div className="github-connected">
                <div className="github-dot"></div>
                연결됨
              </div>
            </a>
            <a href="https://chanwook.kr" target="_blank" rel="noreferrer" className="profile-link">
              <div className="profile-link-icon">🌐</div>
              <div className="profile-link-info">
                <div className="profile-link-label">개인 블로그</div>
                <div className="profile-link-url">chanwook.kr</div>
              </div>
            </a>
            <a href="https://linkedin.com" target="_blank" rel="noreferrer" className="profile-link">
              <div className="profile-link-icon">💼</div>
              <div className="profile-link-info">
                <div className="profile-link-label">LinkedIn</div>
                <div className="profile-link-url">linkedin.com/in/chanwook</div>
              </div>
            </a>
          </div>
        </div>
      </aside>

      {/* RIGHT: 게시글 */}
      <div className="profile-right">
        <div className="tab-bar">
          {['전체 글 (12)', 'Published (10)', 'Draft (2)', '북마크'].map(tab => (
            <div
              key={tab}
              className={`tab ${activeTab === tab ? 'active' : ''}`}
              onClick={() => setActiveTab(tab)}
            >
              {tab}
            </div>
          ))}
        </div>

        <div className="post-list">
          {MOCK_POSTS.map(post => (
            <Link to={`/post/${post.id}`} key={post.id} className="post-item">
              <div className="post-item-top">
                <div className="post-item-title">{post.title}</div>
                <div className="post-item-chips">
                  {post.status === 'draft' ? (
                    <Chip variant="draft">Draft</Chip>
                  ) : (
                    <Chip variant="orange">{post.tags[0]}</Chip>
                  )}
                  {post.tags[1] && <Chip variant="neutral">{post.tags[1]}</Chip>}
                </div>
              </div>
              <div className="post-item-preview">{post.preview}</div>
              <div className="post-item-meta">
                <span>📅 {post.date}</span>
                {post.status === 'published' ? (
                  <>
                    <span>❤️ {post.likes}</span>
                    <span>🔖 {post.bookmarks}</span>
                    <span>💬 {post.comments}</span>
                  </>
                ) : (
                  <span style={{ color: '#3b82f6' }}>✏️ 작성 중</span>
                )}
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}

export default ProfilePage
