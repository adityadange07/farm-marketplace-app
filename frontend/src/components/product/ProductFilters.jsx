import { useQuery } from '@tanstack/react-query';
import { FiChevronDown } from 'react-icons/fi';
import { GiLeaf } from 'react-icons/gi';
import api from '../../api/axios';

const PRICE_RANGES = [
  { label: 'Under $5', min: 0, max: 5 },
  { label: '$5 - $10', min: 5, max: 10 },
  { label: '$10 - $25', min: 10, max: 25 },
  { label: '$25 - $50', min: 25, max: 50 },
  { label: '$50+', min: 50, max: '' },
];

export default function ProductFilters({ filters, onChange, onClose }) {
  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: () => api.get('/categories').then((r) => r.data.data),
    staleTime: 30 * 60 * 1000,
  });

  return (
    <div className="space-y-6">
      {/* Categories */}
      <div>
        <h4 className="font-semibold text-gray-900 mb-3">Category</h4>
        <div className="space-y-2">
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="radio"
              name="category"
              checked={!filters.category}
              onChange={() => onChange('category', '')}
              className="text-green-600"
            />
            <span className="text-sm text-gray-600">All Categories</span>
          </label>
          {categories?.map((cat) => (
            <label
              key={cat.id}
              className="flex items-center gap-2 cursor-pointer"
            >
              <input
                type="radio"
                name="category"
                checked={filters.category === cat.slug}
                onChange={() => onChange('category', cat.slug)}
                className="text-green-600"
              />
              <span className="text-sm text-gray-600">{cat.name}</span>
            </label>
          ))}
        </div>
      </div>

      {/* Price Range */}
      <div>
        <h4 className="font-semibold text-gray-900 mb-3">Price Range</h4>
        <div className="space-y-2">
          {PRICE_RANGES.map((range) => (
            <label
              key={range.label}
              className="flex items-center gap-2 cursor-pointer"
            >
              <input
                type="radio"
                name="price"
                checked={
                  filters.minPrice === String(range.min) &&
                  filters.maxPrice === String(range.max)
                }
                onChange={() => {
                  onChange('minPrice', String(range.min));
                  onChange('maxPrice', String(range.max));
                }}
                className="text-green-600"
              />
              <span className="text-sm text-gray-600">{range.label}</span>
            </label>
          ))}
        </div>

        {/* Custom price */}
        <div className="mt-3 flex items-center gap-2">
          <input
            type="number"
            placeholder="Min"
            value={filters.minPrice}
            onChange={(e) => onChange('minPrice', e.target.value)}
            className="w-20 border rounded px-2 py-1 text-sm"
          />
          <span className="text-gray-400">—</span>
          <input
            type="number"
            placeholder="Max"
            value={filters.maxPrice}
            onChange={(e) => onChange('maxPrice', e.target.value)}
            className="w-20 border rounded px-2 py-1 text-sm"
          />
        </div>
      </div>

      {/* Organic */}
      <div>
        <h4 className="font-semibold text-gray-900 mb-3">Preferences</h4>
        <label className="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={filters.isOrganic}
            onChange={(e) => onChange('isOrganic', e.target.checked)}
            className="w-4 h-4 text-green-600 rounded"
          />
          <GiLeaf className="text-green-500" />
          <span className="text-sm text-gray-600">Organic Only</span>
        </label>
      </div>

      {/* Distance */}
      <div>
        <h4 className="font-semibold text-gray-900 mb-3">
          Distance: {filters.radiusKm}km
        </h4>
        <input
          type="range"
          min="5"
          max="100"
          step="5"
          value={filters.radiusKm}
          onChange={(e) => onChange('radiusKm', parseInt(e.target.value))}
          className="w-full accent-green-600"
        />
        <div className="flex justify-between text-xs text-gray-400 mt-1">
          <span>5km</span>
          <span>100km</span>
        </div>
      </div>
    </div>
  );
}