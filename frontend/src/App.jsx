import { Routes, Route } from 'react-router-dom';
import { Suspense, lazy } from 'react';

// Layout
import ConsumerLayout from './components/layout/ConsumerLayout';
import FarmerLayout from './components/layout/FarmerLayout';
import ProtectedRoute from './components/common/ProtectedRoute';
import RoleRoute from './components/common/RoleRoute';
import Spinner from './components/ui/Spinner';

// Pages (lazy loaded)
const Home = lazy(() => import('./pages/Home'));
const Login = lazy(() => import('./pages/auth/Login'));
const Register = lazy(() => import('./pages/auth/Register'));
const Shop = lazy(() => import('./pages/consumer/Shop'));
const ProductDetail = lazy(() => import('./pages/consumer/ProductDetail'));
const FarmProfile = lazy(() => import('./pages/consumer/FarmProfile'));
const Cart = lazy(() => import('./pages/consumer/Cart'));
const Checkout = lazy(() => import('./pages/consumer/Checkout'));
const Orders = lazy(() => import('./pages/consumer/Orders'));
const OrderDetail = lazy(() => import('./pages/consumer/OrderDetail'));
const Profile = lazy(() => import('./pages/consumer/Profile'));

// Farmer pages
const Dashboard = lazy(() => import('./pages/farmer/Dashboard'));
const FarmerProducts = lazy(() => import('./pages/farmer/Products'));
const AddProduct = lazy(() => import('./pages/farmer/AddProduct'));
const EditProduct = lazy(() => import('./pages/farmer/EditProduct'));
const FarmerOrders = lazy(() => import('./pages/farmer/FarmerOrders'));
const FarmerSettings = lazy(() => import('./pages/farmer/Settings'));

const NotFound = lazy(() => import('./pages/NotFound'));

export default function App() {
  return (
    <Suspense
      fallback={
        <div className="flex items-center justify-center min-h-screen">
          <Spinner size="lg" />
        </div>
      }
    >
      <Routes>
        {/* ── Public Routes ─────────────────── */}
        <Route element={<ConsumerLayout />}>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/shop" element={<Shop />} />
          <Route path="/shop/:category" element={<Shop />} />
          <Route path="/product/:slug" element={<ProductDetail />} />
          <Route path="/farm/:id" element={<FarmProfile />} />
        </Route>

        {/* ── Consumer Protected Routes ─────── */}
        <Route
          element={
            <ProtectedRoute>
              <ConsumerLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/cart" element={<Cart />} />
          <Route path="/checkout" element={<Checkout />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/orders/:id" element={<OrderDetail />} />
          <Route path="/profile" element={<Profile />} />
        </Route>

        {/* ── Farmer Protected Routes ──────── */}
        <Route
          element={
            <RoleRoute role="FARMER">
              <FarmerLayout />
            </RoleRoute>
          }
        >
          <Route path="/farmer/dashboard" element={<Dashboard />} />
          <Route path="/farmer/products" element={<FarmerProducts />} />
          <Route path="/farmer/products/new" element={<AddProduct />} />
          <Route path="/farmer/products/:id/edit" element={<EditProduct />} />
          <Route path="/farmer/orders" element={<FarmerOrders />} />
          <Route path="/farmer/settings" element={<FarmerSettings />} />
        </Route>

        <Route path="*" element={<NotFound />} />
      </Routes>
    </Suspense>
  );
}