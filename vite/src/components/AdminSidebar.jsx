import { Link } from 'react-router-dom'

export default function AdminSidebar() {
  return (
    <aside className="col-md-2 d-none d-md-block border-end sidebar left_bar">
      <ul className="navbar-nav p-3">
        <li><span className="nav-link fw-bold">Usuarios</span></li>
        <li><Link className="nav-link" to="/admin/users/create/empleado">Crear</Link></li>
        <li><Link className="nav-link" to="/admin/users">Modificar</Link></li>

        <li className="mt-3"><span className="nav-link fw-bold">Productos</span></li>
        <li><Link className="nav-link" to="/admin/products/new">Crear</Link></li>
        <li><Link className="nav-link" to="/admin/products">Modificar</Link></li>

        <li className="mt-3"><span className="nav-link fw-bold">Proveedores</span></li>
        <li><Link className="nav-link" to="/admin/providers/new">Crear</Link></li>
        <li><Link className="nav-link" to="/admin/providers">Modificar</Link></li>
      </ul>
    </aside>
  )
}
