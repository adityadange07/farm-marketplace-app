import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import orderApi from '../api/orderApi';

export function useOrders(params) {
  return useQuery({
    queryKey: ['orders', params],
    queryFn: () => orderApi.getMyOrders(params),
  });
}

export function useOrder(id) {
  return useQuery({
    queryKey: ['order', id],
    queryFn: () => orderApi.getById(id),
    enabled: !!id,
  });
}

export function useFarmerOrders(params) {
  return useQuery({
    queryKey: ['farmer-orders', params],
    queryFn: () => orderApi.getFarmerOrders(params),
  });
}

export function useCreateOrder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data) => orderApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['orders']);
    },
  });
}