import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  FiClock, FiCheck, FiX, FiPackage,
  FiTruck, FiAlertCircle, FiFilter,
} from 'react-icons/fi';
import orderApi from '../../api/orderApi';
import OrderStatusBadge from '../../components/order/OrderStatusBadge';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import toast from 'react-hot-toast';

const STATUS_TABS = [
  { value: '', label: 'All', icon: FiFilter },
  { value: 'PENDING', label: 'Pending', icon: FiClock, color: 'text-yellow-500' },
  { value: 'CONFIRMED', label: 'Confirmed', icon: FiCheck, color: 'text-blue-500' },
  { value: 'PROCESSING', label: 'Preparing', icon: FiPackage, color: 'text-indigo-500' },
  { value: 'OUT_FOR_DELIVERY', label: 'In Transit', icon: FiTruck, color: 'text-cyan-500' },
  { value: 'DELIVERED', label: 'Delivered', icon: FiCheck, color: 'text-green-500' },
];

const NEXT_STATUS_MAP = {
  PENDING: { next: 'CONFIRMED', label: 'Confirm', icon: FiCheck, color: 'bg-blue-600' },
  CONFIRMED: { next: 'PROCESSING', label: 'Start Preparing', icon: FiPackage, color: 'bg-indigo-600' },
  PROCESSING: { next: 'READY_FOR_PICKUP', label: 'Mark Ready', icon: FiPackage, color: 'bg-purple-600' },
  READY_FOR_PICKUP: { next: 'OUT_FOR_DELIVERY', label: 'Out for Delivery', icon: FiTruck, color: 'bg-cyan-600' },
  OUT_FOR_DELIVERY: { next: 'DELIVERED', label: 'Mark Delivered', icon: FiCheck, color: 'bg-green-600' },
};

export default function FarmerOrders() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState('');
  const [page, setPage] = useState(0);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [cancelModal, setCancelModal] = useState(null);
  const [cancelReason, setCancelReason] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['farmer-orders', activeTab, page],
    queryFn: () =>
      orderApi.getFarmerOrders({
        status: activeTab || undefined,
        page,
        size: 15,
      }),
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ orderId, status, note }) =>
      orderApi.updateStatus(orderId, { status, note }),
    onSuccess: () => {
      queryClient.invalidateQueries(['farmer-orders']);
      toast.success('Order status updated');
      setSelectedOrder(null);
    },
  });

  const handleAdvanceStatus = (order) => {
    const nextAction = NEXT_STATUS_MAP[order.status];
    if (!nextAction) return;

    updateStatusMutation.mutate({
      orderId: order.id,
      status: nextAction.next,
      note: `Status updated to ${nextAction.next}`,
    });
  };

  const handleCancel = () => {
    if (!cancelReason.trim()) {
      toast.error('Please provide a reason');
      return;
    }
    updateStatusMutation.mutate({
      orderId: cancelModal.id,
      status: 'CANCELLED',
      note: cancelReason,
    });
    setCancelModal(null);
    setCancelReason('');
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Orders</h1>

      {/* Status Tabs */}
      <div className="flex gap-2 overflow-x-auto pb-2">
        {STATUS_TABS.map((tab) => (
          <button
            key={tab.value}
            onClick={() => {
              setActiveTab(tab.value);
              setPage(0);
            }}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg
                         text-sm font-medium whitespace-nowrap transition ${
              activeTab === tab.value
                ? 'bg-green-600 text-white'
                : 'bg-white border text-gray-600 hover:bg-gray-50'
            }`}
          >
            <tab.icon className={`w-4 h-4 ${
              activeTab === tab.value ? '' : tab.color || ''
            }`} />
            {tab.label}
          </button>
        ))}
      </div>

      {/* Orders Table */}
      {isLoading ? (
        <div className="flex justify-center py-20">
          <Spinner size="lg" />
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="text-left px-6 py-3 text-xs font-medium
                                 text-gray-500 uppercase">Order</th>
                  <th className="text-left px-6 py-3 text-xs font-medium
                                 text-gray-500 uppercase">Customer</th>
                  <th className="text-left px-6 py-3 text-xs font-medium
                                 text-gray-500 uppercase">Items</th>
                  <th className="text-left px-6 py-3 text-xs font-medium
                                 text-gray-500 uppercase">Total</th>
                  <th className="text-left px-6 py-3 text-xs font-medium
                                 text-gray-500 uppercase">Status</th>
                  <th className="text-left px-6 py-3 text-xs font-medium
                                 text-gray-500 uppercase">Delivery</th>
                  <th className="text-right px-6 py-3 text-xs font-medium
                                  text-gray-500 uppercase">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {data?.data?.map((order) => {
                  const nextAction = NEXT_STATUS_MAP[order.status];

                  return (
                    <tr key={order.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4">
                        <p className="font-medium text-sm">
                          {order.orderNumber}
                        </p>
                        <p className="text-xs text-gray-400">
                          {new Date(order.placedAt).toLocaleDateString()}
                        </p>
                      </td>

                      <td className="px-6 py-4">
                        <p className="text-sm font-medium">
                          {order.consumer?.firstName} {order.consumer?.lastName}
                        </p>
                        <p className="text-xs text-gray-400">
                          {order.consumer?.email}
                        </p>
                      </td>

                      <td className="px-6 py-4">
                        <p className="text-sm">
                          {order.items?.length} item(s)
                        </p>
                        <p className="text-xs text-gray-400 truncate max-w-[150px]">
                          {order.items?.map((i) => i.productName).join(', ')}
                        </p>
                      </td>

                      <td className="px-6 py-4">
                        <p className="text-sm font-bold text-green-600">
                          ${order.total.toFixed(2)}
                        </p>
                      </td>

                      <td className="px-6 py-4">
                        <OrderStatusBadge status={order.status} />
                      </td>

                      <td className="px-6 py-4">
                        <p className="text-sm capitalize">
                          {order.deliveryType.toLowerCase()}
                        </p>
                        {order.scheduledDate && (
                          <p className="text-xs text-gray-400">
                            {new Date(
                              order.scheduledDate + 'T12:00:00'
                            ).toLocaleDateString('en-US', {
                              month: 'short',
                              day: 'numeric',
                            })}
                          </p>
                        )}
                      </td>

                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          {nextAction && (
                            <button
                              onClick={() => handleAdvanceStatus(order)}
                              className={`flex items-center gap-1 px-3 py-1.5
                                          rounded-lg text-white text-xs font-medium
                                          ${nextAction.color} hover:opacity-90`}
                              disabled={updateStatusMutation.isPending}
                            >
                              <nextAction.icon className="w-3 h-3" />
                              {nextAction.label}
                            </button>
                          )}
                          {['PENDING', 'CONFIRMED', 'PROCESSING'].includes(
                            order.status
                          ) && (
                            <button
                              onClick={() => setCancelModal(order)}
                              className="p-1.5 text-red-400 hover:text-red-600
                                         hover:bg-red-50 rounded-lg"
                              title="Cancel Order"
                            >
                              <FiX className="w-4 h-4" />
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {data?.data?.length === 0 && (
            <div className="text-center py-16">
              <FiPackage className="w-12 h-12 text-gray-200 mx-auto" />
              <p className="text-gray-400 mt-4">No orders found</p>
            </div>
          )}
        </div>
      )}

      {/* Cancel Modal */}
      <Modal
        isOpen={!!cancelModal}
        onClose={() => {
          setCancelModal(null);
          setCancelReason('');
        }}
        title="Cancel Order"
      >
        <div className="space-y-4">
          <div className="flex items-center gap-3 p-3 bg-red-50 rounded-lg">
            <FiAlertCircle className="text-red-500 w-5 h-5" />
            <p className="text-sm text-red-700">
              Are you sure? This will cancel order{' '}
              <strong>{cancelModal?.orderNumber}</strong> and restore inventory.
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">
              Reason for cancellation *
            </label>
            <textarea
              rows={3}
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="e.g., Out of stock, unable to fulfill..."
              className="w-full border rounded-lg px-3 py-2 text-sm"
            />
          </div>

          <div className="flex justify-end gap-3">
            <Button
              variant="outline"
              onClick={() => {
                setCancelModal(null);
                setCancelReason('');
              }}
            >
              Keep Order
            </Button>
            <Button
              variant="danger"
              onClick={handleCancel}
              loading={updateStatusMutation.isPending}
            >
              Cancel Order
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}