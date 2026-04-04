import { useState } from 'react'
import { Link } from 'react-router-dom'
import PostCard from '../components/PostCard'
import Avatar from '../components/Avatar'
import './MainPage.css'

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
    preview: 'BOJ 2565번을 De Morgan → LIS 접근으로 해결한 풀이 과정.',
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

const MOCK_MEMBERS = [
  { name: '장찬욱', role: '백엔드 · 블로그팀장', posts: 12 },
  { name: '노희윤', role: '백엔드', posts: 8 },
  { name: '김주연', role: '백엔드', posts: 6 },
  { name: '김우진', role: '스터디장', posts: 9 }
]

const BOARD_TABS = ['전체', '🤖 AI', '⚙️ 백엔드', '🏆 해커톤']
const FILTER_TAGS = ['전체 기수', '13기', '12기', '11기', 'Spring', 'AWS', 'Docker', '알고리즘', '회고']

function MainPage() {
  const [activeTab, setActiveTab] = useState('전체')
  const [activeTag, setActiveTag] = useState('전체 기수')

  return (
    <div className="main-page">
      {/* HERO */}
      <section className="hero">
        <div className="hero-left">
          <div className="hero-eyebrow">Kyung Hee Univ · Backend</div>
          <h1>코드로 말하는<br /><em>기술 블로그</em></h1>
          <p>멋쟁이사자처럼 경희대 백엔드 팀의 기술 경험과 학습 기록. Spring, AWS, 알고리즘까지 함께 쌓아갑니다.</p>
          <div className="hero-btns">
            <Link to="/board" className="btn-primary">게시판 보기</Link>
            <Link to="/register" className="btn-outline">팀 합류하기</Link>
          </div>
        </div>
        <div className="hero-stats">
          <div className="stat">
            <div className="stat-number">12<span>+</span></div>
            <div className="stat-label">멤버</div>
          </div>
          <div className="stat">
            <div className="stat-number">47</div>
            <div className="stat-label">게시글</div>
          </div>
          <div className="stat">
            <div className="stat-number">3<span>기</span></div>
            <div className="stat-label">활동 기수</div>
          </div>
        </div>
      </section>

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

      {/* FILTER */}
      <div className="filter-bar">
        {FILTER_TAGS.map((tag, idx) => (
          <span key={tag}>
            {idx === 4 && <div className="filter-divider"></div>}
            <span
              className={`tag ${activeTag === tag ? 'active' : ''}`}
              onClick={() => setActiveTag(tag)}
            >
              {tag}
            </span>
          </span>
        ))}
      </div>

      {/* POSTS */}
      <div className="page-wrap">
        <div className="posts">
          {MOCK_POSTS.map((post, idx) => (
            <PostCard key={post.id} post={post} featured={idx === 0} />
          ))}
        </div>

        <div className="more-wrap">
          <Link to="/board" className="btn-more">게시글 더 보기</Link>
        </div>

        {/* MEMBERS */}
        <div className="section-wrap">
          <div className="section-title">멤버 소개</div>
          <div className="members-grid">
            {MOCK_MEMBERS.map(member => (
              <div key={member.name} className="member-card">
                <Avatar name={member.name} size="lg" />
                <div className="member-name">{member.name}</div>
                <div className="member-role">{member.role}</div>
                <div className="member-posts">{member.posts} posts</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

export default MainPage
