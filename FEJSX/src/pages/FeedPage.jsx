import { useCallback, useEffect, useState } from 'react'
import { deletePost, fetchPosts } from '../api/posts'
import { Composer } from '../components/post/Composer'
import { PostCard } from '../components/feed/PostCard'
import { Icon } from '../components/ui/Icon'
import { Alert } from '../components/ui/Alert'

export function FeedPage() {
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = useCallback(async () => {
    try {
      const page = await fetchPosts({ page: 0, size: 20 })
      setPosts(page.content ?? [])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    // "cancelled" evita di aggiornare lo stato se il componente
    // sparisce mentre la richiesta e' ancora in corso.
    let cancelled = false

    async function initialLoad() {
      try {
        const page = await fetchPosts({ page: 0, size: 20 })
        if (!cancelled) setPosts(page.content ?? [])
      } catch (err) {
        if (!cancelled) setError(err.message)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    initialLoad()
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
    <div className="stack">
      <Composer onPublished={load} />

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
          <h2 className="empty__title">La bacheca è vuota</h2>
          <p>Scrivi qualcosa qui sopra per pubblicare il primo post.</p>
        </div>
      ) : (
        posts.map((post) => (
          <PostCard key={post.id} post={post} onDelete={handleDelete} />
        ))
      )}
    </div>
  )
}
