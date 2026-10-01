import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useCart } from '../context/CartContext.jsx';
import { useTheme } from '../hooks/useTheme.js';

export default function Header() {
  const { count, openCart } = useCart();
  const { dark, toggle } = useTheme();

  return (
    <header className="sticky top-0 z-40 border-b border-oliva-claro/20 bg-crema/90 backdrop-blur dark:bg-cafe/90">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link to="/" className="flex items-center gap-3">
          {/* Coloca tu logo en /public/logo.png */}
          <img src="/logo.png" alt="" className="h-10 w-10 object-contain" onError={(e) => (e.currentTarget.style.display = 'none')} />
          <span className="font-display text-xl font-bold tracking-tight text-cafe dark:text-crema">
            Applin &amp; Grain
          </span>
        </Link>

        <div className="flex items-center gap-2">
          <button
            onClick={toggle}
            aria-label={dark ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro'}
            className="rounded-full p-2 text-lg transition hover:bg-oliva-claro/15"
          >
            {dark ? '☀️' : '🌙'}
          </button>

          <button
            onClick={openCart}
            aria-label={`Abrir carrito, ${count} ítems`}
            className="relative rounded-full p-2 text-cafe transition hover:bg-oliva-claro/15 dark:text-crema"
          >
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M5 8h14l-1.2 11.2a2 2 0 0 1-2 1.8H8.2a2 2 0 0 1-2-1.8L5 8Z" />
              <path d="M9 8V6a3 3 0 0 1 6 0v2" />
            </svg>
            <AnimatePresence>
              {count > 0 && (
                <motion.span
                  key={count}
                  initial={{ scale: 0.4 }}
                  animate={{ scale: 1 }}
                  exit={{ scale: 0 }}
                  className="absolute -right-0.5 -top-0.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-lima px-1 text-xs font-bold text-cafe"
                >
                  {count}
                </motion.span>
              )}
            </AnimatePresence>
          </button>
        </div>
      </div>
    </header>
  );
}
