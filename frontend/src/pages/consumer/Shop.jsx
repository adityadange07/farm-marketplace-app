import { useState, useEffect, useCallback } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import {
  FiGrid, FiList, FiMap, FiSliders,
  FiX, FiChevronDown,
} from 'react-icons/fi';
import { useInfiniteQuery } from '@tanstack/react-query';
import productApi from '../../api/productApi';
import { useLocation as useGeoLocation } from '../../hooks/useLocation';
import useDebounce from '../../hooks/useDebounce';
import ProductCard from '../../components/product/ProductCard';
import ProductFilters from '../../components/product/ProductFilters';
import SearchBar from '../../components/common/SearchBar';
import Spinner from '../../components/ui/Spinner';
import Button from '../../components/ui/Button';

const SORT_OPTIONS = [
  { value: 'newest', label: 'Newest' },
  { value: 'price_asc', label: 'Price: Low → High' },
  { value: 'price_desc', label: 'Price: High → Low' },
  { value: 'rating', label: 'Top Rated' },
  { value: 'popular', label: 'Most Popular' },
];

export default function Shop() {
  const { category: urlCategory } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const { location: geoLocation } = useGeoLocation();

  const [showFilters, setShowFilters] = useState(false);
  const [viewMode, setViewMode] = useState('grid');

  const [filters, setFilters] = useState({
    search: searchParams.get('q') || '',
    category: urlCategory || searchParams.get('category') || '',
    minPrice: searchParams.get('minPrice') || '',
    maxPrice: searchParams.get('maxPrice') || '',
    isOrganic: searchParams.get('organic') === 'true',
    sortBy: searchParams.get('sort') || 'newest',
    radiusKm: parseInt(searchParams.get('radius')) || 50,
  });

  const debouncedSearch = useDebounce(filters.search, 400);

  // Build query params
  const queryParams = {
    search: debouncedSearch || undefined,
    category: filters.category || undefined,
    minPrice: filters.minPrice || undefined,
    maxPrice: filters.maxPrice || undefined,
    isOrganic: filters.isOrganic || undefined,
    sortBy: filters.sortBy.replace('_asc', '').replace('_desc', ''),
    sortDir: filters.sortBy.includes('_asc') ? 'asc' : 'desc',
    latitude: geoLocation?.lat,
    longitude: geoLocation?.lng,
    radiusKm: filters.radiusKm,
    size: 20,
  };

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
  } = useInfiniteQuery({
    queryKey: ['products', queryParams],
    queryFn: ({ pageParam = 0 }) =>
      productApi.getAll({ ...queryParams, page: pageParam }),
    getNextPageParam: (lastPage) =>
      lastPage.hasMore ? lastPage.page + 1 : undefined,
  });

  const products = data?.pages.flatMap((page) => page.data) || [];
  const totalCount = data?.pages[0]?.totalElements || 0;

  // Update URL params
  useEffect(() => {
    const params = new URLSearchParams();
    if (filters.search) params.set('q', filters.search);
    if (filters.sortBy !== 'newest') params.set('sort', filters.sortBy);
    if (filters.isOrganic) params.set('organic', 'true');
    if (filters.minPrice) params.set('minPrice', filters.minPrice);
    if (filters.maxPrice) params.set('maxPrice', filters.maxPrice);
    setSearchParams(params, { replace: true });
  }, [filters]);

  const updateFilter = useCallback((key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  }, []);

  const clearFilters = () => {
    setFilters({
      search: '',
      category: urlCategory || '',
      minPrice: '',
      maxPrice: '',
      isOrganic: false,
      sortBy: 'newest',
      radiusKm: 50,
    });
  };

  const activeFilterCount = [
    filters.minPrice,
    filters.maxPrice,
    filters.isOrganic,
  ].filter(Boolean).length;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* ── Top Bar ──────────────────── */}
      <div className="bg-white border-b sticky top-16 z-30">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center gap-4">
            {/* Search */}
            <SearchBar
              value={filters.search}
              onChange={(val) => updateFilter('search', val)}
              placeholder="Search fresh produce, farms..."
              className="flex-1"
            />

            {/* Filter Toggle */}
            <button
              onClick={() => setShowFilters(!showFilters)}
              className={`flex items-center gap-2 px-4 py-2.5 border
                          rounded-lg hover:bg-gray-50 transition text-sm
                          font-medium ${showFilters ? 'bg-green-50 border-green-300' : ''}`}
            >
              <FiSliders className="w-4 h-4" />
              Filters
              {activeFilterCount > 0 && (
                <span className="bg-green-600 text-white text-xs w-5 h-5
                                 rounded-full flex items-center justify-center">
                  {activeFilterCount}
                </span>
              )}
            </button>

            {/* Sort */}
            <div className="relative hidden md:block">
              <select
                value={filters.sortBy}
                onChange={(e) => updateFilter('sortBy', e.target.value)}
                className="appearance-none border rounded-lg px-4 py-2.5
                           pr-8 text-sm bg-white cursor-pointer"
              >
                {SORT_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
              <FiChevronDown className="absolute right-2 top-1/2
                                        -translate-y-1/2 text-gray-400 w-4 h-4" />
            </div>

            {/* View Mode */}
            <div className="hidden md:flex border rounded-lg overflow-hidden">
              {[
                { mode: 'grid', icon: FiGrid },
                { mode: 'list', icon: FiList },
              ].map(({ mode, icon: Icon }) => (
                <button
                  key={mode}
                  onClick={() => setViewMode(mode)}
                  className={`p-2.5 ${
                    viewMode === mode
                      ? 'bg-green-50 text-green-600'
                      : 'text-gray-400 hover:bg-gray-50'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                </button>
              ))}
            </div>
          </div>

          {/* Results count + active filters */}
          <div className="flex items-center gap-3 mt-3 text-sm text-gray-500">
            <span>{totalCount} products found</span>
            {urlCategory && (
              <span className="px-2 py-0.5 bg-green-100 text-green-700
                               rounded-full text-xs capitalize">
                {urlCategory.replace('-', ' ')}
              </span>
            )}
            {geoLocation && (
              <span>• Within {filters.radiusKm}km</span>
            )}
            {activeFilterCount > 0 && (
              <button
                onClick={clearFilters}
                className="text-red-500 hover:underline flex items-center gap-1"
              >
                <FiX className="w-3 h-3" /> Clear filters
              </button>
            )}
          </div>
        </div>
      </div>

      {/* ── Main Content ─────────────── */}
      <div className="max-w-7xl mx-auto px-4 py-6">
        <div className="flex gap-6">
          {/* Sidebar Filters */}
          {showFilters && (
            <aside className="w-64 flex-shrink-0 hidden lg:block">
              <ProductFilters
                filters={filters}
                onChange={updateFilter}
                onClose={() => setShowFilters(false)}
              />
            </aside>
          )}

          {/* Product Grid */}
          <main className="flex-1">
            {isLoading ? (
              <div className="flex justify-center py-20">
                <Spinner size="lg" />
              </div>
            ) : products.length === 0 ? (
              <div className="text-center py-20">
                <p className="text-6xl mb-4">🥕</p>
                <h3 className="text-xl font-semibold text-gray-900">
                  No products found
                </h3>
                <p className="text-gray-500 mt-2">
                  Try adjusting your filters or search terms
                </p>
                <Button
                  variant="outline"
                  className="mt-4"
                  onClick={clearFilters}
                >
                  Clear All Filters
                </Button>
              </div>
            ) : (
              <>
                <div
                  className={
                    viewMode === 'grid'
                      ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6'
                      : 'space-y-4'
                  }
                >
                  {products.map((product) => (
                    <ProductCard
                      key={product.id}
                      product={product}
                      layout={viewMode}
                      showDistance={!!geoLocation}
                    />
                  ))}
                </div>

                {/* Load More */}
                {hasNextPage && (
                  <div className="flex justify-center mt-10">
                    <Button
                      variant="outline"
                      onClick={() => fetchNextPage()}
                      loading={isFetchingNextPage}
                    >
                      Load More Products
                    </Button>
                  </div>
                )}
              </>
            )}
          </main>
        </div>
      </div>

      {/* Mobile Filter Drawer */}
      {showFilters && (
        <div className="lg:hidden fixed inset-0 z-50">
          <div
            className="absolute inset-0 bg-black/40"
            onClick={() => setShowFilters(false)}
          />
          <div className="absolute right-0 top-0 bottom-0 w-80 bg-white
                          shadow-xl overflow-y-auto">
            <div className="flex items-center justify-between p-4 border-b">
              <h3 className="font-semibold">Filters</h3>
              <button onClick={() => setShowFilters(false)}>
                <FiX className="w-5 h-5" />
              </button>
            </div>
            <div className="p-4">
              <ProductFilters
                filters={filters}
                onChange={updateFilter}
                onClose={() => setShowFilters(false)}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}