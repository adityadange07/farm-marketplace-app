import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { FiUpload, FiX, FiPlus } from 'react-icons/fi';
import toast from 'react-hot-toast';
import productApi from '../../api/productApi';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';

const UNIT_OPTIONS = [
  { value: 'KG', label: 'Kilogram (kg)' },
  { value: 'LB', label: 'Pound (lb)' },
  { value: 'PIECE', label: 'Piece' },
  { value: 'DOZEN', label: 'Dozen' },
  { value: 'BUNCH', label: 'Bunch' },
  { value: 'BASKET', label: 'Basket' },
  { value: 'BOX', label: 'Box' },
];

export default function AddProduct() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [images, setImages] = useState([]);
  const [previews, setPreviews] = useState([]);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      stockQuantity: 0,
      lowStockThreshold: 5,
      maxOrderQuantity: 100,
      isOrganic: false,
      isSeasonal: false,
    },
  });

  const createMutation = useMutation({
    mutationFn: (formData) => productApi.create(formData),
    onSuccess: () => {
      toast.success('Product created successfully!');
      queryClient.invalidateQueries(['farmer', 'products']);
      navigate('/farmer/products');
    },
  });

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);
    if (images.length + files.length > 5) {
      toast.error('Maximum 5 images allowed');
      return;
    }

    setImages((prev) => [...prev, ...files]);

    // Generate previews
    files.forEach((file) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        setPreviews((prev) => [...prev, e.target.result]);
      };
      reader.readAsDataURL(file);
    });
  };

  const removeImage = (index) => {
    setImages((prev) => prev.filter((_, i) => i !== index));
    setPreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const onSubmit = (data) => {
    const formData = new FormData();

    // Append product JSON
    const productJson = JSON.stringify({
      name: data.name,
      description: data.description,
      shortDescription: data.shortDescription,
      price: parseFloat(data.price),
      compareAtPrice: data.compareAtPrice
        ? parseFloat(data.compareAtPrice) : null,
      unit: data.unit,
      stockQuantity: parseInt(data.stockQuantity),
      lowStockThreshold: parseInt(data.lowStockThreshold),
      maxOrderQuantity: parseInt(data.maxOrderQuantity),
      isOrganic: data.isOrganic,
      isSeasonal: data.isSeasonal,
      growingMethod: data.growingMethod,
      categoryId: data.categoryId || null,
    });

    formData.append(
      'product',
      new Blob([productJson], { type: 'application/json' })
    );

    // Append images
    images.forEach((file) => {
      formData.append('images', file);
    });

    createMutation.mutate(formData);
  };

  return (
    <div className="max-w-3xl mx-auto">
      <h1 className="text-2xl font-bold mb-8">Add New Product</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
        {/* ── Images ─────────────────── */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Product Images (max 5)
          </label>
          <div className="flex flex-wrap gap-4">
            {previews.map((preview, idx) => (
              <div
                key={idx}
                className="relative w-28 h-28 rounded-lg overflow-hidden border"
              >
                <img
                  src={preview}
                  alt=""
                  className="w-full h-full object-cover"
                />
                <button
                  type="button"
                  onClick={() => removeImage(idx)}
                  className="absolute top-1 right-1 p-1 bg-red-500
                             text-white rounded-full"
                >
                  <FiX className="w-3 h-3" />
                </button>
                {idx === 0 && (
                  <span className="absolute bottom-0 left-0 right-0
                                   text-center text-xs bg-green-600
                                   text-white py-0.5">
                    Primary
                  </span>
                )}
              </div>
            ))}
            {images.length < 5 && (
              <label className="w-28 h-28 border-2 border-dashed rounded-lg
                                flex flex-col items-center justify-center
                                cursor-pointer hover:border-green-500 transition">
                <FiUpload className="w-6 h-6 text-gray-400" />
                <span className="text-xs text-gray-400 mt-1">Upload</span>
                <input
                  type="file"
                  multiple
                  accept="image/*"
                  onChange={handleImageChange}
                  className="hidden"
                />
              </label>
            )}
          </div>
        </div>

        {/* ── Basic Info ─────────────── */}
        <div className="bg-white p-6 rounded-xl shadow-sm space-y-4">
          <h2 className="font-semibold text-lg">Basic Information</h2>

          <Input
            label="Product Name *"
            placeholder="e.g., Fresh Organic Tomatoes"
            {...register('name', { required: 'Name is required' })}
            error={errors.name?.message}
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              rows={4}
              className="w-full border rounded-lg px-3 py-2 focus:ring-2
                         focus:ring-green-500 focus:border-green-500"
              placeholder="Describe your product..."
              {...register('description')}
            />
          </div>

          <Input
            label="Short Description"
            placeholder="Brief one-liner"
            {...register('shortDescription')}
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Category
            </label>
            <select
              className="w-full border rounded-lg px-3 py-2"
              {...register('categoryId')}
            >
              <option value="">Select category</option>
              <option value="vegetables">Vegetables</option>
              <option value="fruits">Fruits</option>
              <option value="dairy-eggs">Dairy & Eggs</option>
              <option value="meat-poultry">Meat & Poultry</option>
              <option value="grains-cereals">Grains</option>
              <option value="honey-preserves">Honey & Preserves</option>
            </select>
          </div>
        </div>

        {/* ── Pricing ────────────────── */}
        <div className="bg-white p-6 rounded-xl shadow-sm space-y-4">
          <h2 className="font-semibold text-lg">Pricing & Inventory</h2>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Price *"
              type="number"
              step="0.01"
              placeholder="0.00"
              {...register('price', {
                required: 'Price is required',
                min: { value: 0.01, message: 'Must be > 0' },
              })}
              error={errors.price?.message}
            />

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Unit *
              </label>
              <select
                className="w-full border rounded-lg px-3 py-2"
                {...register('unit', { required: 'Unit is required' })}
              >
                {UNIT_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Compare at Price"
              type="number"
              step="0.01"
              placeholder="Market price (optional)"
              {...register('compareAtPrice')}
            />
            <Input
              label="Stock Quantity *"
              type="number"
              {...register('stockQuantity', {
                required: 'Required',
                min: { value: 0, message: 'Min 0' },
              })}
              error={errors.stockQuantity?.message}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Low Stock Alert"
              type="number"
              {...register('lowStockThreshold')}
            />
            <Input
              label="Max Order Qty"
              type="number"
              {...register('maxOrderQuantity')}
            />
          </div>
        </div>

        {/* ── Additional ─────────────── */}
        <div className="bg-white p-6 rounded-xl shadow-sm space-y-4">
          <h2 className="font-semibold text-lg">Details</h2>

          <Input
            label="Growing Method"
            placeholder="e.g., Hydroponic, Free-range, etc."
            {...register('growingMethod')}
          />

          <div className="flex gap-6">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                className="w-4 h-4 text-green-600 rounded"
                {...register('isOrganic')}
              />
              <span className="text-sm">🌿 Organic</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                className="w-4 h-4 text-green-600 rounded"
                {...register('isSeasonal')}
              />
              <span className="text-sm">🍂 Seasonal</span>
            </label>
          </div>
        </div>

        {/* ── Submit ─────────────────── */}
        <div className="flex justify-end gap-4">
          <Button
            type="button"
            variant="outline"
            onClick={() => navigate('/farmer/products')}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            loading={createMutation.isPending}
          >
            <FiPlus className="mr-2" />
            Create Product
          </Button>
        </div>
      </form>
    </div>
  );
}