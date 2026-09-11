import { useNavigate } from 'react-router-dom'
import { Composer } from '../components/post/Composer'

/*
  Stessa schermata del composer in cima alla bacheca, ma aperta fin
  da subito: e' la versione a pagina intera, raggiungibile dal menu.
*/
export function NewPostPage() {
  const navigate = useNavigate()

  return (
    <>
      <h1 className="page-title">Nuovo post</h1>
      <p className="page-subtitle">
        Scrivi il testo, poi usa i pulsanti qui sotto per allegare foto,
        documenti o la posizione.
      </p>
      <Composer alwaysOpen onPublished={() => navigate('/')} />
    </>
  )
}
