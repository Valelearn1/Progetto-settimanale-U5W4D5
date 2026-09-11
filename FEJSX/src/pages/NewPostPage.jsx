import { useNavigate } from 'react-router-dom'
import { PostForm } from '../components/post/PostForm'

export function NewPostPage() {
  const navigate = useNavigate()

  return (
    <>
      <h1 className="page-title">Nuovo momento</h1>
      <p className="page-subtitle">
        Scrivi due righe, allega le foto e segna dove eri.
      </p>
      <PostForm onPublished={() => navigate('/')} />
    </>
  )
}
