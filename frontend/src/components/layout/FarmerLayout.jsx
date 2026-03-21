import { Outlet, Link, useLocation } from 'react-router-dom';
import {
  FiGrid, FiPackage, FiShoppingBag,
  FiSettings, FiBarChart2, FiLogOut,
} from 'react-icons/fi';
import { GiWheat } from 'react-icons/gi';
import { useAuthStore } from '../../store/authStore';

const sidebarItems = [
  { path: '/farmer/dashboard', label: 'Dashboard', icon: FiGrid },
  { path: '/farmer/products', label: 'Products', icon: FiPackage },
  { path: '/farmer/orders', label: 'Orders', icon: FiShoppingBag },
  { path: '/farmer/analytics', label: 'Analytics', icon: FiBarChart2 },
  { path: '/farmer/settings', label: 'Settings', icon: FiSettings },
];

export default function FarmerLayout() {
  const location = useLocation();
  const { user, logout } = useAuthStore();

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <aside className="w-64 bg-white border-r flex flex-col">
        {/* Logo */}
        <div className="px-6 py-5 border-b">
          <Link to="/" className="flex items-center gap-2">
            <GiWheat className="w-8 h-8 text-green-600" />
            <div>
              <p className="font-bold text-green-700">FarmFresh</p>
              <p className="text-xs text-gray-400">Farmer Dashboard</p>
            </div>
          </Link>
        </div>

        {/* Nav Items */}
        <nav className="flex-1 px-3 py-4 space-y-1">
          {sidebarItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-3 px-4 py-2.5 rounded-lg
                            text-sm font-medium transition ${
                  isActive
                    ? 'bg-green-50 text-green-700'
                    : 'text-gray-600 hover:bg-gray-50'
                }`}
              >
                <item.icon
                  className={`w-5 h-5 ${
                    isActive ? 'text-green-600' : 'text-gray-400'
                  }`}
                />
                {item.label}
              </Link>
            );
          })}
        </nav>

        {/* User */}
        <div className="px-3 py-4 border-t">
          <div className="flex items-center gap-3 px-4 py-2">
            <div className="w-9 h-9 bg-green-100 rounded-full flex
                            items-center justify-center">
              <span className="text-green-600 font-bold text-sm">
                {user?.firstName?.[0]}
              </span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium truncate">
                {user?.firstName} {user?.lastName}
              </p>
              <p className="text-xs text-gray-400 truncate">{user?.email}</p>
            </div>
          </div>
          <button
            onClick={logout}
            className="flex items-center gap-3 px-4 py-2 mt-2
                       text-sm text-red-500 hover:bg-red-50
                       rounded-lg w-full transition"
          >
            <FiLogOut className="w-4 h-4" />
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-y-auto">
        <div className="p-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
}