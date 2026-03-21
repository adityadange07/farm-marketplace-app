import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import api from '../api/axios';

export const useAuthStore = create(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,

      // ── Register ──────────────────
      register: async (data) => {
        set({ isLoading: true });
        try {
          const res = await api.post('/auth/register', data);
          const { accessToken, refreshToken, ...user } = res.data.data;
          set({
            user,
            accessToken,
            refreshToken,
            isAuthenticated: true,
            isLoading: false,
          });
          return res.data;
        } catch (error) {
          set({ isLoading: false });
          throw error;
        }
      },

      // ── Login ─────────────────────
      login: async (email, password) => {
        set({ isLoading: true });
        try {
          const res = await api.post('/auth/login', { email, password });
          const { accessToken, refreshToken, ...user } = res.data.data;
          set({
            user,
            accessToken,
            refreshToken,
            isAuthenticated: true,
            isLoading: false,
          });
          return res.data;
        } catch (error) {
          set({ isLoading: false });
          throw error;
        }
      },

      // ── Logout ────────────────────
      logout: () => {
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
        });
      },

      // ── Set Tokens ────────────────
      setTokens: (accessToken, refreshToken) => {
        set({ accessToken, refreshToken });
      },

      // ── Computed ──────────────────
      isFarmer: () => get().user?.role === 'FARMER',
      isAdmin: () => get().user?.role === 'ADMIN',
    }),
    {
      name: 'farm-auth',
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);