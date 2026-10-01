import axios from 'axios';

const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api',
  timeout: 10000,
});

/** Error normalizado: el resto de la app nunca toca el error crudo de axios. */
export class ApiError extends Error {
  constructor(message, { status = 0, code = null } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
  /** Detecta el InvalidStockException del backend (por código o por texto). */
  get isInvalidStock() {
    const hay = `${this.code ?? ''} ${this.message}`.toLowerCase();
    return hay.includes('invalidstock') || hay.includes('stock');
  }
}

// ErrorResponseDto del backend: se asume { message, error/code, status }.
http.interceptors.response.use(
  (res) => res,
  (err) => {
    const data = err.response?.data;
    const message =
      data?.message ??
      (err.code === 'ECONNABORTED'
        ? 'El servidor tardó demasiado en responder.'
        : err.response
          ? 'Ocurrió un error en el servidor.'
          : 'No pudimos conectar con el catálogo.');
    return Promise.reject(
      new ApiError(message, {
        status: err.response?.status,
        code: data?.error ?? data?.code ?? null,
      })
    );
  }
);

/** ProductResponseDto -> modelo del frontend (tolerante a nombres en es/en). */
const mapProduct = (dto) => ({
  id: dto.id,
  name: dto.nombre ?? dto.name ?? 'Sin nombre',
  description: dto.descripcion ?? dto.description ?? '',
  price: Number(dto.precio ?? dto.price ?? 0),
  image: dto.imagen ?? dto.imagenUrl ?? dto.image ?? null,
  stock: Number(dto.stock ?? 0),
});

export const catalogService = {
  async getProducts() {
    const { data } = await http.get('/products');
    const list = Array.isArray(data) ? data : (data.content ?? []); // soporta Page<>
    return list.map(mapProduct);
  },

  async getProductById(id) {
    const { data } = await http.get(`/products/${id}`);
    return mapProduct(data);
  },

  /**
   * Revalida stock contra el servidor (lo que hay en pantalla puede estar viejo).
   * Lanza ApiError de tipo InvalidStock si algún ítem excede el stock real.
   */
  async validateCart(items) {
    const fresh = await Promise.all(items.map((i) => this.getProductById(i.product.id)));
    fresh.forEach((p, idx) => {
      const wanted = items[idx].quantity;
      if (p.stock < wanted) {
        throw new ApiError(
          p.stock === 0
            ? `"${p.name}" se agotó.`
            : `Solo quedan ${p.stock} unidades de "${p.name}".`,
          { status: 409, code: 'InvalidStockException' }
        );
      }
    });
    return fresh;
  },
};
