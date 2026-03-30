import axios from 'axios';
import { useAuthStore } from '../store/authStore';
import toast from 'react-hot-toast';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ── Request Interceptor ─────────────────
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().accessToken;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // Add timing start
    config.metadata = { startTime: Date.now() };
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response Interceptor ────────────────
api.interceptors.response.use(
  (response) => {
    // Track successful API call
    const duration = Date.now() - response.config.metadata.startTime;
    performanceTracker.trackApiCall(
      response.config.method.toUpperCase(),
      response.config.url,
      duration,
      response.status
    );
    return response;
  },
  async (error) => {
    // Track failed API call
    if (error.config?.metadata) {
      const duration = Date.now() - error.config.metadata.startTime;
      performanceTracker.trackApiCall(
        error.config.method.toUpperCase(),
        error.config.url,
        duration,
        error.response?.status || 0
      );
    }

    const originalRequest = error.config;

    // Token expired — try refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = useAuthStore.getState().refreshToken;
        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken,
        });

        const { accessToken, refreshToken: newRefresh } = response.data.data;
        useAuthStore.getState().setTokens(accessToken, newRefresh);

        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        useAuthStore.getState().logout();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    // Show error toast
    const message =
      error.response?.data?.message || 'Something went wrong';
    if (error.response?.status !== 401) {
      toast.error(message);
    }

    return Promise.reject(error);
  }
);

export default api;