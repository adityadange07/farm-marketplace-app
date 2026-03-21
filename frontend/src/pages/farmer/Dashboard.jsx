import { useQuery } from '@tanstack/react-query';
import {
  FiDollarSign, FiClock, FiPackage,
  FiTrendingUp, FiTrendingDown, FiAlertTriangle,
  FiShoppingBag, FiCheckCircle,
} from 'react-icons/fi';
import { Link } from 'react-router-dom';
import api from '../../api/axios';
import StatCard from '../../components/dashboard/StatCard';
import RevenueChart from '../../components/dashboard/RevenueChart';
import OrderCard from '../../components/order/OrderCard';
import Spinner from '../../components/ui/Spinner';
import { formatCurrency } from '../../utils/helpers';

export default function Dashboard() {
  const { data: stats, isLoading } = useQuery({
    queryKey: ['farmer', 'dashboard'],
    queryFn: () =>
      api.get('/farmer/dashboard/stats').then((r) => r.data.data),
    refetchInterval: 60000, // Refresh every minute
  });

  const { data: pendingOrders } = useQuery({
    queryKey: ['farmer', 'orders', 'pending'],
    queryFn: () =>
      api
        .get('/farmer/orders', { params: { status: 'PENDING', size: 5 } })
        .then((r) => r.data.data),
  });

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-96">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">
          Good Morning, {stats?.farmerName} 🌱
        </h1>
        <p className="text-gray-500 mt-1">
          Here's your farm at a glance
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Today's Revenue"
          value={formatCurrency(stats?.todayRevenue)}
          change={stats?.revenueChange}
          icon={<FiDollarSign />}
          color="green"
        />
        <StatCard
          title="Pending Orders"
          value={stats?.pendingOrders || 0}
          subtitle={stats?.todayOrders > 0
            ? `${stats.todayOrders} today`
            : undefined}
          icon={<FiClock />}
          color="yellow"
          urgent={stats?.pendingOrders > 5}
        />
        <StatCard
          title="Active Products"
          value={stats?.activeProducts || 0}
          subtitle={
            (stats?.lowStockProducts > 0
              ? `${stats.lowStockProducts} low stock`
              : '') +
            (stats?.outOfStockProducts > 0
              ? ` · ${stats.outOfStockProducts} out`
              : '')
          }
          icon={<FiPackage />}
          color="blue"
        />
        <StatCard
          title="This Month"
          value={formatCurrency(stats?.monthlyRevenue)}
          change={stats?.monthlyChange}
          icon={<FiTrendingUp />}
          color="purple"
        />
      </div>

      {/* Alerts */}
      {stats?.lowStockProducts > 0 && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-xl
                        p-4 flex items-center gap-3">
          <FiAlertTriangle className="w-5 h-5 text-yellow-500 flex-shrink-0" />
          <span className="text-yellow-700 text-sm">
            <strong>{stats.lowStockProducts}</strong> products running low.
            {stats.outOfStockProducts > 0 && (
              <> <strong>{stats.outOfStockProducts}</strong> out of stock.</>
            )}
            <Link to="/farmer/products?filter=low_stock"
                  className="underline ml-1">
              View products →
            </Link>
          </span>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Revenue Chart */}
        <div className="lg:col-span-2 bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-semibold mb-4">Revenue — Last 30 Days</h2>
          <RevenueChart data={stats?.revenueChart || []} />
        </div>

        {/* Top Products */}
        <div className="bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-semibold mb-4">Top Products</h2>
          <div className="space-y-4">
            {stats?.topProducts?.length > 0 ? (
              stats.topProducts.map((p, i) => (
                <div key={p.id || i} className="flex items-center gap-3">
                  <span className="text-sm font-bold text-gray-300 w-6">
                    #{i + 1}
                  </span>
                  {p.image && (
                    <img
                      src={p.image}
                      alt={p.name}
                      className="w-10 h-10 rounded-lg object-cover"
                    />
                  )}
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-sm truncate">{p.name}</p>
                    <p className="text-xs text-gray-400">
                      {p.totalSold} sold · ⭐ {Number(p.avgRating).toFixed(1)}
                    </p>
                  </div>
                  <span className="font-semibold text-green-600 text-sm">
                    {formatCurrency(p.revenue)}
                  </span>
                </div>
              ))
            ) : (
              <p className="text-gray-400 text-center text-sm py-8">
                No sales yet. Add products to get started!
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Pending Orders */}
      <div className="bg-white rounded-xl shadow-sm p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold">
            Pending Orders
            {stats?.pendingOrders > 0 && (
              <span className="ml-2 px-2 py-0.5 bg-yellow-100 text-yellow-700
                               rounded-full text-xs">
                {stats.pendingOrders}
              </span>
            )}
          </h2>
          <Link to="/farmer/orders"
                className="text-green-600 text-sm hover:underline">
            View All →
          </Link>
        </div>
        <div className="space-y-3">
          {pendingOrders?.length > 0 ? (
            pendingOrders.map((order) => (
              <OrderCard key={order.id} order={order} role="farmer" />
            ))
          ) : (
            <p className="text-gray-400 text-center py-8">
              No pending orders 🎉
            </p>
          )}
        </div>
      </div>
    </div>
  );
}