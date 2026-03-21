import { Link } from 'react-router-dom';
import { FiShoppingCart, FiHeart, FiMapPin } from 'react-icons/fi';
import { GiLeaf } from 'react-icons/gi';
import { useCartStore } from '../../store/cartStore';
import RatingStars from '../common/RatingStar';
import Badge from '../ui/Badge';

export default function ProductCard({ product, showDistance = false }) {
  const addItem = useCartStore((s) => s.addItem);

  const primaryImage =
    product.images?.find((img) => img.isPrimary)?.url ||
    product.images?.[0]?.url ||
    '/placeholder-product.jpg';

  const discount = product.compareAtPrice
    ? Math.round(
        ((product.compareAtPrice - product.price) / product.compareAtPrice) * 100
      )
    : 0;

  return (
    <div className="group bg-white rounded-2xl shadow-sm border border-gray-100
                     overflow-hidden hover:shadow-md transition-all duration-300">
      {/* Image */}
      <div className="relative aspect-square overflow-hidden">
        <Link to={`/product/${product.slug}`}>
          <img
            src={primaryImage}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105
                       transition-transform duration-300"
          />
        </Link>

        {/* Badges */}
        <div className="absolute top-3 left-3 flex flex-col gap-1">
          {product.isOrganic && (
            <Badge color="green" size="sm">
              <GiLeaf className="mr-1" /> Organic
            </Badge>
          )}
          {discount > 0 && (
            <Badge color="red" size="sm">{discount}% OFF</Badge>
          )}
        </div>

        {/* Wishlist */}
        <button
          className="absolute top-3 right-3 p-2 bg-white/80
                     backdrop-blur-sm rounded-full hover:bg-white transition"
        >
          <FiHeart className="w-4 h-4 text-gray-600" />
        </button>

        {/* Quick Add */}
        <div className="absolute bottom-3 left-3 right-3 opacity-0
                        group-hover:opacity-100 transition-opacity">
          <button
            onClick={(e) => {
              e.preventDefault();
              addItem(product);
            }}
            disabled={product.stockQuantity === 0}
            className="w-full flex items-center justify-center gap-2
                       bg-green-600 text-white py-2.5 rounded-lg
                       hover:bg-green-700 transition font-medium text-sm
                       disabled:bg-gray-400"
          >
            <FiShoppingCart className="w-4 h-4" />
            {product.stockQuantity === 0 ? 'Out of Stock' : 'Add to Cart'}
          </button>
        </div>
      </div>

      {/* Content */}
      <div className="p-4">
        <p className="text-xs text-green-600 font-medium">
          {product.category?.name}
        </p>

        <Link to={`/product/${product.slug}`}>
          <h3 className="mt-1 font-semibold text-gray-900 line-clamp-2
                         hover:text-green-700 transition">
            {product.name}
          </h3>
        </Link>

        {product.farm && (
          <div className="flex items-center gap-1 mt-1 text-sm text-gray-500">
            <FiMapPin className="w-3 h-3" />
            <span className="truncate">{product.farm.farmName}</span>
            {showDistance && product.distanceKm && (
              <span className="text-xs ml-1">
                ({product.distanceKm.toFixed(1)} km)
              </span>
            )}
          </div>
        )}

        <div className="flex items-center gap-2 mt-2">
          <RatingStars rating={product.avgRating} size="sm" />
          <span className="text-xs text-gray-400">
            ({product.reviewCount})
          </span>
        </div>

        <div className="flex items-baseline gap-2 mt-3">
          <span className="text-lg font-bold text-gray-900">
            ${product.price.toFixed(2)}
          </span>
          <span className="text-sm text-gray-400">/ {product.unit}</span>
          {product.compareAtPrice && (
            <span className="text-sm text-gray-400 line-through">
              ${product.compareAtPrice.toFixed(2)}
            </span>
          )}
        </div>

        {product.stockQuantity > 0 &&
          product.stockQuantity <= (product.lowStockThreshold || 5) && (
            <p className="text-xs text-orange-500 mt-1">
              Only {product.stockQuantity} left
            </p>
          )}
      </div>
    </div>
  );
}