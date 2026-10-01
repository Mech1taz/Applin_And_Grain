import { useMemo, useState } from 'react';
import { motion } from 'framer-motion';
import ProductCard from '../components/ProductCard.jsx';
import { useProducts } from '../hooks/useProducts.js';
import { normalize } from '../utils/format.js';

const FILTERS = [
  { label: 'Todos', match: '' },
  { label: 'Prensa francesa', match: 'prensa' },
  { label: 'V60', match: 'v60' },
];

const grid = { hidden: {}, show: { transition: { staggerChildren: 0.07 } } };

export default function Home() {
  const { data: products, loading, error, reload } = useProducts();
  const [filter, setFilter] = useState(FILTERS[0]);

  const visible = useMemo(
    () => (products ?? []).filter((p) => normalize(`${p.name} ${p.description}`).includes(filter.match)),
    [products, filter]
  );

  return (
    <section className="mx-auto max-w-6xl px-4 py-10">
      <h1 className="font-display text-4xl font-bold text-cafe dark:text-crema">Café de especialidad</h1>
      <p className="mt-2 max-w-prose">Tostado en lotes pequeños. Elige tu método y te mostramos lo que mejor funciona.</p>

      <div className="mt-6 flex flex-wrap gap-2">
        {FILTERS.map((f) => (
          <button
            key={f.label}
            onClick={() => setFilter(f)}
            aria-pressed={filter === f}
            className={`rounded-full px-4 py-1.5 text-sm font-medium transition ${
              filter === f
                ? 'bg-lima text-cafe'
                : 'bg-oliva-claro/15 text-cafe hover:bg-oliva-claro/30 dark:text-crema'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {loading && (
        <div className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-4" aria-busy="true">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="h-72 animate-pulse rounded-2xl bg-oliva-claro/15" />
          ))}
        </div>
      )}

      {error && (
        <div className="mt-8 rounded-2xl bg-terracota/10 p-6 text-terracota dark:text-lima">
          <p className="font-medium">{error.message}</p>
          <button onClick={reload} className="mt-3 rounded-lg bg-terracota px-4 py-2 text-sm font-bold text-white dark:bg-terracota-muted">Reintentar</button>
        </div>
      )}

      {!loading && !error && visible.length === 0 && (
        <p className="mt-8 rounded-xl bg-oliva-claro/10 p-4">No hay productos para este método todavía.</p>
      )}

      {!loading && !error && visible.length > 0 && (
        <motion.ul
          key={filter.label}
          variants={grid} initial="hidden" animate="show"
          className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-4"
        >
          {visible.map((p) => <ProductCard key={p.id} product={p} />)}
        </motion.ul>
      )}
    </section>
  );
}
