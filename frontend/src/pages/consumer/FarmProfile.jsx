import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { FiMapPin, FiStar, FiShield, FiClock } from 'react-icons/fi';
import { GiLeaf } from 'react-icons/gi';
import farmApi from '../../api/farmApi';
import ProductCard from '../../components/product/ProductCard';
import RatingStars from '../../components/common/RatingStars';
import Badge from '../../components/ui/Badge';
import Spinner from '../../components/ui/Spinner';

export default function FarmProfile() {
  const { id } = useParams();

  const { data: farm, isLoading } = useQuery({
    queryKey: ['farm', id],
    queryFn: () => farmApi.getById(id),
  });

  const { data: productsData } = useQuery({
    queryKey: ['farm-products', id],
    queryFn: () => farmApi.getProducts(id, { page: 0, size: 50 }),
    enabled: !!id,
  });

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!farm) return <div className="text-center py-20">Farm not found</div>;

  return (
    <div>
      {/* Banner */}
      <div className="h-64 bg-gradient-to-r from-green-600 to-green-400 relative">
        {farm.bannerImage && (
          <img
            src={farm.bannerImage}
            alt={farm.farmName}
            className="w-full h-full object-cover"
          />
        )}
        <div className="absolute inset-0 bg-black/30" />
      </div>

      <div className="max-w-7xl mx-auto px-4 -mt-16 relative z-10">
        {/* Farm Info Card */}
        <div className="bg-white rounded-2xl shadow-lg p-8">
          <div className="flex flex-col md:flex-row md:items-center justify-between">
            <div>
              <div className="flex items-center gap-3">
                <h1 className="text-3xl font-bold text-gray-900">
                  {farm.farmName}
                </h1>
                {farm.isOrganic && (
                  <Badge color="green"><GiLeaf className="mr-1" /> Organic</Badge>
                )}
                {farm.verificationStatus === 'VERIFIED' && (
                  <Badge color="blue"><FiShield className="mr-1" /> Verified</Badge>
                )}
              </div>
              <p className="flex items-center gap-1 text-gray-500 mt-2">
                <FiMapPin className="w-4 h-4" />
                {farm.city}, {farm.state}
              </p>
              <div className="flex items-center gap-3 mt-2">
                <RatingStars rating={farm.rating} />
                <span className="text-sm text-gray-400">
                  ({farm.totalReviews} reviews)
                </span>
              </div>
            </div>
            <div className="mt-4 md:mt-0 flex gap-3">
              <div className="text-center px-4 py-2 bg-green-50 rounded-lg">
                <p className="text-2xl font-bold text-green-600">
                  {productsData?.totalElements || 0}
                </p>
                <p className="text-xs text-gray-500">Products</p>
              </div>
              <div className="text-center px-4 py-2 bg-green-50 rounded-lg">
                <p className="text-2xl font-bold text-green-600">
                  {farm.deliveryRadiusKm}km
                </p>
                <p className="text-xs text-gray-500">Delivery</p>
              </div>
            </div>
          </div>

          {farm.description && (
            <p className="mt-6 text-gray-600 leading-relaxed">
              {farm.description}
            </p>
          )}
        </div>

        {/* Products */}
        <div className="mt-10 mb-16">
          <h2 className="text-2xl font-bold mb-6">Products</h2>
          {productsData?.data?.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
              {productsData.data.map((product) => (
                <ProductCard key={product.id} product={product} showFarm={false} />
              ))}
            </div>
          ) : (
            <p className="text-center text-gray-400 py-12">
              No products available
            </p>
          )}
        </div>
      </div>
    </div>
  );
}