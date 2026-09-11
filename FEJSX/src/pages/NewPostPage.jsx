import { useNavigate } from 'react-router-dom'
import { PostForm } from '../components/post/PostForm'

export function NewPostPage() {
  const navigate = useNavigate()

  return (
    <>
      <h1 className="page-title">Nuovo post</h1>
      <p className="page-subtitle">
        Scrivi il testo, allega le foto e indica la posizione.
      </p>
      <PostForm onPublished={() => navigate('/')} />
    </>
  )
}
