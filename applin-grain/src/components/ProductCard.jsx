import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { formatPrice } from '../utils/format.js';

const cardVariants = {
  hidden: { opacity: 0 },
  show: { opacity: 1, transition: { duration: 0.5 } },
};

export default function ProductCard({ product }) {
  const soldOut = product.stock === 0;

  return (
    <motion.li variants={cardVariants} className="list-none">
      <Link
        to={`/product/${product.id}`}
        className="group block overflow-hidden rounded-2xl bg-white shadow-md transition duration-300
                   hover:-translate-y-1 hover:shadow-xl dark:bg-oliva"
      >
        <div className="relative aspect-square overflow-hidden bg-crema dark:bg-cafe/40">
          {product.image ? (
            <img
              src={product.image}
              alt={product.name}
              loading="lazy"
              className={`h-full w-full object-cover transition duration-500 group-hover:scale-105 ${soldOut ? 'grayscale' : ''}`}
            />
          ) : (
            <div className="flex h-full items-center justify-center text-5xl">☕</div>
          )}
          {soldOut && (
            <span className="absolute left-3 top-3 rounded-full bg-terracota px-3 py-1 text-xs font-bold text-white dark:bg-terracota-muted">
              Sin stock
            </span>
          )}
        </div>
        <div className="space-y-1 p-4">
          <h3 className="font-display text-lg font-bold leading-snug text-cafe dark:text-crema">{product.name}</h3>
          <p className="line-clamp-2 text-sm text-oliva-claro dark:text-crema/80">{product.description}</p>
          <p className="pt-1 text-lg font-bold text-cafe dark:text-lima">{formatPrice(product.price)}</p>
        </div>
      </Link>
    </motion.li>
  );
}
