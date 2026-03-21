import { useParams, useLocation } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  FiMapPin, FiClock, FiPhone, FiMessageSquare,
  FiPackage, FiCheck, FiTruck,
} from 'react-icons/fi';
import orderApi from '../../api/orderApi';
import OrderStatusBadge from '../../components/order/OrderStatusBadge';
import OrderTimeline from '../../components/order/OrderTimeline';
import Spinner from '../../components/ui/Spinner';
import Button from '../../components/ui/Button';
import Confetti from '../../components/ui/Confetti';

export default function OrderDetail() {
  const { id } = useParams();
  const location = useLocation();
  const justPlaced = location.state?.justPlaced;

  const { data: order, isLoading } = useQuery({
    queryKey: ['order', id],
    queryFn: () => orderApi.getById(id),
  });

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!order) {
    return <div className="text-center py-20">Order not found</div>;
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {justPlaced && <Confetti />}

      {/* Success Banner */}
      {justPlaced && (
        <div className="bg-green-50 border border-green-200 rounded-xl p-6
                        text-center mb-8">
          <div className="text-4xl mb-2">🎉</div>
          <h2 className="text-xl font-bold text-green-700">
            Order Placed Successfully!
          </h2>
          <p className="text-green-600 mt-1">
            The farmer will confirm your order shortly
          </p>
        </div>
      )}

      {/* Order Header */}
      <div className="bg-white rounded-xl shadow-sm p-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-gray-900">
              Order {order.orderNumber}
            </h1>
            <p className="text-sm text-gray-500 mt-1">
              Placed on{' '}
              {new Date(order.placedAt).toLocaleDateString('en-US', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </p>
          </div>
          <OrderStatusBadge status={order.status} size="lg" />
        </div>

        {/* Farm Info */}
        <div className="mt-4 p-4 bg-gray-50 rounded-lg flex items-center
                        justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-green-100 rounded-full flex
                            items-center justify-center">
              <span className="text-green-600 font-bold">
                {order.farm?.farmName?.[0]}
              </span>
            </div>
            <div>
              <p className="font-medium">{order.farm?.farmName}</p>
              <p className="text-sm text-gray-500 flex items-center gap-1">
                <FiMapPin className="w-3 h-3" />
                {order.farm?.city}, {order.farm?.state}
              </p>
            </div>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" size="sm">
              <FiPhone className="mr-1" /> Call
            </Button>
            <Button variant="outline" size="sm">
              <FiMessageSquare className="mr-1" /> Chat
            </Button>
          </div>
        </div>
      </div>

      <div className="grid lg:grid-cols-3 gap-6 mt-6">
        {/* Left: Timeline + Items */}
        <div className="lg:col-span-2 space-y-6">
          {/* Timeline */}
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h3 className="font-semibold mb-4">Order Status</h3>
            <OrderTimeline
              statusHistory={order.statusHistory}
              currentStatus={order.status}
            />
          </div>

          {/* Items */}
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h3 className="font-semibold mb-4">
              Items ({order.items?.length})
            </h3>
            <div className="divide-y">
              {order.items?.map((item) => (
                <div key={item.id} className="flex items-center gap-4 py-4">
                  <img
                    src={item.productImage || '/placeholder-product.jpg'}
                    alt={item.productName}
                    className="w-16 h-16 rounded-lg object-cover"
                  />
                  <div className="flex-1">
                    <p className="font-medium">{item.productName}</p>
                    <p className="text-sm text-gray-500">
                      {item.quantity} × ${item.unitPrice.toFixed(2)} / {item.unit}
                    </p>
                  </div>
                  <p className="font-semibold">
                    ${item.totalPrice.toFixed(2)}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right: Summary */}
        <div className="space-y-6">
          {/* Price Breakdown */}
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h3 className="font-semibold mb-4">Order Summary</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Subtotal</span>
                <span>${order.subtotal.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Delivery</span>
                <span>
                  {parseFloat(order.deliveryFee) === 0
                    ? 'Free'
                    : `$${order.deliveryFee.toFixed(2)}`}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Service Fee</span>
                <span>${order.serviceFee.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Tax</span>
                <span>${order.tax.toFixed(2)}</span>
              </div>
              {parseFloat(order.discount) > 0 && (
                <div className="flex justify-between text-green-600">
                  <span>Discount</span>
                  <span>-${order.discount.toFixed(2)}</span>
                </div>
              )}
              <div className="border-t pt-2 flex justify-between font-bold text-lg">
                <span>Total</span>
                <span>${order.total.toFixed(2)}</span>
              </div>
            </div>
          </div>

          {/* Delivery Details */}
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h3 className="font-semibold mb-4">Delivery Details</h3>
            <div className="space-y-3 text-sm">
              <div className="flex items-start gap-2">
                <FiTruck className="w-4 h-4 text-gray-400 mt-0.5" />
                <span className="capitalize">
                  {order.deliveryType.toLowerCase()}
                </span>
              </div>
              {order.scheduledDate && (
                <div className="flex items-start gap-2">
                  <FiClock className="w-4 h-4 text-gray-400 mt-0.5" />
                  <div>
                    <p>
                      {new Date(
                        order.scheduledDate + 'T12:00:00'
                      ).toLocaleDateString('en-US', {
                        weekday: 'long',
                        month: 'long',
                        day: 'numeric',
                      })}
                    </p>
                    <p className="text-gray-500">
                      {order.scheduledTimeSlot}
                    </p>
                  </div>
                </div>
              )}
              {order.deliveryAddress && (
                <div className="flex items-start gap-2">
                  <FiMapPin className="w-4 h-4 text-gray-400 mt-0.5" />
                  <div>
                    <p>{order.deliveryAddress.line1}</p>
                    {order.deliveryAddress.line2 && (
                      <p>{order.deliveryAddress.line2}</p>
                    )}
                    <p>
                      {order.deliveryAddress.city},{' '}
                      {order.deliveryAddress.state}{' '}
                      {order.deliveryAddress.zipCode}
                    </p>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Actions */}
          {order.status === 'PENDING' && (
            <Button variant="danger" className="w-full">
              Cancel Order
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}