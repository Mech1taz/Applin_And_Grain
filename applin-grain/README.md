<div align="center">
# ☕ Applin & Grain
 
**Tienda de café de especialidad · Frontend**
 
![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite&logoColor=white)
![Tailwind](https://img.shields.io/badge/Tailwind-3-06B6D4?logo=tailwindcss&logoColor=white)
![Framer Motion](https://img.shields.io/badge/Framer_Motion-11-0055FF?logo=framer&logoColor=white)
 
</div>
---
 
## 📖 ¿Qué es esto?
 
Interfaz web de **Applin & Grain**: catálogo de cafés, detalle de producto y carrito de compras, con modo claro y oscuro.
 
El frontend **no habla directamente con los microservicios**. Toda petición pasa por el **BFF** (Backend For Frontend), que se encarga de hablar con `ms-pedidos360-catalog` y el resto.
 
```
┌──────────────┐   /api/...   ┌─────────┐        ┌──────────────────────┐
│  Frontend    │ ───────────▶ │   BFF   │ ─────▶ │ ms-pedidos360-catalog│ :8080
│ React · 5173 │ ◀─────────── │  :8090  │ ◀───── │ (Spring Boot)        │
└──────────────┘              └─────────┘        └──────────────────────┘
```
 
---
 
## 🚀 Inicio rápido
 
### Requisitos
 
| Herramienta | Versión |
|---|---|
| Node.js | 18 o superior (`node -v`) |
| BFF | corriendo y accesible |
| ms-pedidos360-catalog | corriendo en `:8080` (el BFF lo consume) |
 
### Pasos
 
```powershell
cd "M:\CLOUD NATIVE I\Cafeteria\applin-grain"
npm install
npm run dev
```
 
Abre **http://localhost:5173** 🎉
 
> 💡 Copia tu logo a `public/logo.png`. Si no existe, el header muestra solo el nombre.
 
---
 
## ⚙️ Configuración (`.env`)
 
| Variable | Para qué sirve | Valor por defecto |
|---|---|---|
| `BFF_URL` | A dónde apunta el proxy de Vite en desarrollo | `http://localhost:8090` |
| `VITE_API_URL` | Base de las peticiones del front | `/api` |
 
⚠️ **El puerto `8090` es un supuesto.** Cámbialo por el puerto real de tu BFF y reinicia `npm run dev` (Vite solo lee `.env` al arrancar).
 
**En producción:** pon en `VITE_API_URL` la URL pública del BFF (por ejemplo `https://bff.midominio.cl/api`) y habilita CORS en el BFF para el dominio del front.
 
---
 
## 🔌 Contrato con el BFF
 
El front espera estos endpoints (rutas relativas a `VITE_API_URL`):
 
| Método | Ruta | Uso |
|---|---|---|
| `GET` | `/products` | Catálogo completo |
| `GET` | `/products/{id}` | Detalle y revalidación de stock |
 
**Forma del producto** (`ProductResponseDto`). `mapProduct` también acepta los nombres en inglés:
 
```json
{
  "id": 1,
  "nombre": "Café Huehuetenango",
  "descripcion": "Notas a chocolate y caramelo. Tueste medio.",
  "precio": 12990,
  "imagen": "https://.../cafe.jpg",
  "stock": 25
}
```
 
**Forma del error** (`ErrorResponseDto`). El front muestra `message` en un toast:
 
```json
{ "status": 409, "error": "InvalidStockException", "message": "Solo quedan 2 unidades" }
```
 
Si tu BFF usa otras rutas o campos, ajusta `src/services/catalogService.js` (es el único archivo que conoce la API).
 
---
 
## 🧠 Reglas de negocio
 
- **Stock = 0** → el botón *Agregar al carrito* queda deshabilitado (opacidad reducida), y la tarjeta muestra la etiqueta *Sin stock*.
- **Nunca se supera el stock**: se valida en pantalla y se vuelve a consultar al servidor antes de agregar.
- **Finalizar compra** revalida todo el carrito contra el servidor.
- **Errores del backend** (como `InvalidStockException`) se muestran en un toast con el mensaje del servidor.
- El carrito se guarda en `localStorage`: sobrevive a recargas.
---
 
## 🗂️ Estructura
 
```
applin-grain/
├── public/
│   └── logo.png                  ← tu logo
├── src/
│   ├── App.jsx                   Rutas + layout
│   ├── main.jsx                  Providers (Router, Toast, Cart)
│   ├── index.css                 Tailwind + estilos base
│   ├── components/
│   │   ├── Header.jsx            Header sticky, badge del carrito, modo oscuro
│   │   ├── ProductCard.jsx       Tarjeta del catálogo
│   │   └── Cart.jsx              Carrito lateral (slide-over)
│   ├── pages/
│   │   ├── Home.jsx              Grilla + filtros por método
│   │   └── ProductDetail.jsx     /product/:id
│   ├── context/
│   │   ├── CartContext.jsx       Estado global del carrito
│   │   └── ToastContext.jsx      Avisos al usuario
│   ├── hooks/
│   │   ├── useProducts.js        Carga de datos (loading / error / reload)
│   │   └── useTheme.js           Modo claro / oscuro
│   ├── services/
│   │   └── catalogService.js     Único punto de contacto con el BFF
│   └── utils/
│       └── format.js             Precio en CLP, normalización de texto
├── tailwind.config.js            Paleta de colores
├── vite.config.js                Proxy hacia el BFF
└── .env
```
 
---
 
## 🎨 Paleta
 
Los colores están definidos como tokens en `tailwind.config.js`.
 
| Elemento | Modo claro | Modo oscuro |
|---|---|---|
| Fondo principal | `#F2EDD0` crema | `#401A1A` café profundo |
| Tarjetas | `#FFFFFF` | `#6A7349` oliva oscuro |
| Texto principal | `#808C54` oliva (títulos en café) | `#F2EDD0` crema |
| Botón principal | `#D9A362` ocre tostado | `#A64141` terracota muted |
| Alertas / sin stock | `#BF5050` terracota | `#A64141` terracota muted |
| Acentos | `#E3F26D` lima pastel | `#808C54` oliva |
 
---
 
## 📜 Scripts
 
| Comando | Qué hace |
|---|---|
| `npm run dev` | Servidor de desarrollo con recarga en vivo |
| `npm run build` | Build de producción en `dist/` |
| `npm run preview` | Sirve el build para probarlo |
 
---
 
## 🛠️ Problemas comunes
 
| Síntoma | Causa probable | Solución |
|---|---|---|
| "No pudimos conectar con el catálogo" | BFF apagado o puerto incorrecto | Revisa `BFF_URL` en `.env` y reinicia `npm run dev` |
| Error 404 en `/api/products` | El BFF expone otra ruta | Ajusta las rutas en `catalogService.js` |
| Error de CORS en producción | El BFF no permite el dominio del front | Habilita CORS en el BFF |
| Productos sin nombre o precio | El DTO usa otros campos | Ajusta `mapProduct` en `catalogService.js` |
| `npm` no se reconoce | Node.js no instalado | Instálalo desde nodejs.org |
| Cambié `.env` y nada pasó | Vite lee `.env` solo al iniciar | Detén y vuelve a ejecutar `npm run dev` |