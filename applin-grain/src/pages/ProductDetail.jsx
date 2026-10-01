import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useProduct } from '../hooks/useProducts.js';
import { useCart } from '../context/CartContext.jsx';
import { formatPrice } from '../utils/format.js';

export default function ProductDetail() {
  const { id } = useParams();
  const { data: product, loading, error } = useProduct(id);
  const { addItem, openCart } = useCart();
  const [qty, setQty] = useState(1);
  const [adding, setAdding] = useState(false);

  if (loading) return <div className="mx-auto mt-10 h-96 max-w-5xl animate-pulse rounded-2xl bg-oliva-claro/15" />;
  if (error) {
    return (
      <div className="mx-auto max-w-5xl p-10">
        <p className="text-terracota dark:text-lima">{error.status === 404 ? 'Este producto no existe.' : error.message}</p>
        <Link to="/" className="mt-4 inline-block underline">Volver al catálogo</Link>
      </div>
    );
  }

  const soldOut = product.stock === 0;

  const handleAdd = async () => {
    setAdding(true);
    const ok = await addItem(product, qty);
    setAdding(false);
    if (ok) openCart();
  };

  return (
    <motion.article
      initial={{ opacity: 0 }} animate={{ opacity: 1 }}
      className="mx-auto grid max-w-5xl gap-10 px-4 py-10 md:grid-cols-2"
    >
      <div className="overflow-hidden rounded-3xl bg-white shadow-md dark:bg-oliva">
        {product.image
          ? <img src={product.image} alt={product.name} className={`aspect-square w-full object-cover ${soldOut ? 'grayscale' : ''}`} />
          : <div className="flex aspect-square items-center justify-center text-7xl">☕</div>}
      </div>

      <div className="flex flex-col">
        <Link to="/" className="mb-4 text-sm underline">← Volver al catálogo</Link>
        <h1 className="font-display text-4xl font-bold text-cafe dark:text-crema">{product.name}</h1>
        <p className="mt-3 text-3xl font-bold text-cafe dark:text-lima">{formatPrice(product.price)}</p>
        <p className="mt-5 max-w-prose whitespace-pre-line leading-relaxed">{product.description}</p>

        <p className={`mt-5 text-sm font-medium ${soldOut ? 'text-terracota dark:text-terracota-muted' : ''}`}>
          {soldOut ? 'Sin stock por ahora' : `${product.stock} disponibles`}
        </p>

        <div className="mt-6 flex items-center gap-4">
          <div className="flex items-center gap-3 rounded-full bg-oliva-claro/15 px-2 py-1">
            <button onClick={() => setQty((q) => Math.max(1, q - 1))} disabled={soldOut || qty <= 1} aria-label="Disminuir cantidad" className="h-8 w-8 rounded-full font-bold disabled:opacity-40">−</button>
            <span className="w-6 text-center font-bold text-cafe dark:text-crema" aria-live="polite">{qty}</span>
            <button onClick={() => setQty((q) => Math.min(product.stock, q + 1))} disabled={soldOut || qty >= product.stock} aria-label="Aumentar cantidad" className="h-8 w-8 rounded-full font-bold disabled:opacity-40">+</button>
          </div>

          {/* Regla de negocio: stock === 0 => botón deshabilitado */}
          <button
            onClick={handleAdd}
            disabled={soldOut || adding}
            className="flex-1 rounded-xl bg-ocre px-6 py-3 font-bold text-cafe transition hover:brightness-110
                       disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:brightness-100
                       dark:bg-terracota-muted dark:text-crema"
          >
            {soldOut ? 'Sin stock' : adding ? 'Agregando…' : 'Agregar al carrito'}
          </button>
        </div>
      </div>
    </motion.article>
  );
}
