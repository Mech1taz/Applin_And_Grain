import { createContext, useCallback, useContext, useEffect, useMemo, useReducer } from 'react';
import { catalogService } from '../services/catalogService.js';
import { useToast } from './ToastContext.jsx';

const CartContext = createContext(null);
export const useCart = () => useContext(CartContext);

const STORAGE_KEY = 'applin-grain-cart';

const load = () => {
  try {
    return { items: JSON.parse(localStorage.getItem(STORAGE_KEY)) ?? [], isOpen: false };
  } catch {
    return { items: [], isOpen: false };
  }
};

function reducer(state, action) {
  switch (action.type) {
    case 'SET_ITEM': { // fija la cantidad total de un producto
      const { product, quantity } = action;
      const exists = state.items.some((i) => i.product.id === product.id);
      const items = exists
        ? state.items.map((i) => (i.product.id === product.id ? { product, quantity } : i))
        : [...state.items, { product, quantity }];
      return { ...state, items };
    }
    case 'REMOVE':
      return { ...state, items: state.items.filter((i) => i.product.id !== action.id) };
    case 'CLEAR':
      return { ...state, items: [] };
    case 'OPEN':
      return { ...state, isOpen: true };
    case 'CLOSE':
      return { ...state, isOpen: false };
    default:
      return state;
  }
}

export function CartProvider({ children }) {
  const [state, dispatch] = useReducer(reducer, undefined, load);
  const { notify } = useToast();

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state.items));
  }, [state.items]);

  /**
   * Regla crítica: nunca se supera el stock. Se valida en cliente (rápido)
   * y se revalida contra el servidor (fuente de verdad).
   */
  const addItem = useCallback(
    async (product, quantity = 1) => {
      if (product.stock <= 0) {
        notify(`"${product.name}" está sin stock.`, 'error');
        return false;
      }
      const current = state.items.find((i) => i.product.id === product.id)?.quantity ?? 0;
      const total = current + quantity;
      if (total > product.stock) {
        notify(`Solo quedan ${product.stock} unidades de "${product.name}".`, 'error');
        return false;
      }
      try {
        const fresh = await catalogService.getProductById(product.id);
        if (fresh.stock < total) {
          notify(
            fresh.stock === 0
              ? `"${fresh.name}" se agotó recién.`
              : `Solo quedan ${fresh.stock} unidades de "${fresh.name}".`,
            'error'
          );
          return false;
        }
        dispatch({ type: 'SET_ITEM', product: fresh, quantity: total });
        notify(`${quantity} × ${fresh.name} agregado al carrito.`, 'success', 2500);
        return true;
      } catch (err) {
        notify(err.message, 'error'); // incluye InvalidStockException del backend
        return false;
      }
    },
    [state.items, notify]
  );

  const setQuantity = useCallback(
    (product, quantity) => {
      if (quantity < 1) return dispatch({ type: 'REMOVE', id: product.id });
      if (quantity > product.stock) {
        return notify(`Máximo disponible: ${product.stock}.`, 'error');
      }
      dispatch({ type: 'SET_ITEM', product, quantity });
    },
    [notify]
  );

  const removeItem = useCallback((id) => dispatch({ type: 'REMOVE', id }), []);

  /** Revalida todo el carrito con el servidor antes de pagar. */
  const checkout = useCallback(async () => {
    try {
      await catalogService.validateCart(state.items);
      notify('Stock confirmado. ¡Listo para continuar con el pago!', 'success');
      return true;
    } catch (err) {
      notify(err.message, 'error', 6000);
      return false;
    }
  }, [state.items, notify]);

  const value = useMemo(
    () => ({
      items: state.items,
      isOpen: state.isOpen,
      count: state.items.reduce((n, i) => n + i.quantity, 0),
      total: state.items.reduce((s, i) => s + i.quantity * i.product.price, 0),
      addItem,
      setQuantity,
      removeItem,
      checkout,
      clear: () => dispatch({ type: 'CLEAR' }),
      openCart: () => dispatch({ type: 'OPEN' }),
      closeCart: () => dispatch({ type: 'CLOSE' }),
    }),
    [state, addItem, setQuantity, removeItem, checkout]
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}
