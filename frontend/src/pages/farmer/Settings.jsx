import { useQuery, useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { FiCreditCard, FiMapPin } from 'react-icons/fi';
import farmApi from '../../api/farmApi';
import api from '../../api/axios';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import toast from 'react-hot-toast';

export default function FarmerSettings() {
  const { data: farm, isLoading } = useQuery({
    queryKey: ['my-farm'],
    queryFn: () => api.get('/farms/mine').then((r) => r.data.data).catch(() => null),
  });

  const submitMutation = useMutation({
    mutationFn: (data) => farm ? farmApi.update(data) : farmApi.create(data),
    onSuccess: () => {
      toast.success(farm ? 'Farm updated!' : 'Farm Profile created!');
      setTimeout(() => window.location.reload(), 1000);
    },
  });

  const connectMutation = useMutation({
    mutationFn: () => api.post('/payments/connect').then((r) => r.data.data),
    onSuccess: (data) => {
      window.location.href = data.onboardingUrl;
    },
  });

  const { register, handleSubmit } = useForm({
    values: farm || {},
  });

  if (isLoading) {
    return <div className="flex justify-center py-20"><Spinner size="lg" /></div>;
  }

  return (
    <div className="max-w-3xl mx-auto space-y-8">
      <h1 className="text-2xl font-bold">Farm Settings</h1>

      {/* Farm Details */}
      <form
        onSubmit={handleSubmit((data) => submitMutation.mutate({
          ...data,
          deliveryRadiusKm: Number(data.deliveryRadiusKm) || 0,
          latitude: data.latitude || 39.7392,
          longitude: data.longitude || -104.9903
        }))}
        className="bg-white rounded-xl shadow-sm p-6 space-y-4"
      >
        <h2 className="text-lg font-semibold flex items-center gap-2">
          <FiMapPin className="text-green-600" /> Farm Information
        </h2>

        <Input label="Farm Name" {...register('farmName')} />
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Description
          </label>
          <textarea rows={3} className="w-full border rounded-lg px-3 py-2"
            {...register('description')}
          />
        </div>
        <Input label="Address Line 1" {...register('addressLine1')} />
        <Input label="Address Line 2 (Optional)" {...register('addressLine2')} />
        <div className="grid grid-cols-3 gap-4">
          <Input label="City" {...register('city')} />
          <Input label="State" {...register('state')} />
          <Input label="Zip Code" {...register('zipCode')} />
        </div>
        <Input label="Delivery Radius (km)" type="number"
          {...register('deliveryRadiusKm')}
        />

        <label className="flex items-center gap-2">
          <input type="checkbox" className="w-4 h-4 text-green-600 rounded"
            {...register('isOrganic')}
          />
          <span className="text-sm">Organic Farm</span>
        </label>

        <Button type="submit" loading={submitMutation.isPending}>
          {farm ? 'Save Changes' : 'Create Farm Profile'}
        </Button>
      </form>

      {/* Stripe Connect */}
      <div className="bg-white rounded-xl shadow-sm p-6">
        <h2 className="text-lg font-semibold flex items-center gap-2 mb-4">
          <FiCreditCard className="text-green-600" /> Payment Setup
        </h2>
        <p className="text-sm text-gray-500 mb-4">
          Connect your Stripe account to receive payments directly.
        </p>
        <Button
          onClick={() => connectMutation.mutate()}
          loading={connectMutation.isPending}
        >
          Connect Stripe Account
        </Button>
      </div>
    </div>
  );
}