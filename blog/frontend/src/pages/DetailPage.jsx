import { useState } from 'react'
import { Link } from 'react-router-dom'
import Avatar from '../components/Avatar'
import Chip from '../components/Chip'
import './DetailPage.css'

function DetailPage() {
  const [liked, setLiked] = useState(true)
  const [likeCount, setLikeCount] = useState(14)
  const [bookmarked, setBookmarked] = useState(false)

  const toggleLike = () => {
    setLiked(!liked)
    setLikeCount(liked ? likeCount - 1 : likeCount + 1)
  }

  const copyLink = () => {
    navigator.clipboard.writeText(window.location.href)
    alert('링크가 복사되었습니다.')
  }

  return (
    <div className="detail-wrap">
      {/* DRAFT BANNER */}
      <div className="draft-banner">
        ✏️ <strong>Draft</strong> — 아직 발행되지 않은 글입니다. 본인에게만 보입니다.
        <Link to="/edit/1" className="draft-edit-link">수정하러 가기 →</Link>
      </div>

      {/* BREADCRUMB */}
      <div className="breadcrumb">
        <Link to="/">홈</Link>
        <span>/</span>
        <Link to="/board">게시판</Link>
        <span>/</span>
        Spring Boot
      </div>

      {/* POST HEADER */}
      <div className="post-chips">
        <Chip variant="orange">Spring Boot</Chip>
        <Chip variant="neutral">AWS EC2</Chip>
        <Chip variant="neutral">CI/CD</Chip>
      </div>

      <h1 className="post-title">
        Spring Boot 애플리케이션을 EC2에<br />GitHub Actions로 자동 배포하기
      </h1>

      <div className="post-meta">
        <div className="author">
          <Avatar name="장찬욱" size="md" />
          <div>
            <div className="author-name">장찬욱</div>
            <div className="post-date">2026.03.28 · 읽는 시간 약 5분</div>
          </div>
        </div>
        <div className="meta-actions">
          <Link to="/edit/1" className="btn-secondary">수정</Link>
          <button className="btn-danger">삭제</button>
        </div>
      </div>

      {/* POST BODY */}
      <div className="post-body">
        <p>
          이번 포스트에서는 <code>Spring Boot</code> 프로젝트를 AWS EC2에 자동 배포하는
          파이프라인을 구성한 과정을 공유합니다. GitHub Actions와 Docker를 활용해
          push만 하면 자동으로 배포되는 구조를 만들었습니다.
        </p>

        <h2>전체 구조</h2>
        <p>크게 세 단계로 나뉩니다. GitHub에 push → Actions가 Docker 이미지 빌드 → EC2에 SSH 접속 후 컨테이너 재시작.</p>

        <pre><code>{`GitHub Push
  └─ GitHub Actions
       ├─ Build (./gradlew build)
       ├─ Docker build & push (DockerHub)
       └─ EC2 SSH → docker pull & run`}</code></pre>

        <h2>GitHub Actions 워크플로우</h2>
        <p>아래는 실제 사용한 <code>.github/workflows/deploy.yml</code> 파일입니다.</p>

        <pre><code>{`name: Deploy to EC2

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build with Gradle
        run: ./gradlew build -x test
      - name: Docker build & push
        run: |
          docker build -t \${{ secrets.DOCKER_USERNAME }}/blog .
          docker push \${{ secrets.DOCKER_USERNAME }}/blog`}</code></pre>

        <h3>트러블슈팅</h3>
        <p>
          배포 중 가장 많이 만난 문제는 EC2 보안 그룹 설정이었습니다.
          8080 포트를 열지 않아서 접속이 안 됐는데, 인바운드 규칙에 추가하니 바로 해결됐습니다.
        </p>

        <blockquote>
          실수하기 쉬운 부분: EC2 퍼블릭 IP는 재시작 시 바뀝니다.
          Elastic IP를 붙여두는 걸 추천합니다.
        </blockquote>

        <p>전체 코드는 GitHub 레포지토리에서 확인할 수 있습니다. 질문은 댓글로 남겨주세요!</p>
      </div>

      {/* ACTION BAR */}
      <div className="action-bar">
        <button className={`action-btn ${liked ? 'active' : ''}`} onClick={toggleLike}>
          ❤️ <span>{likeCount}</span>
        </button>
        <button className={`action-btn ${bookmarked ? 'active' : ''}`} onClick={() => setBookmarked(!bookmarked)}>
          🔖 <span>7</span>
        </button>
        <Link to="/write" className="action-btn repost">↩️ Repost</Link>
        <div className="action-spacer"></div>
        <button className="action-btn" onClick={copyLink}>🔗 공유</button>
      </div>

      {/* COMMENT SECTION */}
      <div className="comment-section">
        <div className="comment-title">
          댓글 <span className="comment-count">3</span>
        </div>

        <div className="comment-input-wrap">
          <Avatar name="찬" size="md" />
          <div className="comment-input-box">
            <textarea className="comment-input" placeholder="댓글을 입력하세요..."></textarea>
            <div className="comment-submit-wrap">
              <button className="btn-primary">댓글 작성</button>
            </div>
          </div>
        </div>

        <div className="comment-list">
          <div className="comment-item">
            <Avatar name="희" size="sm" />
            <div className="comment-body">
              <div className="comment-header">
                <span className="comment-name">노희윤</span>
                <span className="comment-date">2026.03.29</span>
              </div>
              <div className="comment-text">
                Elastic IP 꼭 붙여야 한다는 거 저도 몰랐는데 덕분에 배웠어요!
              </div>
              <div className="comment-actions">
                <button className="comment-action-btn">❤️ 2</button>
                <button className="comment-action-btn">답글</button>
              </div>
            </div>
          </div>

          <div className="comment-item">
            <Avatar name="주" size="sm" />
            <div className="comment-body">
              <div className="comment-header">
                <span className="comment-name">김주연</span>
                <span className="comment-date">2026.03.30</span>
              </div>
              <div className="comment-text">
                보안 그룹 설정 저도 맨날 헷갈리는데 잘 정리해주셨네요.
                다음엔 ALB 붙이는 것도 써주시면 좋겠어요!
              </div>
              <div className="comment-actions">
                <button className="comment-action-btn">❤️ 1</button>
                <button className="comment-action-btn">답글</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <hr className="post-divider" />

      <div className="post-footer">
        <Link to="/board" className="btn-secondary">← 목록으로</Link>
        <div className="footer-actions">
          <Link to="/edit/1" className="btn-secondary">수정</Link>
          <button className="btn-danger">삭제</button>
        </div>
      </div>
    </div>
  )
}

export default DetailPage
