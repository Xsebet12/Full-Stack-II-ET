import Header from '../components/Header'
import LoginForm from '../components/LoginForm'
import Footer from '../components/Footer'
import { useNavigate } from 'react-router-dom'

export default function AdminLogin() {
  const navigate = useNavigate()
  return (
    <main id="main" className="container-fluid p-0">
      <Header />
      <main id="main-adm">
        {/* Reemplazar LoginForm sin props por versión con onAuthenticated */}
        <LoginForm onAuthenticated={(user) => {
          if (user?.rol === 'ADMIN') {
            navigate('/admin')
          } else {
            alert('Esta cuenta no tiene rol ADMIN')
          }
        }} />
      </main>
      <Footer />
    </main>
  )
}