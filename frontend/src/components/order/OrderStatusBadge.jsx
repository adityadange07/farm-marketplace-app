const STATUS_CONFIG = {
  PENDING: {
    label: 'Pending',
    color: 'bg-yellow-100 text-yellow-700',
    dot: 'bg-yellow-500',
  },
  CONFIRMED: {
    label: 'Confirmed',
    color: 'bg-blue-100 text-blue-700',
    dot: 'bg-blue-500',
  },
  PROCESSING: {
    label: 'Preparing',
    color: 'bg-indigo-100 text-indigo-700',
    dot: 'bg-indigo-500',
  },
  READY_FOR_PICKUP: {
    label: 'Ready',
    color: 'bg-purple-100 text-purple-700',
    dot: 'bg-purple-500',
  },
  OUT_FOR_DELIVERY: {
    label: 'On the Way',
    color: 'bg-cyan-100 text-cyan-700',
    dot: 'bg-cyan-500',
  },
  DELIVERED: {
    label: 'Delivered',
    color: 'bg-green-100 text-green-700',
    dot: 'bg-green-500',
  },
  CANCELLED: {
    label: 'Cancelled',
    color: 'bg-red-100 text-red-700',
    dot: 'bg-red-500',
  },
  REFUNDED: {
    label: 'Refunded',
    color: 'bg-gray-100 text-gray-700',
    dot: 'bg-gray-500',
  },
};

export default function OrderStatusBadge({ status, size = 'sm' }) {
  const config = STATUS_CONFIG[status] || STATUS_CONFIG.PENDING;
  const sizeClasses =
    size === 'lg' ? 'px-4 py-1.5 text-sm' : 'px-2.5 py-0.5 text-xs';

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full
                   font-medium ${config.color} ${sizeClasses}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${config.dot}`} />
      {config.label}
    </span>
  );
}