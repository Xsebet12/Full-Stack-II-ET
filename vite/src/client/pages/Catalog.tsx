import React, { useEffect, useState } from 'react'
import { addToCart, getProductos, addPendingItem, processPendingCheckout } from '../api/client'
import { useLocation, Link, useNavigate } from 'react-router-dom'
import { getAuthToken } from '../api/client'

type Imagen={url:string}
type Producto={id:number; nombre:string; descripcion?:string; precio?:number; imagen?:string; imagenes?:Imagen[]}

export default function Catalog(){
  const [items,setItems]=useState<Producto[]>([])
  const location = useLocation()
  const navigate = useNavigate()
  const params = new URLSearchParams(location.search)
  const q = params.get('q') || ''
  useEffect(()=>{
    let ignore=false
    async function load(){
      try{ const list = await getProductos(q); if(!ignore) setItems(list) }
      catch(e:any){ alert(String(e?.message||'No se pudo cargar catálogo')) }
    }
    load(); return ()=>{ignore=true}
  },[q])
  return (
    <main className="container py-4" style={{marginTop:70}}>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 className="m-0">Productos</h2>
        <Link to="/carrito" className="btn btn-outline-primary">Ver carrito</Link>
      </div>
      <div className="row">
        {items.map(p=>{
          const img=p.imagen||(p.imagenes&&p.imagenes[0]?.url)||'/vite.svg'
          return (
            <div className="col-md-4 mb-4" key={p.id}>
              <div className="card shadow-sm h-100">
                <Link to={`/producto/${p.id}`}><img src={img} className="card-img-top" alt={p.nombre} loading="lazy" decoding="async" /></Link>
                <div className="card-body text-center d-flex flex-column">
                  <h5 className="card-title"><Link to={`/producto/${p.id}`}>{p.nombre}</Link></h5>
                  <p className="card-text">${p.precio??0}</p>
                  <p className="card-text">{p.descripcion??''}</p>
                  <div className="d-flex align-items-center gap-2 mt-auto">
                    <input type="number" className="form-control" defaultValue={1} min={1} style={{width:100}} id={`qty-${p.id}`}/>
                    <button className="btn btn-outline-success" onClick={async()=>{
                      const tok = getAuthToken()
                      if(!tok){ alert('Debes iniciar sesión para agregar al carrito'); navigate('/login'); return }
                      const el = document.getElementById(`qty-${p.id}`) as HTMLInputElement
                      let q = Number(el?.value||1)
                      if(!Number.isFinite(q) || q<1){ alert('Cantidad inválida'); return }
                      q = Math.min(99, Math.floor(q))
                      try{ await addToCart(p.id, q); alert('Producto agregado') }
                      catch(e:any){
                        if(e?.status===401){ alert('Sesión expirada o no autenticado. Inicia sesión.'); navigate('/login'); return }
                        if(e?.status===500){
                          addPendingItem(p.id, q)
                          alert('El carrito dio error, pero el producto quedó guardado para Comprar ahora')
                          return
                        }
                        alert(String(e?.message||'No se pudo agregar'))
                      }
                    }}>Agregar al carrito</button>
                    <button className="btn btn-primary" onClick={async()=>{
                      const tok = getAuthToken()
                      if(!tok){ alert('Debes iniciar sesión para comprar'); navigate('/login'); return }
                      const el = document.getElementById(`qty-${p.id}`) as HTMLInputElement
                      let q = Number(el?.value||1)
                      if(!Number.isFinite(q) || q<1){ alert('Cantidad inválida'); return }
                      q = Math.min(99, Math.floor(q))
                      try{ addPendingItem(p.id, q); const r = await processPendingCheckout(); alert(`Compra registrada: ${r?.id??''}`); navigate('/carrito') }
                      catch(e:any){ alert(String(e?.message||'No se pudo completar la compra')) }
                    }}>Comprar ahora</button>
                  </div>
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </main>
  )
}
