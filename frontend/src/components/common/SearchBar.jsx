import { FiSearch, FiX } from 'react-icons/fi';
import { clsx } from 'clsx';

export default function SearchBar({
  value,
  onChange,
  placeholder = 'Search...',
  className = '',
}) {
  return (
    <div className={clsx('relative', className)}>
      <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2
                           text-gray-400 w-5 h-5" />
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="w-full pl-10 pr-10 py-2.5 border rounded-xl
                   text-sm focus:ring-2 focus:ring-green-500
                   focus:border-green-500"
      />
      {value && (
        <button
          onClick={() => onChange('')}
          className="absolute right-3 top-1/2 -translate-y-1/2
                     text-gray-400 hover:text-gray-600"
        >
          <FiX className="w-4 h-4" />
        </button>
      )}
    </div>
  );
}