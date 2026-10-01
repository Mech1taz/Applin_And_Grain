import { Route, Routes } from 'react-router-dom';
import Header from './components/Header.jsx';
import Cart from './components/Cart.jsx';
import Home from './pages/Home.jsx';
import ProductDetail from './pages/ProductDetail.jsx';

export default function App() {
  return (
    <>
      <Header />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/product/:id" element={<ProductDetail />} />
          <Route path="*" element={<p className="p-10">Página no encontrada.</p>} />
        </Routes>
      </main>
      <Cart />
    </>
  );
}
