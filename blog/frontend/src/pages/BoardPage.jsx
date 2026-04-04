import { useState } from 'react'
import { Link } from 'react-router-dom'
import PostCard from '../components/PostCard'
import './BoardPage.css'

const MOCK_POSTS = [
  {
    id: 1,
    title: 'Spring Boot 애플리케이션을 EC2에\nGitHub Actions로 자동 배포하기',
    preview: 'Docker + GitHub Actions로 CI/CD 파이프라인을 구성한 과정. 직접 겪은 트러블슈팅을 함께 정리했습니다.',
    tags: ['Spring Boot', 'AWS EC2'],
    authorName: '장찬욱',
    date: '2026.03.28',
    featured: true
  },
  {
    id: 2,
    title: 'LIS로 풀어본 전선 교차 문제',
    preview: 'BOJ 2565번을 De Morgan → LIS 접근으로 해결한 풀이 과정을 정리합니다.',
    tags: ['알고리즘'],
    authorName: '노희윤',
    date: '2026.03.25'
  },
  {
    id: 3,
    title: 'PostgreSQL 스트리밍 복제 직접 구성해보기',
    preview: 'Primary-Replica 구조를 Raspberry Pi 클러스터에 세팅한 경험 공유.',
    tags: ['데이터베이스'],
    authorName: '김주연',
    date: '2026.03.20'
  },
  {
    id: 4,
    title: 'k3s에서 Nginx로 리버스 프록시 직접 구성하기',
    preview: '기본 Traefik 대신 Nginx를 붙인 이유와 설정 방법 정리.',
    tags: ['Docker'],
    authorName: '장찬욱',
    date: '2026.03.15'
  },
  {
    id: 5,
    title: '1학기 백엔드 스터디 회고',
    preview: 'Spring 스터디를 마치며 느낀 점과 다음 시즌 계획을 공유합니다.',
    tags: ['회고'],
    authorName: '노희윤',
    date: '2026.03.10'
  }
]

const BOARD_TABS = ['전체', '🤖 AI', '⚙️ 백엔드', '🏆 해커톤']
const CATEGORY_CHIPS = ['전체', 'LLM', 'MLOps', '모델 서빙', 'CI/CD', 'DevOps', 'Security']

function BoardPage() {
  const [activeTab, setActiveTab] = useState('전체')
  const [activeCategory, setActiveCategory] = useState('전체')
  const [searchQuery, setSearchQuery] = useState('')

  return (
    <div className="board-page">
      {/* BOARD TABS */}
      <div className="board-tabs">
        {BOARD_TABS.map(tab => (
          <button
            key={tab}
            className={`board-tab ${activeTab === tab ? 'active' : ''}`}
            onClick={() => setActiveTab(tab)}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* FILTER ROW */}
      <div className="filter-bar">
        <div className="filter-group">
          <span className="filter-label">기수</span>
          <span className="tag active">전체</span>
          <span className="tag">13기</span>
          <span className="tag">12기</span>
          <span className="tag">11기</span>
        </div>
        <div className="filter-divider"></div>
        <div className="filter-group">
          <span className="filter-label">저자</span>
          <span className="tag active">전체</span>
          <span className="tag">장찬욱</span>
          <span className="tag">노희윤</span>
          <span className="tag">김주연</span>
          <span className="tag">김우진</span>
        </div>
        <div className="search-wrap">
          <input
            className="search-input"
            type="text"
            placeholder="제목, 태그 검색"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          <button className="btn-primary">검색</button>
        </div>
      </div>

      {/* CATEGORY CHIPS */}
      <div className="category-bar">
        {CATEGORY_CHIPS.map(chip => (
          <span
            key={chip}
            className={`cat-chip ${activeCategory === chip ? 'active' : ''}`}
            onClick={() => setActiveCategory(chip)}
          >
            {chip}
          </span>
        ))}
      </div>

      <div className="page-wrap">
        <div className="posts-header">
          <span className="posts-count">// 전체 47개 게시글</span>
          <Link to="/write" className="btn-primary">+ 글쓰기</Link>
        </div>

        <div className="posts">
          {MOCK_POSTS.map((post, idx) => (
            <PostCard key={post.id} post={post} featured={idx === 0} />
          ))}
        </div>

        <div className="pagination">
          <button className="page-btn">‹</button>
          <button className="page-btn active">1</button>
          <button className="page-btn">2</button>
          <button className="page-btn">3</button>
          <button className="page-btn">›</button>
        </div>
      </div>
    </div>
  )
}

export default BoardPage
