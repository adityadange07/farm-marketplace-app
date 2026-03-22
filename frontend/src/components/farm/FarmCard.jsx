import { Link } from 'react-router-dom';
import { FiMapPin, FiStar } from 'react-icons/fi';
import { FaLeaf } from 'react-icons/fa';
import Badge from '../ui/Badge';

export default function FarmCard({ farm }) {
  return (
    <Link
      to={`/farm/${farm.id}`}
      className="block bg-white rounded-2xl shadow-sm border overflow-hidden
                 hover:shadow-md transition-all"
    >
      <div className="h-40 bg-gradient-to-r from-green-400 to-green-600">
        {farm.bannerImage && (
          <img
            src={farm.bannerImage}
            alt={farm.farmName}
            className="w-full h-full object-cover"
          />
        )}
      </div>
      <div className="p-4">
        <div className="flex items-center gap-2">
          <h3 className="font-semibold text-gray-900">{farm.farmName}</h3>
          {farm.isOrganic && (
            <FaLeaf className="text-green-500 w-4 h-4" />
          )}
        </div>
        <p className="flex items-center gap-1 text-sm text-gray-500 mt-1">
          <FiMapPin className="w-3 h-3" />
          {farm.city}, {farm.state}
          {farm.distanceKm && (
            <span className="ml-1">({farm.distanceKm.toFixed(1)} km)</span>
          )}
        </p>
        <div className="flex items-center gap-2 mt-2">
          <div className="flex items-center gap-0.5">
            <FiStar className="w-3 h-3 text-yellow-400 fill-yellow-400" />
            <span className="text-sm font-medium">
              {farm.rating?.toFixed(1) || '0.0'}
            </span>
          </div>
          <span className="text-xs text-gray-400">
            ({farm.totalReviews || 0} reviews)
          </span>
        </div>
      </div>
    </Link>
  );
}