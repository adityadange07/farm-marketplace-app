import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import toast from 'react-hot-toast';

export default function useAuth() {
  const navigate = useNavigate();
  const { user, isAuthenticated, login, register, logout, isLoading } =
    useAuthStore();

  const handleLogin = useCallback(
    async (email, password) => {
      try {
        const res = await login(email, password);
        toast.success(`Welcome back, ${res.data.firstName}!`);
        if (res.data.role === 'FARMER') {
          navigate('/farmer/dashboard');
        } else {
          navigate('/');
        }
      } catch (err) {
        // handled by interceptor
      }
    },
    [login, navigate]
  );

  const handleLogout = useCallback(() => {
    logout();
    navigate('/');
    toast.success('Logged out');
  }, [logout, navigate]);

  return {
    user,
    isAuthenticated,
    isLoading,
    login: handleLogin,
    register,
    logout: handleLogout,
    isFarmer: user?.role === 'FARMER',
    isAdmin: user?.role === 'ADMIN',
  };
}