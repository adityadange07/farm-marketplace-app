import { FiCheck, FiClock, FiPackage, FiTruck, FiX } from 'react-icons/fi';

const STATUS_STEPS = [
  { key: 'PENDING', label: 'Order Placed', icon: FiClock },
  { key: 'CONFIRMED', label: 'Confirmed', icon: FiCheck },
  { key: 'PROCESSING', label: 'Preparing', icon: FiPackage },
  { key: 'OUT_FOR_DELIVERY', label: 'On the Way', icon: FiTruck },
  { key: 'DELIVERED', label: 'Delivered', icon: FiCheck },
];

const STATUS_ORDER = [
  'PENDING', 'CONFIRMED', 'PROCESSING',
  'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY', 'DELIVERED',
];

export default function OrderTimeline({ statusHistory = [], currentStatus }) {
  const currentIndex = STATUS_ORDER.indexOf(currentStatus);
  const isCancelled = currentStatus === 'CANCELLED';

  return (
    <div className="space-y-0">
      {STATUS_STEPS.map((step, index) => {
        const isCompleted = currentIndex >= STATUS_ORDER.indexOf(step.key);
        const isCurrent = currentStatus === step.key;
        const Icon = step.icon;

        // Find timestamp from history
        const historyEntry = statusHistory?.find(
          (h) => h.status === step.key
        );

        return (
          <div key={step.key} className="flex gap-4">
            {/* Line + Circle */}
            <div className="flex flex-col items-center">
              <div
                className={`w-8 h-8 rounded-full flex items-center
                            justify-center flex-shrink-0 ${
                  isCompleted
                    ? 'bg-green-600 text-white'
                    : isCurrent
                    ? 'bg-green-100 text-green-600 ring-2 ring-green-600'
                    : 'bg-gray-100 text-gray-400'
                }`}
              >
                <Icon className="w-4 h-4" />
              </div>
              {index < STATUS_STEPS.length - 1 && (
                <div
                  className={`w-0.5 h-12 ${
                    isCompleted ? 'bg-green-600' : 'bg-gray-200'
                  }`}
                />
              )}
            </div>

            {/* Content */}
            <div className="pb-8">
              <p
                className={`font-medium ${
                  isCompleted || isCurrent
                    ? 'text-gray-900'
                    : 'text-gray-400'
                }`}
              >
                {step.label}
              </p>
              {historyEntry && (
                <p className="text-xs text-gray-400 mt-0.5">
                  {new Date(historyEntry.timestamp).toLocaleString()}
                </p>
              )}
              {historyEntry?.note && (
                <p className="text-sm text-gray-500 mt-1">
                  {historyEntry.note}
                </p>
              )}
            </div>
          </div>
        );
      })}

      {/* Cancelled status */}
      {isCancelled && (
        <div className="flex gap-4">
          <div className="flex flex-col items-center">
            <div className="w-8 h-8 rounded-full flex items-center
                            justify-center bg-red-600 text-white">
              <FiX className="w-4 h-4" />
            </div>
          </div>
          <div>
            <p className="font-medium text-red-600">Cancelled</p>
            {statusHistory?.find((h) => h.status === 'CANCELLED') && (
              <p className="text-xs text-gray-400 mt-0.5">
                {new Date(
                  statusHistory.find((h) => h.status === 'CANCELLED').timestamp
                ).toLocaleString()}
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}