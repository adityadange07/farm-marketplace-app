import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  FiMinus, FiPlus, FiShoppingCart, FiHeart,
  FiMapPin, FiTruck, FiStar, FiShare2,
} from 'react-icons/fi';
import { GiLeaf } from 'react-icons/gi';
import { useProduct } from '../../hooks/useProducts';
import { useCartStore } from '../../store/cartStore';
import RatingStars from '../../components/common/RatingStars';
import Badge from '../../components/ui/Badge';
import Spinner from '../../components/ui/Spinner';

export default function ProductDetail() {
  const { slug } = useParams();
  const { data: product, isLoading, error } = useProduct(slug);
  const addItem = useCartStore((s) => s.addItem);
  const [quantity, setQuantity] = useState(1);
  const [selectedImage, setSelectedImage] = useState(0);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <Spinner size="lg" />
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="text-center py-20">
        <h2 className="text-2xl font-bold text-gray-400">Product Not Found</h2>
      </div>
    );
  }

  const primaryImage = product.images?.[selectedImage]?.url
    || '/placeholder-product.jpg';

  const handleAddToCart = () => {
    addItem(product, quantity);
    setQuantity(1);
  };

  const discount = product.compareAtPrice
    ? Math.round(
        ((product.compareAtPrice - product.price) / product.compareAtPrice) * 100
      )
    : 0;

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Breadcrumb */}
      <nav className="text-sm text-gray-500 mb-6">
        <Link to="/" className="hover:text-green-600">Home</Link>
        <span className="mx-2">/</span>
        <Link to="/shop" className="hover:text-green-600">Shop</Link>
        <span className="mx-2">/</span>
        {product.category && (
          <>
            <Link
              to={`/shop/${product.category.slug}`}
              className="hover:text-green-600"
            >
              {product.category.name}
            </Link>
            <span className="mx-2">/</span>
          </>
        )}
        <span className="text-gray-900">{product.name}</span>
      </nav>

      <div className="grid md:grid-cols-2 gap-10">
        {/* ── Images ─────────────────── */}
        <div>
          <div className="aspect-square rounded-2xl overflow-hidden bg-gray-100">
            <img
              src={primaryImage}
              alt={product.name}
              className="w-full h-full object-cover"
            />
          </div>
          {product.images?.length > 1 && (
            <div className="flex gap-3 mt-4">
              {product.images.map((img, idx) => (
                <button
                  key={idx}
                  onClick={() => setSelectedImage(idx)}
                  className={`w-20 h-20 rounded-lg overflow-hidden border-2
                    ${idx === selectedImage
                      ? 'border-green-500'
                      : 'border-transparent'}`}
                >
                  <img
                    src={img.url}
                    alt=""
                    className="w-full h-full object-cover"
                  />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* ── Details ────────────────── */}
        <div>
          {/* Badges */}
          <div className="flex gap-2 mb-3">
            {product.isOrganic && (
              <Badge color="green" icon={<GiLeaf />}>Organic</Badge>
            )}
            {product.isSeasonal && (
              <Badge color="orange">In Season</Badge>
            )}
            {discount > 0 && (
              <Badge color="red">{discount}% OFF</Badge>
            )}
          </div>

          <h1 className="text-3xl font-bold text-gray-900">{product.name}</h1>

          {/* Farm */}
          {product.farm && (
            <Link
              to={`/farm/${product.farm.id}`}
              className="flex items-center gap-2 mt-2 text-gray-500
                         hover:text-green-600 transition"
            >
              <FiMapPin className="w-4 h-4" />
              <span>{product.farm.farmName}</span>
            </Link>
          )}

          {/* Rating */}
          <div className="flex items-center gap-3 mt-3">
            <RatingStars rating={product.avgRating} />
            <span className="text-sm text-gray-400">
              ({product.reviewCount} reviews)
            </span>
            <span className="text-sm text-gray-400">
              · {product.totalSold} sold
            </span>
          </div>

          {/* Price */}
          <div className="mt-6 flex items-baseline gap-3">
            <span className="text-4xl font-bold text-green-600">
              ${product.price.toFixed(2)}
            </span>
            <span className="text-lg text-gray-400">/ {product.unit}</span>
            {product.compareAtPrice && (
              <span className="text-xl text-gray-400 line-through">
                ${product.compareAtPrice.toFixed(2)}
              </span>
            )}
          </div>

          {/* Description */}
          <p className="mt-6 text-gray-600 leading-relaxed">
            {product.description}
          </p>

          {/* Growing Method */}
          {product.growingMethod && (
            <div className="mt-4 p-3 bg-green-50 rounded-lg">
              <span className="text-sm font-medium text-green-700">
                Growing Method: {product.growingMethod}
              </span>
            </div>
          )}

          {/* Stock */}
          <div className="mt-6">
            {product.stockQuantity > 0 ? (
              <span className="text-green-600 font-medium">
                ✓ In Stock
                {product.stockQuantity <= product.lowStockThreshold && (
                  <span className="text-orange-500 ml-2">
                    (Only {product.stockQuantity} left)
                  </span>
                )}
              </span>
            ) : (
              <span className="text-red-500 font-medium">✕ Out of Stock</span>
            )}
          </div>

          {/* Quantity + Add to Cart */}
          <div className="mt-8 flex items-center gap-4">
            <div className="flex items-center border rounded-lg">
              <button
                onClick={() => setQuantity(Math.max(1, quantity - 1))}
                className="p-3 hover:bg-gray-50"
              >
                <FiMinus />
              </button>
              <span className="px-6 py-3 font-semibold">{quantity}</span>
              <button
                onClick={() =>
                  setQuantity(
                    Math.min(product.maxOrderQuantity, quantity + 1)
                  )
                }
                className="p-3 hover:bg-gray-50"
              >
                <FiPlus />
              </button>
            </div>

            <button
              onClick={handleAddToCart}
              disabled={product.stockQuantity === 0}
              className="flex-1 flex items-center justify-center gap-2
                         bg-green-600 text-white py-3 px-6 rounded-lg
                         hover:bg-green-700 transition font-semibold
                         disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              <FiShoppingCart />
              Add to Cart
            </button>

            <button className="p-3 border rounded-lg hover:bg-red-50 hover:border-red-200 transition">
              <FiHeart className="text-gray-400 hover:text-red-500" />
            </button>
          </div>

          {/* Delivery Info */}
          <div className="mt-8 p-4 bg-gray-50 rounded-xl space-y-3">
            <div className="flex items-center gap-3">
              <FiTruck className="text-green-600" />
              <span className="text-sm text-gray-600">
                Delivery within 24 hours of harvest
              </span>
            </div>
            <div className="flex items-center gap-3">
              <FiMapPin className="text-green-600" />
              <span className="text-sm text-gray-600">
                Pickup available at farm
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}