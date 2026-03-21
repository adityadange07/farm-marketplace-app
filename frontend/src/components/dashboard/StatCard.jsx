import { FiTrendingUp, FiTrendingDown } from 'react-icons/fi';

const colorMap = {
  green: 'bg-green-100 text-green-600',
  yellow: 'bg-yellow-100 text-yellow-600',
  blue: 'bg-blue-100 text-blue-600',
  purple: 'bg-purple-100 text-purple-600',
  red: 'bg-red-100 text-red-600',
};

export default function StatCard({
  title,
  value,
  change,
  subtitle,
  icon,
  color = 'green',
  urgent = false,
}) {
  return (
    <div
      className={`bg-white rounded-xl shadow-sm p-6 ${
        urgent ? 'ring-2 ring-yellow-400' : ''
      }`}
    >
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-500">{title}</p>
        <div className={`p-2 rounded-lg ${colorMap[color]}`}>
          {icon}
        </div>
      </div>
      <p className="text-2xl font-bold text-gray-900 mt-2">{value}</p>

      <div className="flex items-center gap-2 mt-2">
        {change !== undefined && change !== null && (
          <span
            className={`flex items-center gap-0.5 text-sm font-medium ${
              change >= 0 ? 'text-green-600' : 'text-red-500'
            }`}
          >
            {change >= 0 ? (
              <FiTrendingUp className="w-3 h-3" />
            ) : (
              <FiTrendingDown className="w-3 h-3" />
            )}
            {Math.abs(change).toFixed(1)}%
          </span>
        )}
        {subtitle && (
          <span className="text-xs text-gray-400">{subtitle}</span>
        )}
      </div>
    </div>
  );
}