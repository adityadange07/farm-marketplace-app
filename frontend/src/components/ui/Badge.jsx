import { clsx } from 'clsx';

const colorMap = {
  green: 'bg-green-100 text-green-700',
  red: 'bg-red-100 text-red-700',
  yellow: 'bg-yellow-100 text-yellow-700',
  blue: 'bg-blue-100 text-blue-700',
  orange: 'bg-orange-100 text-orange-700',
  gray: 'bg-gray-100 text-gray-700',
  purple: 'bg-purple-100 text-purple-700',
};

export default function Badge({
  children,
  color = 'gray',
  size = 'md',
  className = '',
}) {
  return (
    <span
      className={clsx(
        'inline-flex items-center rounded-full font-medium',
        colorMap[color],
        size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-3 py-1 text-sm',
        className
      )}
    >
      {children}
    </span>
  );
}