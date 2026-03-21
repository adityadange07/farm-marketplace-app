import { Link } from 'react-router-dom';
import { FiArrowRight, FiMapPin, FiTruck, FiShield } from 'react-icons/fi';
import { GiWheat, GiFruitBowl, GiCow, GiHoneyJar } from 'react-icons/gi';
import ProductCard from '../components/product/ProductCard';
import FarmCard from '../components/farm/FarmCard';
import { useFeaturedProducts, useNearbyProducts } from '../hooks/useProduct';
import { useLocation } from '../hooks/useLocation';
import Spinner from '../components/ui/Spinner';

const categories = [
  { name: 'Vegetables', slug: 'vegetables', icon: GiWheat, color: 'bg-green-100 text-green-600' },
  { name: 'Fruits', slug: 'fruits', icon: GiFruitBowl, color: 'bg-red-100 text-red-600' },
  { name: 'Dairy & Eggs', slug: 'dairy-eggs', icon: GiCow, color: 'bg-yellow-100 text-yellow-600' },
  { name: 'Honey', slug: 'honey-preserves', icon: GiHoneyJar, color: 'bg-amber-100 text-amber-600' },
];

export default function Home() {
  const { location } = useLocation();
  const { data: featured, isLoading: loadingFeatured } = useFeaturedProducts();
  const { data: nearby, isLoading: loadingNearby } = useNearbyProducts(
    location?.lat,
    location?.lng,
    25
  );

  return (
    <div>
      {/* ════ HERO SECTION ════════════════ */}
      <section className="relative bg-gradient-to-r from-green-700 to-green-500 text-white">
        <div className="max-w-7xl mx-auto px-4 py-24 md:py-32">
          <div className="max-w-2xl">
            <h1 className="text-4xl md:text-6xl font-bold leading-tight">
              Fresh from the
              <span className="text-yellow-300"> Farm</span>
              <br />
              to Your Table
            </h1>
            <p className="mt-6 text-lg text-green-100">
              Buy directly from local farmers. Fresher produce, fair prices,
              and support for your community.
            </p>
            <div className="mt-8 flex gap-4">
              <Link
                to="/shop"
                className="px-8 py-3 bg-white text-green-700 rounded-full
                           font-semibold hover:bg-green-50 transition
                           flex items-center gap-2"
              >
                Shop Now <FiArrowRight />
              </Link>
              <Link
                to="/register?role=FARMER"
                className="px-8 py-3 border-2 border-white rounded-full
                           font-semibold hover:bg-white/10 transition"
              >
                Sell Your Produce
              </Link>
            </div>
          </div>
        </div>

        {/* Stats */}
        <div className="max-w-5xl mx-auto px-4 -mb-12 relative z-10">
          <div className="bg-white rounded-2xl shadow-lg p-6 grid grid-cols-3 gap-8 text-center">
            {[
              { value: '500+', label: 'Local Farms' },
              { value: '10K+', label: 'Happy Customers' },
              { value: '50K+', label: 'Products Delivered' },
            ].map((stat) => (
              <div key={stat.label}>
                <p className="text-3xl font-bold text-green-600">{stat.value}</p>
                <p className="text-gray-500 mt-1">{stat.label}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ════ CATEGORIES ══════════════════ */}
      <section className="max-w-7xl mx-auto px-4 pt-24 pb-16">
        <h2 className="text-2xl font-bold text-gray-900">Shop by Category</h2>
        <div className="mt-8 grid grid-cols-2 md:grid-cols-4 gap-4">
          {categories.map((cat) => (
            <Link
              key={cat.slug}
              to={`/shop/${cat.slug}`}
              className="flex flex-col items-center p-6 rounded-2xl border
                         hover:shadow-md transition group"
            >
              <div className={`p-4 rounded-full ${cat.color} group-hover:scale-110 transition`}>
                <cat.icon className="w-8 h-8" />
              </div>
              <span className="mt-3 font-medium text-gray-700">
                {cat.name}
              </span>
            </Link>
          ))}
        </div>
      </section>

      {/* ════ FEATURED PRODUCTS ═══════════ */}
      <section className="max-w-7xl mx-auto px-4 py-16">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-bold text-gray-900">
            Featured Products
          </h2>
          <Link
            to="/shop"
            className="text-green-600 hover:underline flex items-center gap-1"
          >
            View all <FiArrowRight />
          </Link>
        </div>

        {loadingFeatured ? (
          <div className="flex justify-center py-12">
            <Spinner />
          </div>
        ) : (
          <div className="mt-8 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {featured?.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
      </section>

      {/* ════ NEARBY (if location available) ═══ */}
      {location && (
        <section className="bg-green-50 py-16">
          <div className="max-w-7xl mx-auto px-4">
            <div className="flex items-center gap-2">
              <FiMapPin className="text-green-600" />
              <h2 className="text-2xl font-bold text-gray-900">
                Near You
              </h2>
            </div>

            {loadingNearby ? (
              <div className="flex justify-center py-12">
                <Spinner />
              </div>
            ) : (
              <div className="mt-8 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                {nearby?.slice(0, 8).map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    showDistance
                  />
                ))}
              </div>
            )}
          </div>
        </section>
      )}

      {/* ════ WHY CHOOSE US ═══════════════ */}
      <section className="max-w-7xl mx-auto px-4 py-20">
        <h2 className="text-2xl font-bold text-center">Why Farm Fresh?</h2>
        <div className="mt-12 grid md:grid-cols-3 gap-8">
          {[
            {
              icon: FiMapPin,
              title: 'Direct from Farms',
              desc: 'No middlemen. Products come straight from verified local farms.',
            },
            {
              icon: FiTruck,
              title: 'Fresh Delivery',
              desc: 'Harvested and delivered within 24 hours for maximum freshness.',
            },
            {
              icon: FiShield,
              title: 'Quality Guaranteed',
              desc: 'All farms are verified. Organic certifications you can trust.',
            },
          ].map((feature) => (
            <div
              key={feature.title}
              className="text-center p-6 rounded-2xl hover:bg-green-50 transition"
            >
              <div className="inline-flex p-4 bg-green-100 rounded-full">
                <feature.icon className="w-6 h-6 text-green-600" />
              </div>
              <h3 className="mt-4 text-lg font-semibold">{feature.title}</h3>
              <p className="mt-2 text-gray-500">{feature.desc}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}