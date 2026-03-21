import { FiStar } from 'react-icons/fi';

export default function RatingStars({ rating = 0, size = 'md', count }) {
  const stars = [1, 2, 3, 4, 5];
  const iconSize = size === 'sm' ? 'w-3 h-3' : 'w-4 h-4';

  return (
    <div className="flex items-center gap-0.5">
      {stars.map((star) => (
        <FiStar
          key={star}
          className={`${iconSize} ${
            star <= Math.round(rating)
              ? 'text-yellow-400 fill-yellow-400'
              : 'text-gray-200'
          }`}
        />
      ))}
      {count !== undefined && (
        <span className="ml-1 text-xs text-gray-400">({count})</span>
      )}
    </div>
  );
}