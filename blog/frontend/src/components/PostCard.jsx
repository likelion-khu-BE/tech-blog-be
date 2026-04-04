import { Link } from 'react-router-dom'
import Avatar from './Avatar'
import Chip from './Chip'
import './PostCard.css'

function PostCard({ post, featured = false }) {
  return (
    <Link to={`/post/${post.id}`} className={`post-card ${featured ? 'featured' : ''}`}>
      <div className="card-chips">
        {featured && <Chip variant="orange">FEATURED</Chip>}
        {post.tags?.map((tag, idx) => (
          <Chip key={idx} variant={idx === 0 && !featured ? 'orange' : 'neutral'}>{tag}</Chip>
        ))}
      </div>
      <h2>{post.title}</h2>
      <p>{post.preview}</p>
      <div className="card-footer">
        <div className="author">
          <Avatar name={post.authorName} size="sm" />
          <div>
            <div className="author-name">{post.authorName}</div>
            <div className="post-date">{post.date}</div>
          </div>
        </div>
        <span className="read-link">읽기 →</span>
      </div>
    </Link>
  )
}

export default PostCard
