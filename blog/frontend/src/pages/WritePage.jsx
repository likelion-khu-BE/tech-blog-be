import { useState } from 'react'
import { Link } from 'react-router-dom'
import './WritePage.css'

function WritePage() {
  const [title, setTitle] = useState('')
  const [body, setBody] = useState('')
  const [tags, setTags] = useState(['Spring Boot', 'AWS EC2'])
  const [tagInput, setTagInput] = useState('')
  const [status, setStatus] = useState('draft')

  const handleTagKeyDown = (e) => {
    if (e.key === 'Enter' && tagInput.trim()) {
      e.preventDefault()
      if (!tags.includes(tagInput.trim())) {
        setTags([...tags, tagInput.trim()])
      }
      setTagInput('')
    }
  }

  const removeTag = (tagToRemove) => {
    setTags(tags.filter(tag => tag !== tagToRemove))
  }

  const handleSubmit = () => {
    alert('저장되었습니다.')
  }

  return (
    <div className="write-wrap">
      <div className="page-title">새 글 작성</div>

      <div className="form-group">
        <div className="ai-field-wrap">
          <input
            className="input-title"
            type="text"
            placeholder="제목을 입력하세요"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
          <span className="ai-tab-hint">Tab ↹ 완성</span>
        </div>
      </div>

      <div className="form-group">
        <label>카테고리</label>
        <select className="input-text">
          <option value="">카테고리를 선택하세요</option>
          <optgroup label="AI">
            <option>LLM</option>
            <option>MLOps</option>
            <option>모델 서빙</option>
            <option>모델 학습</option>
          </optgroup>
          <optgroup label="백엔드">
            <option>CI/CD</option>
            <option>DevOps</option>
            <option>Security</option>
          </optgroup>
          <optgroup label="해커톤">
            <option>해커톤 후기</option>
          </optgroup>
        </select>
      </div>

      <div className="form-group">
        <label>기수</label>
        <select className="input-text">
          <option>13기</option>
          <option>12기</option>
          <option>11기</option>
        </select>
      </div>

      <div className="form-group">
        <label>태그</label>
        <div className="tag-input-wrap">
          {tags.map(tag => (
            <span key={tag} className="tag-item">
              {tag} <span className="tag-remove" onClick={() => removeTag(tag)}>×</span>
            </span>
          ))}
          <input
            className="tag-input"
            type="text"
            placeholder="태그 입력 후 Enter"
            value={tagInput}
            onChange={(e) => setTagInput(e.target.value)}
            onKeyDown={handleTagKeyDown}
          />
        </div>
      </div>

      <div className="form-group">
        <label>본문</label>
        <div className="editor-toolbar">
          <button className="toolbar-btn" title="Bold">B</button>
          <button className="toolbar-btn" title="Italic" style={{ fontStyle: 'italic' }}>I</button>
          <button className="toolbar-btn" title="Strikethrough" style={{ textDecoration: 'line-through' }}>S</button>
          <div className="toolbar-divider"></div>
          <button className="toolbar-btn" title="H1">H1</button>
          <button className="toolbar-btn" title="H2">H2</button>
          <button className="toolbar-btn" title="H3">H3</button>
          <div className="toolbar-divider"></div>
          <button className="toolbar-btn" title="Code">&lt;/&gt;</button>
          <button className="toolbar-btn" title="Quote">❝</button>
          <button className="toolbar-btn" title="Link">🔗</button>
          <button className="toolbar-btn" title="Image">🖼</button>
          <button className="toolbar-btn" title="Video">🎬</button>
          <button className="toolbar-btn" title="Diagram">📐</button>
        </div>
        <textarea
          className="editor-body"
          placeholder="본문을 작성하세요. 마크다운을 지원합니다."
          value={body}
          onChange={(e) => setBody(e.target.value)}
        />
        <div className="gh-notice">
          <div className="gh-notice-icon">🐙</div>
          <div className="gh-notice-text">
            <strong>GitHub PR로 작성하기</strong> — 마크다운 파일을 PR로 올리면 관리자 승인 후 자동 발행됩니다.
            <a href="#" className="gh-link">가이드 보기 →</a>
          </div>
        </div>
      </div>

      <div className="form-actions">
        <Link to="/board" className="btn-secondary">취소</Link>
        <div className="actions-right">
          <div className="publish-toggle">
            <button
              className={`toggle-opt ${status === 'draft' ? 'active-draft' : ''}`}
              onClick={() => setStatus('draft')}
            >
              ✏️ Draft
            </button>
            <button
              className={`toggle-opt ${status === 'pub' ? 'active-pub' : ''}`}
              onClick={() => setStatus('pub')}
            >
              🚀 발행
            </button>
          </div>
          <button className="btn-primary" onClick={handleSubmit}>저장하기</button>
        </div>
      </div>
    </div>
  )
}

export default WritePage
