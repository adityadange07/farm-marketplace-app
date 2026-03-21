import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useMutation } from '@tanstack/react-query';
import { FiUser, FiMail, FiPhone, FiCamera } from 'react-icons/fi';
import { useAuthStore } from '../../store/authStore';
import api from '../../api/axios';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import toast from 'react-hot-toast';

export default function Profile() {
  const user = useAuthStore((s) => s.user);
  const [avatarPreview, setAvatarPreview] = useState(null);

  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: {
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      phone: user?.phone || '',
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data) => api.put('/users/me', data),
    onSuccess: () => toast.success('Profile updated!'),
  });

  const onSubmit = (data) => updateMutation.mutate(data);

  const handleAvatarChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setAvatarPreview(URL.createObjectURL(file));

    const formData = new FormData();
    formData.append('file', file);

    try {
      await api.post('/users/me/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      toast.success('Avatar updated!');
    } catch (err) {
      toast.error('Failed to update avatar');
    }
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-8">My Profile</h1>

      {/* Avatar */}
      <div className="flex items-center gap-6 mb-8">
        <div className="relative">
          <div className="w-24 h-24 rounded-full bg-green-100 flex items-center
                          justify-center overflow-hidden">
            {avatarPreview || user?.avatarUrl ? (
              <img
                src={avatarPreview || user?.avatarUrl}
                alt="Avatar"
                className="w-full h-full object-cover"
              />
            ) : (
              <FiUser className="w-10 h-10 text-green-600" />
            )}
          </div>
          <label className="absolute bottom-0 right-0 p-1.5 bg-green-600
                            text-white rounded-full cursor-pointer hover:bg-green-700">
            <FiCamera className="w-4 h-4" />
            <input
              type="file"
              accept="image/*"
              onChange={handleAvatarChange}
              className="hidden"
            />
          </label>
        </div>
        <div>
          <h2 className="font-semibold text-lg">
            {user?.firstName} {user?.lastName}
          </h2>
          <p className="text-gray-500 text-sm">{user?.email}</p>
          <p className="text-xs text-green-600 mt-1 capitalize">{user?.role?.toLowerCase()}</p>
        </div>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="First Name"
            {...register('firstName', { required: 'Required' })}
            error={errors.firstName?.message}
          />
          <Input
            label="Last Name"
            {...register('lastName', { required: 'Required' })}
            error={errors.lastName?.message}
          />
        </div>

        <div className="flex items-center gap-2 p-3 bg-gray-50 rounded-lg">
          <FiMail className="text-gray-400" />
          <span className="text-sm text-gray-600">{user?.email}</span>
          <span className="text-xs text-gray-400 ml-auto">(cannot change)</span>
        </div>

        <Input
          label="Phone"
          type="tel"
          placeholder="+1 (555) 000-0000"
          {...register('phone')}
        />

        <Button type="submit" loading={updateMutation.isPending}>
          Save Changes
        </Button>
      </form>
    </div>
  );
}