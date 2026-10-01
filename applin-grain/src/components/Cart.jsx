import { useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { useCart } from '../context/CartContext.jsx';
import { formatPrice } from '../utils/format.js';

export default function Cart() {
  const { items, isOpen, closeCart, total, setQuantity, removeItem, checkout } = useCart();
  const [busy, setBusy] = useState(false);

  const handleCheckout = async () => {
    setBusy(true);
    await checkout();
    setBusy(false);
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div
            key="overlay"
            className="fixed inset-0 z-50 bg-cafe/60"
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            onClick={closeCart}
          />
          <motion.aside
            key="panel"
            role="dialog"
            aria-label="Carrito de compras"
            className="fixed right-0 top-0 z-50 flex h-full w-full max-w-md flex-col bg-crema shadow-2xl dark:bg-cafe"
            initial={{ x: '100%' }} animate={{ x: 0 }} exit={{ x: '100%' }}
            transition={{ type: 'tween', duration: 0.3 }}
          >
            <div className="flex items-center justify-between border-b border-oliva-claro/20 p-4">
              <h2 className="font-display text-xl font-bold text-cafe dark:text-crema">Tu carrito</h2>
              <button onClick={closeCart} aria-label="Cerrar carrito" className="rounded-full p-2 hover:bg-oliva-claro/15">✕</button>
            </div>

            <div className="flex-1 space-y-3 overflow-y-auto p-4">
              {items.length === 0 && (
                <p className="rounded-xl bg-oliva-claro/10 p-4 text-sm">Tu carrito está vacío. Elige un café para empezar.</p>
              )}
              <AnimatePresence initial={false}>
                {items.map(({ product, quantity }) => (
                  <motion.div
                    key={product.id}
                    layout
                    initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }}
                    className="flex gap-3 overflow-hidden rounded-xl bg-white p-3 shadow-sm dark:bg-oliva"
                  >
                    <div className="h-16 w-16 shrink-0 overflow-hidden rounded-lg bg-crema">
                      {product.image && <img src={product.image} alt="" className="h-full w-full object-cover" />}
                    </div>
                    <div className="flex-1">
                      <p className="font-medium text-cafe dark:text-crema">{product.name}</p>
                      <p className="text-sm">{formatPrice(product.price)}</p>
                      <div className="mt-1 flex items-center gap-2">
                        <button onClick={() => setQuantity(product, quantity - 1)} aria-label="Quitar uno" className="h-7 w-7 rounded-full bg-oliva-claro/20 font-bold">−</button>
                        <span className="w-6 text-center text-cafe dark:text-crema">{quantity}</span>
                        <button onClick={() => setQuantity(product, quantity + 1)} disabled={quantity >= product.stock} aria-label="Agregar uno" className="h-7 w-7 rounded-full bg-oliva-claro/20 font-bold disabled:cursor-not-allowed disabled:opacity-40">+</button>
                        <button onClick={() => removeItem(product.id)} className="ml-auto text-sm text-terracota underline dark:text-lima">Quitar</button>
                      </div>
                    </div>
                  </motion.div>
                ))}
              </AnimatePresence>
            </div>

            <div className="space-y-3 border-t border-oliva-claro/20 p-4">
              <div className="flex justify-between text-lg font-bold text-cafe dark:text-crema">
                <span>Total</span><span>{formatPrice(total)}</span>
              </div>
              <button
                onClick={handleCheckout}
                disabled={items.length === 0 || busy}
                className="w-full rounded-xl bg-ocre py-3 font-bold text-cafe transition hover:brightness-110
                           disabled:cursor-not-allowed disabled:opacity-50 dark:bg-terracota-muted dark:text-crema"
              >
                {busy ? 'Verificando stock…' : 'Finalizar compra'}
              </button>
            </div>
          </motion.aside>
        </>
      )}
    </AnimatePresence>
  );
}
