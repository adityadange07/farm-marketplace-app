import { useQuery, useInfiniteQuery } from '@tanstack/react-query';
import productApi from '../api/productApi';

export function useProducts(filters) {
  return useInfiniteQuery({
    queryKey: ['products', filters],
    queryFn: ({ pageParam = 0 }) =>
      productApi.getAll({ ...filters, page: pageParam }),
    getNextPageParam: (lastPage) =>
      lastPage.hasMore ? lastPage.page + 1 : undefined,
    keepPreviousData: true,
  });
}

export function useProduct(slug) {
  return useQuery({
    queryKey: ['product', slug],
    queryFn: () => productApi.getBySlug(slug),
    enabled: !!slug,
  });
}

export function useFeaturedProducts() {
  return useQuery({
    queryKey: ['products', 'featured'],
    queryFn: productApi.getFeatured,
    staleTime: 10 * 60 * 1000,
  });
}

export function useNearbyProducts(lat, lng, radiusKm) {
  return useQuery({
    queryKey: ['products', 'nearby', lat, lng, radiusKm],
    queryFn: () => productApi.getNearby(lat, lng, radiusKm),
    enabled: !!lat && !!lng,
  });
}