import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { AppShell } from './components/layout/AppShell'
import { FeedPage } from './pages/FeedPage'
import { NewPostPage } from './pages/NewPostPage'
import { ProfilePage } from './pages/ProfilePage'
import './components/ui/ui.css'

function App() {
  return (
    <BrowserRouter>
      <AppShell>
        <Routes>
          <Route path="/" element={<FeedPage />} />
          <Route path="/nuovo" element={<NewPostPage />} />
          <Route path="/profilo" element={<ProfilePage />} />
        </Routes>
      </AppShell>
    </BrowserRouter>
  )
}

export default App
