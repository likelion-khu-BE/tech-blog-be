import { Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import MainPage from './pages/MainPage'
import BoardPage from './pages/BoardPage'
import DetailPage from './pages/DetailPage'
import WritePage from './pages/WritePage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import AdminPage from './pages/AdminPage'
import ProfilePage from './pages/ProfilePage'
import './App.css'

function App() {
  return (
    <div className="app">
      <Header />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<MainPage />} />
          <Route path="/board" element={<BoardPage />} />
          <Route path="/post/:id" element={<DetailPage />} />
          <Route path="/write" element={<WritePage />} />
          <Route path="/edit/:id" element={<WritePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/admin" element={<AdminPage />} />
          <Route path="/profile/:id" element={<ProfilePage />} />
          <Route path="/members" element={<ProfilePage />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
