import { Link, useNavigate } from 'react-router-dom';
import {
  FiShoppingCart, FiUser, FiSearch,
  FiMenu, FiLogOut, FiPackage,
} from 'react-icons/fi';
import { GiWheat } from 'react-icons/gi';
import { useAuthStore } from '../../store/authStore';
import { useCartStore } from '../../store/cartStore';
import { useState } from 'react';

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuthStore();
  const totalItems = useCartStore((s) => s.totalItems());
  const openCart = useCartStore((s) => s.openCart);
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="bg-white border-b sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2">
            <GiWheat className="w-8 h-8 text-green-600" />
            <span className="text-xl font-bold text-green-700">
              FarmFresh
            </span>
          </Link>

          {/* Center Nav */}
          <div className="hidden md:flex items-center gap-8">
            <Link to="/shop" className="text-gray-600 hover:text-green-600 transition">
              Shop
            </Link>
            <Link to="/shop/vegetables" className="text-gray-600 hover:text-green-600 transition">
              Vegetables
            </Link>
            <Link to="/shop/fruits" className="text-gray-600 hover:text-green-600 transition">
              Fruits
            </Link>
            <Link to="/shop/dairy-eggs" className="text-gray-600 hover:text-green-600 transition">
              Dairy
            </Link>
          </div>

          {/* Right */}
          <div className="flex items-center gap-4">
            <Link to="/shop" className="p-2 hover:bg-gray-100 rounded-full">
              <FiSearch className="w-5 h-5 text-gray-600" />
            </Link>

            {/* Cart */}
            <button
              onClick={openCart}
              className="relative p-2 hover:bg-gray-100 rounded-full"
            >
              <FiShoppingCart className="w-5 h-5 text-gray-600" />
              {totalItems > 0 && (
                <span className="absolute -top-1 -right-1 bg-green-600
                                 text-white text-xs w-5 h-5 rounded-full
                                 flex items-center justify-center">
                  {totalItems}
                </span>
              )}
            </button>

            {/* Auth */}
            {isAuthenticated ? (
              <div className="relative">
                <button
                  onClick={() => setMenuOpen(!menuOpen)}
                  className="flex items-center gap-2 p-2 hover:bg-gray-100
                             rounded-lg"
                >
                  <div className="w-8 h-8 bg-green-100 rounded-full
                                  flex items-center justify-center">
                    <span className="text-green-600 font-semibold text-sm">
                      {user?.firstName?.[0]}
                    </span>
                  </div>
                </button>

                {menuOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-white
                                  rounded-xl shadow-lg border py-2 z-50">
                    <div className="px-4 py-2 border-b">
                      <p className="font-medium text-sm">
                        {user?.firstName} {user?.lastName}
                      </p>
                      <p className="text-xs text-gray-400">{user?.role}</p>
                    </div>

                    {user?.role === 'FARMER' && (
                      <Link
                        to="/farmer/dashboard"
                        className="flex items-center gap-2 px-4 py-2
                                   hover:bg-gray-50 text-sm"
                        onClick={() => setMenuOpen(false)}
                      >
                        <FiPackage className="w-4 h-4" />
                        Farmer Dashboard
                      </Link>
                    )}

                    <Link
                      to="/orders"
                      className="flex items-center gap-2 px-4 py-2
                                 hover:bg-gray-50 text-sm"
                      onClick={() => setMenuOpen(false)}
                    >
                      <FiPackage className="w-4 h-4" />
                      My Orders
                    </Link>

                    <Link
                      to="/profile"
                      className="flex items-center gap-2 px-4 py-2
                                 hover:bg-gray-50 text-sm"
                      onClick={() => setMenuOpen(false)}
                    >
                      <FiUser className="w-4 h-4" />
                      Profile
                    </Link>

                    <button
                      onClick={handleLogout}
                      className="flex items-center gap-2 px-4 py-2
                                 hover:bg-gray-50 text-sm text-red-500 w-full"
                    >
                      <FiLogOut className="w-4 h-4" />
                      Logout
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/login"
                  className="px-4 py-2 text-sm font-medium text-gray-600
                             hover:text-green-600 transition"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 text-sm font-medium bg-green-600
                             text-white rounded-lg hover:bg-green-700
                             transition"
                >
                  Sign Up
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}