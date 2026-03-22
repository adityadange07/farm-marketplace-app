import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  FiPlus, FiEdit2, FiTrash2, FiEye,
  FiMoreVertical, FiAlertTriangle,
} from 'react-icons/fi';
import { FaLeaf } from 'react-icons/fa';
import productApi from '../../api/productApi';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import Spinner from '../../components/ui/Spinner';
import toast from 'react-hot-toast';

export default function FarmerProducts() {
  const queryClient = useQueryClient();
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['farmer-products', statusFilter, page],
    queryFn: () =>
      productApi.getFarmerProducts({
        status: statusFilter || undefined,
        page,
        size: 20,
      }),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => productApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['farmer-products']);
      toast.success('Product archived');
    },
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">My Products</h1>
          <p className="text-gray-500 text-sm mt-1">
            Manage your product listings
          </p>
        </div>
        <Link to="/farmer/products/new">
          <Button>
            <FiPlus className="mr-2" /> Add Product
          </Button>
        </Link>
      </div>

      {/* Filters */}
      <div className="flex gap-2">
        {[
          { value: '', label: 'All' },
          { value: 'ACTIVE', label: 'Active' },
          { value: 'DRAFT', label: 'Draft' },
          { value: 'OUT_OF_STOCK', label: 'Out of Stock' },
        ].map((tab) => (
          <button
            key={tab.value}
            onClick={() => setStatusFilter(tab.value)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
              statusFilter === tab.value
                ? 'bg-green-100 text-green-700'
                : 'text-gray-500 hover:bg-gray-100'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Products Grid */}
      {isLoading ? (
        <div className="flex justify-center py-20">
          <Spinner size="lg" />
        </div>
      ) : data?.data?.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-xl shadow-sm">
          <p className="text-6xl mb-4">🌱</p>
          <h3 className="text-xl font-semibold text-gray-900">
            No products yet
          </h3>
          <p className="text-gray-500 mt-2">
            Start listing your fresh produce
          </p>
          <Link to="/farmer/products/new">
            <Button className="mt-4">
              <FiPlus className="mr-2" /> Add First Product
            </Button>
          </Link>
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="text-left px-6 py-3 text-xs font-medium
                               text-gray-500 uppercase">Product</th>
                <th className="text-left px-6 py-3 text-xs font-medium
                               text-gray-500 uppercase">Price</th>
                <th className="text-left px-6 py-3 text-xs font-medium
                               text-gray-500 uppercase">Stock</th>
                <th className="text-left px-6 py-3 text-xs font-medium
                               text-gray-500 uppercase">Status</th>
                <th className="text-left px-6 py-3 text-xs font-medium
                               text-gray-500 uppercase">Sales</th>
                <th className="text-right px-6 py-3 text-xs font-medium
                                text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {data?.data?.map((product) => (
                <tr key={product.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <img
                        src={
                          product.images?.[0]?.url ||
                          '/placeholder-product.jpg'
                        }
                        alt={product.name}
                        className="w-12 h-12 rounded-lg object-cover"
                      />
                      <div>
                        <p className="font-medium text-sm flex items-center gap-1">
                          {product.name}
                          {product.isOrganic && (
                            <FaLeaf className="text-green-500 w-3 h-3" />
                          )}
                        </p>
                        <p className="text-xs text-gray-400">
                          {product.category?.name}
                        </p>
                      </div>
                    </div>
                  </td>

                  <td className="px-6 py-4">
                    <p className="text-sm font-medium">
                      ${product.price.toFixed(2)}
                    </p>
                    <p className="text-xs text-gray-400">
                      per {product.unit}
                    </p>
                  </td>

                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <span
                        className={`text-sm font-medium ${
                          product.stockQuantity === 0
                            ? 'text-red-600'
                            : product.stockQuantity <=
                              (product.lowStockThreshold || 5)
                            ? 'text-yellow-600'
                            : 'text-gray-900'
                        }`}
                      >
                        {product.stockQuantity}
                      </span>
                      {product.stockQuantity <=
                        (product.lowStockThreshold || 5) &&
                        product.stockQuantity > 0 && (
                          <FiAlertTriangle className="w-3 h-3 text-yellow-500" />
                        )}
                    </div>
                  </td>

                  <td className="px-6 py-4">
                    <Badge
                      color={
                        product.status === 'ACTIVE'
                          ? 'green'
                          : product.status === 'DRAFT'
                          ? 'gray'
                          : 'red'
                      }
                      size="sm"
                    >
                      {product.status}
                    </Badge>
                  </td>

                  <td className="px-6 py-4">
                    <p className="text-sm">{product.totalSold} sold</p>
                    <p className="text-xs text-gray-400">
                      ⭐ {product.avgRating.toFixed(1)} ({product.reviewCount})
                    </p>
                  </td>

                  <td className="px-6 py-4 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <Link
                        to={`/product/${product.slug}`}
                        target="_blank"
                        className="p-2 hover:bg-gray-100 rounded-lg"
                        title="View"
                      >
                        <FiEye className="w-4 h-4 text-gray-400" />
                      </Link>
                      <Link
                        to={`/farmer/products/${product.id}/edit`}
                        className="p-2 hover:bg-gray-100 rounded-lg"
                        title="Edit"
                      >
                        <FiEdit2 className="w-4 h-4 text-gray-400" />
                      </Link>
                      <button
                        onClick={() => {
                          if (confirm('Archive this product?')) {
                            deleteMutation.mutate(product.id);
                          }
                        }}
                        className="p-2 hover:bg-red-50 rounded-lg"
                        title="Archive"
                      >
                        <FiTrash2 className="w-4 h-4 text-red-400" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}