import {
  AreaChart, Area, XAxis, YAxis,
  CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar,
} from 'recharts';
import { useState } from 'react';
import { formatCurrency } from '../../utils/helpers';

export default function RevenueChart({ data = [] }) {
  const [chartType, setChartType] = useState('area'); // 'area' | 'bar'

  // Use provided data or empty
  const chartData = data.length > 0
    ? data.map((d) => ({
      ...d,
      revenue: Number(d.revenue) || 0,
      orders: Number(d.orders) || 0,
    }))
    : [];

  if (chartData.length === 0) {
    return (
      <div className="flex items-center justify-center h-[300px]
                      text-gray-400 text-sm">
        No revenue data yet. Deliver orders to see your chart!
      </div>
    );
  }

  const CustomTooltip = ({ active, payload, label }) => {
    if (!active || !payload?.length) return null;
    return (
      <div className="bg-white shadow-lg rounded-xl p-3 border text-sm">
        <p className="font-medium text-gray-700">{label}</p>
        <p className="text-green-600 font-semibold mt-1">
          {formatCurrency(payload[0]?.value)}
        </p>
        {payload[0]?.payload?.orders !== undefined && (
          <p className="text-gray-400 text-xs mt-0.5">
            {payload[0].payload.orders} order(s)
          </p>
        )}
      </div>
    );
  };

  return (
    <div>
      {/* Chart Type Toggle */}
      <div className="flex justify-end mb-2">
        <div className="flex border rounded-lg overflow-hidden text-xs">
          <button
            onClick={() => setChartType('area')}
            className={`px-3 py-1 ${chartType === 'area'
                ? 'bg-green-50 text-green-600'
                : 'text-gray-400'
              }`}
          >
            Area
          </button>
          <button
            onClick={() => setChartType('bar')}
            className={`px-3 py-1 ${chartType === 'bar'
                ? 'bg-green-50 text-green-600'
                : 'text-gray-400'
              }`}
          >
            Bar
          </button>
        </div>
      </div>

      <ResponsiveContainer width="100%" height={300}>
        {chartType === 'area' ? (
          <AreaChart data={chartData}>
            <defs>
              <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#16a34a" stopOpacity={0.15} />
                <stop offset="95%" stopColor="#16a34a" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
            <XAxis
              dataKey="name"
              tick={{ fontSize: 11, fill: '#9ca3af' }}
              axisLine={false}
              tickLine={false}
              interval={Math.floor(chartData.length / 7)}
            />
            <YAxis
              tick={{ fontSize: 11, fill: '#9ca3af' }}
              axisLine={false}
              tickLine={false}
              tickFormatter={(val) => `$${val}`}
            />
            <Tooltip content={<CustomTooltip />} />
            <Area
              type="monotone"
              dataKey="revenue"
              stroke="#16a34a"
              strokeWidth={2}
              fillOpacity={1}
              fill="url(#colorRevenue)"
            />
          </AreaChart>
        ) : (
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
            <XAxis
              dataKey="name"
              tick={{ fontSize: 11, fill: '#9ca3af' }}
              axisLine={false}
              tickLine={false}
              interval={Math.floor(chartData.length / 7)}
            />
            <YAxis
              tick={{ fontSize: 11, fill: '#9ca3af' }}
              axisLine={false}
              tickLine={false}
              tickFormatter={(val) => `$${val}`}
            />
            <Tooltip content={<CustomTooltip />} />
            <Bar
              dataKey="revenue"
              fill="#16a34a"
              radius={[4, 4, 0, 0]}
              maxBarSize={30}
            />
          </BarChart>
        )}
      </ResponsiveContainer>
    </div>
  );
}