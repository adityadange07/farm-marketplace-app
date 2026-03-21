import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { FiPackage, FiChevronRight, FiClock } from 'react-icons/fi';
import orderApi from '../../api/orderApi';
import OrderStatusBadge from '../../components/order/OrderStatusBadge';
import Spinner from '../../components/ui/Spinner';

const STATUS_TABS = [
  { value: '', label: 'All Orders' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'CONFIRMED', label: 'In Progress' },
  { value: 'DELIVERED', label: 'Delivered' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

export default function Orders() {
  const [activeTab, setActiveTab] = useState('');
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['orders', activeTab, page],
    queryFn: () =>
      orderApi.getMyOrders({
        status: activeTab || undefined,
        page,
        size: 10,
      }),
  });

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">My Orders</h1>

      {/* Tabs */}
      <div className="flex gap-2 overflow-x-auto mb-6 pb-2">
        {STATUS_TABS.map((tab) => (
          <button
            key={tab.value}
            onClick={() => {
              setActiveTab(tab.value);
              setPage(0);
            }}
            className={`px-4 py-2 rounded-full text-sm font-medium
                         whitespace-nowrap transition ${
              activeTab === tab.value
                ? 'bg-green-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Orders List */}
      {isLoading ? (
        <div className="flex justify-center py-20">
          <Spinner size="lg" />
        </div>
      ) : data?.data?.length === 0 ? (
        <div className="text-center py-20">
          <FiPackage className="w-16 h-16 text-gray-200 mx-auto" />
          <h3 className="text-lg font-semibold text-gray-400 mt-4">
            No orders found
          </h3>
          <Link
            to="/shop"
            className="mt-4 inline-block text-green-600 hover:underline"
          >
            Start shopping →
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {data?.data?.map((order) => (
            <Link
              key={order.id}
              to={`/orders/${order.id}`}
              className="block bg-white rounded-xl shadow-sm border p-6
                         hover:shadow-md transition"
            >
              <div className="flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-3">
                    <span className="font-semibold text-gray-900">
                      {order.orderNumber}
                    </span>
                    <OrderStatusBadge status={order.status} />
                  </div>
                  <p className="text-sm text-gray-500 mt-1">
                    {order.farm?.farmName}
                  </p>
                </div>
                <FiChevronRight className="text-gray-400" />
              </div>

              {/* Items preview */}
              <div className="mt-4 flex gap-3">
                {order.items?.slice(0, 4).map((item, i) => (
                  <div
                    key={i}
                    className="w-14 h-14 rounded-lg bg-gray-100 overflow-hidden"
                  >
                    <img
                      src={item.productImage || '/placeholder-product.jpg'}
                      alt={item.productName}
                      className="w-full h-full object-cover"
                    />
                  </div>
                ))}
                {order.items?.length > 4 && (
                  <div className="w-14 h-14 rounded-lg bg-gray-100
                                  flex items-center justify-center text-sm
                                  text-gray-500">
                    +{order.items.length - 4}
                  </div>
                )}
              </div>

              <div className="mt-4 flex items-center justify-between text-sm">
                <div className="flex items-center gap-1 text-gray-400">
                  <FiClock className="w-3 h-3" />
                  {new Date(order.placedAt).toLocaleDateString('en-US', {
                    month: 'short',
                    day: 'numeric',
                    year: 'numeric',
                  })}
                </div>
                <span className="font-bold text-green-600">
                  ${order.total.toFixed(2)}
                </span>
              </div>
            </Link>
          ))}

          {/* Pagination */}
          {data?.totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-6">
              <button
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                className="px-4 py-2 border rounded-lg disabled:opacity-50"
              >
                Previous
              </button>
              <span className="px-4 py-2">
                {page + 1} / {data.totalPages}
              </span>
              <button
                onClick={() => setPage(page + 1)}
                disabled={!data.hasMore}
                className="px-4 py-2 border rounded-lg disabled:opacity-50"
              >
                Next
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}