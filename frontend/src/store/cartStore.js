import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import toast from 'react-hot-toast';

export const useCartStore = create(
  persist(
    (set, get) => ({
      items: [],
      isOpen: false,

      // ── Add Item ──────────────────
      addItem: (product, quantity = 1) => {
        set((state) => {
          const existing = state.items.findIndex(
            (item) => item.product.id === product.id
          );

          if (existing > -1) {
            const newItems = [...state.items];
            const newQty = newItems[existing].quantity + quantity;

            if (newQty > product.maxOrderQuantity) {
              toast.error(`Max ${product.maxOrderQuantity} per order`);
              return state;
            }

            newItems[existing].quantity = newQty;
            toast.success('Cart updated');
            return { items: newItems };
          }

          if (product.stockQuantity < quantity) {
            toast.error('Not enough stock');
            return state;
          }

          toast.success(`${product.name} added to cart`);
          return {
            items: [...state.items, { product, quantity }],
            isOpen: true,
          };
        });
      },

      // ── Remove Item ──────────────
      removeItem: (productId) => {
        set((state) => ({
          items: state.items.filter((i) => i.product.id !== productId),
        }));
        toast.success('Removed from cart');
      },

      // ── Update Quantity ───────────
      updateQuantity: (productId, quantity) => {
        if (quantity <= 0) {
          get().removeItem(productId);
          return;
        }
        set((state) => ({
          items: state.items.map((item) =>
            item.product.id === productId ? { ...item, quantity } : item
          ),
        }));
      },

      // ── Clear ─────────────────────
      clearCart: () => set({ items: [] }),
      toggleCart: () => set((s) => ({ isOpen: !s.isOpen })),
      openCart: () => set({ isOpen: true }),
      closeCart: () => set({ isOpen: false }),

      // ── Computed ──────────────────
      totalItems: () =>
        get().items.reduce((sum, item) => sum + item.quantity, 0),

      subtotal: () =>
        get().items.reduce(
          (sum, item) => sum + item.product.price * item.quantity,
          0
        ),

      // Group items by farm
      itemsByFarm: () => {
        const groups = {};
        get().items.forEach((item) => {
          const farmId = item.product.farm?.id || 'unknown';
          if (!groups[farmId]) {
            groups[farmId] = {
              farm: item.product.farm,
              items: [],
            };
          }
          groups[farmId].items.push(item);
        });
        return groups;
      },
    }),
    {
      name: 'farm-cart',
    }
  )
);