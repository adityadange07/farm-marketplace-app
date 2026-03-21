import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import productApi from '../../api/productApi';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import toast from 'react-hot-toast';

export default function EditProduct() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: product, isLoading } = useQuery({
    queryKey: ['product-edit', id],
    queryFn: () => productApi.getBySlug(id),
  });

  const updateMutation = useMutation({
    mutationFn: (data) => productApi.update(id, data),
    onSuccess: () => {
      toast.success('Product updated!');
      queryClient.invalidateQueries(['farmer-products']);
      navigate('/farmer/products');
    },
  });

  const { register, handleSubmit, formState: { errors } } = useForm({
    values: product
      ? {
          name: product.name,
          description: product.description,
          shortDescription: product.shortDescription,
          price: product.price,
          stockQuantity: product.stockQuantity,
          isOrganic: product.isOrganic,
          status: product.status,
        }
      : undefined,
  });

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto">
      <h1 className="text-2xl font-bold mb-8">Edit Product</h1>

      <form
        onSubmit={handleSubmit((data) => updateMutation.mutate(data))}
        className="space-y-6"
      >
        <div className="bg-white p-6 rounded-xl shadow-sm space-y-4">
          <Input
            label="Product Name"
            {...register('name', { required: 'Required' })}
            error={errors.name?.message}
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              rows={4}
              className="w-full border rounded-lg px-3 py-2"
              {...register('description')}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Price"
              type="number"
              step="0.01"
              {...register('price', { required: 'Required' })}
              error={errors.price?.message}
            />
            <Input
              label="Stock Quantity"
              type="number"
              {...register('stockQuantity', { required: 'Required' })}
              error={errors.stockQuantity?.message}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Status
            </label>
            <select
              className="w-full border rounded-lg px-3 py-2"
              {...register('status')}
            >
              <option value="ACTIVE">Active</option>
              <option value="DRAFT">Draft</option>
              <option value="OUT_OF_STOCK">Out of Stock</option>
            </select>
          </div>

          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              className="w-4 h-4 text-green-600 rounded"
              {...register('isOrganic')}
            />
            <span className="text-sm">Organic</span>
          </label>
        </div>

        <div className="flex justify-end gap-4">
          <Button
            type="button"
            variant="outline"
            onClick={() => navigate('/farmer/products')}
          >
            Cancel
          </Button>
          <Button type="submit" loading={updateMutation.isPending}>
            Save Changes
          </Button>
        </div>
      </form>
    </div>
  );
}