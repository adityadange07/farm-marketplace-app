import { create } from 'zustand';

export const useUiStore = create((set) => ({
  sidebarOpen: true,
  mobileMenuOpen: false,
  theme: 'light',

  toggleSidebar: () =>
    set((s) => ({ sidebarOpen: !s.sidebarOpen })),
  toggleMobileMenu: () =>
    set((s) => ({ mobileMenuOpen: !s.mobileMenuOpen })),
  closeMobileMenu: () => set({ mobileMenuOpen: false }),
  setTheme: (theme) => set({ theme }),
}));