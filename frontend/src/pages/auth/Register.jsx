import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { FiUser, FiMail, FiLock, FiPhone, FiEye, FiEyeOff } from 'react-icons/fi';
import { GiWheat, GiFarmer, GiShoppingCart } from 'react-icons/gi';
import { useAuthStore } from '../../store/authStore';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import toast from 'react-hot-toast';

export default function Register() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const registerUser = useAuthStore((s) => s.register);
  const isLoading = useAuthStore((s) => s.isLoading);
  const [showPassword, setShowPassword] = useState(false);
  const [selectedRole, setSelectedRole] = useState(
    searchParams.get('role') || null
  );

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm();

  const password = watch('password');

  const onSubmit = async (data) => {
    try {
      await registerUser({
        ...data,
        role: selectedRole,
      });
      toast.success('Account created successfully!');
      if (selectedRole === 'FARMER') {
        navigate('/farmer/dashboard');
      } else {
        navigate('/shop');
      }
    } catch (err) {
      // handled by interceptor
    }
  };

  // ── Role Selection Screen ──
  if (!selectedRole) {
    return (
      <div className="min-h-[80vh] flex items-center justify-center px-4">
        <div className="w-full max-w-lg text-center">
          <GiWheat className="w-12 h-12 text-green-600 mx-auto" />
          <h1 className="mt-4 text-3xl font-bold text-gray-900">
            Join FarmFresh
          </h1>
          <p className="mt-2 text-gray-500">How would you like to use FarmFresh?</p>

          <div className="mt-8 grid grid-cols-2 gap-4">
            {/* Consumer */}
            <button
              onClick={() => setSelectedRole('CONSUMER')}
              className="p-8 border-2 rounded-2xl hover:border-green-500
                         hover:bg-green-50 transition-all group text-left"
            >
              <GiShoppingCart className="w-12 h-12 text-green-500
                                         group-hover:scale-110 transition" />
              <h3 className="mt-4 text-lg font-bold text-gray-900">
                I want to Buy
              </h3>
              <p className="mt-1 text-sm text-gray-500">
                Browse & buy fresh produce directly from local farmers
              </p>
            </button>

            {/* Farmer */}
            <button
              onClick={() => setSelectedRole('FARMER')}
              className="p-8 border-2 rounded-2xl hover:border-green-500
                         hover:bg-green-50 transition-all group text-left"
            >
              <GiFarmer className="w-12 h-12 text-green-500
                                    group-hover:scale-110 transition" />
              <h3 className="mt-4 text-lg font-bold text-gray-900">
                I want to Sell
              </h3>
              <p className="mt-1 text-sm text-gray-500">
                List your farm products and sell directly to consumers
              </p>
            </button>
          </div>

          <p className="mt-6 text-sm text-gray-400">
            Already have an account?{' '}
            <Link to="/login" className="text-green-600 hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    );
  }

  // ── Registration Form ──
  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <GiWheat className="w-10 h-10 text-green-600 mx-auto" />
          <h1 className="mt-4 text-2xl font-bold text-gray-900">
            Create {selectedRole === 'FARMER' ? 'Farmer' : ''} Account
          </h1>
          <p className="mt-1 text-gray-500">
            {selectedRole === 'FARMER'
              ? 'Start selling your fresh produce'
              : 'Get access to fresh local produce'}
          </p>
          <button
            onClick={() => setSelectedRole(null)}
            className="mt-2 text-sm text-green-600 hover:underline"
          >
            ← Change role
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="First Name"
              placeholder="John"
              {...register('firstName', { required: 'Required' })}
              error={errors.firstName?.message}
            />
            <Input
              label="Last Name"
              placeholder="Doe"
              {...register('lastName', { required: 'Required' })}
              error={errors.lastName?.message}
            />
          </div>

          <Input
            label="Email"
            type="email"
            placeholder="you@example.com"
            {...register('email', {
              required: 'Email is required',
              pattern: {
                value: /^\S+@\S+\.\S+$/,
                message: 'Invalid email format',
              },
            })}
            error={errors.email?.message}
          />

          <Input
            label="Phone (optional)"
            type="tel"
            placeholder="+1 (555) 000-0000"
            {...register('phone')}
          />

          <div className="relative">
            <Input
              label="Password"
              type={showPassword ? 'text' : 'password'}
              placeholder="Min 8 characters"
              className="pr-10"
              {...register('password', {
                required: 'Password is required',
                minLength: {
                  value: 8,
                  message: 'Min 8 characters',
                },
                pattern: {
                  value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/,
                  message: 'Must include uppercase, lowercase, and number',
                },
              })}
              error={errors.password?.message}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-9 text-gray-400"
            >
              {showPassword ? <FiEyeOff /> : <FiEye />}
            </button>
          </div>

          <Input
            label="Confirm Password"
            type="password"
            placeholder="Repeat password"
            {...register('confirmPassword', {
              required: 'Please confirm password',
              validate: (val) =>
                val === password || 'Passwords do not match',
            })}
            error={errors.confirmPassword?.message}
          />

          {/* Terms */}
          <label className="flex items-start gap-2">
            <input
              type="checkbox"
              className="w-4 h-4 mt-0.5 text-green-600 rounded"
              {...register('terms', {
                required: 'You must agree to terms',
              })}
            />
            <span className="text-sm text-gray-500">
              I agree to the{' '}
              <a href="#" className="text-green-600 hover:underline">
                Terms of Service
              </a>{' '}
              and{' '}
              <a href="#" className="text-green-600 hover:underline">
                Privacy Policy
              </a>
            </span>
          </label>
          {errors.terms && (
            <p className="text-xs text-red-500">{errors.terms.message}</p>
          )}

          <Button type="submit" className="w-full" size="lg" loading={isLoading}>
            Create Account
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-500">
          Already have an account?{' '}
          <Link to="/login" className="text-green-600 font-medium hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}