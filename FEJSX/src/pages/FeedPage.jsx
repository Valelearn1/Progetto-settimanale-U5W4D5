import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { deletePost, fetchPosts } from '../api/posts'
import { PostCard } from '../components/feed/PostCard'
import { Icon } from '../components/ui/Icon'
import { Alert } from '../components/ui/Alert'

export function FeedPage() {
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    // "cancelled" evita di aggiornare lo stato se il componente
    // sparisce mentre la richiesta e' ancora in corso.
    let cancelled = false

    async function load() {
      try {
        const page = await fetchPosts({ page: 0, size: 20 })
        if (!cancelled) setPosts(page.content ?? [])
      } catch (err) {
        if (!cancelled) setError(err.message)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  async function handleDelete(id) {
    // Rimozione ottimistica: l'elemento sparisce subito e viene
    // rimesso se la chiamata fallisce.
    const previous = posts
    setPosts((current) => current.filter((post) => post.id !== id))
    try {
      await deletePost(id)
    } catch (err) {
      setPosts(previous)
      setError(err.message)
    }
  }

  return (
    <>
      <h1 className="page-title">Bacheca</h1>
      <p className="page-subtitle">Gli ultimi post pubblicati, dal più recente.</p>

      {error && <Alert variant="error">{error}</Alert>}

      {loading ? (
        <div className="empty">
          <span className="spinner" />
        </div>
      ) : posts.length === 0 ? (
        <div className="card empty">
          <span className="empty__icon">
            <Icon name="board" size={24} />
          </span>
          <h2 className="empty__title">Ancora nessun post</h2>
          <p>La bacheca è vuota. Pubblica il primo post.</p>
          <Link to="/nuovo" className="btn btn--primary" style={{ marginTop: 'var(--space-4)' }}>
            <Icon name="plus" size={18} />
            Crea un post
          </Link>
        </div>
      ) : (
        <div className="stack">
          {posts.map((post) => (
            <PostCard key={post.id} post={post} onDelete={handleDelete} />
          ))}
        </div>
      )}
    </>
  )
}
