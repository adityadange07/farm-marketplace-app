export const API_URL = import.meta.env.VITE_API_URL || '/api';
export const STRIPE_KEY = import.meta.env.VITE_STRIPE_PUBLIC_KEY;
export const MAPS_KEY = import.meta.env.VITE_GOOGLE_MAPS_KEY;

export const ORDER_STATUS = {
  PENDING: 'PENDING',
  CONFIRMED: 'CONFIRMED',
  PROCESSING: 'PROCESSING',
  READY_FOR_PICKUP: 'READY_FOR_PICKUP',
  OUT_FOR_DELIVERY: 'OUT_FOR_DELIVERY',
  DELIVERED: 'DELIVERED',
  CANCELLED: 'CANCELLED',
  REFUNDED: 'REFUNDED',
};

export const UNIT_TYPES = [
  { value: 'KG', label: 'Kilogram (kg)' },
  { value: 'LB', label: 'Pound (lb)' },
  { value: 'PIECE', label: 'Piece' },
  { value: 'DOZEN', label: 'Dozen' },
  { value: 'BUNCH', label: 'Bunch' },
  { value: 'LITER', label: 'Liter' },
  { value: 'BASKET', label: 'Basket' },
  { value: 'BOX', label: 'Box' },
];

export const CATEGORIES = [
  { slug: 'vegetables', name: 'Vegetables', emoji: '🥬' },
  { slug: 'fruits', name: 'Fruits', emoji: '🍎' },
  { slug: 'dairy-eggs', name: 'Dairy & Eggs', emoji: '🥛' },
  { slug: 'meat-poultry', name: 'Meat & Poultry', emoji: '🥩' },
  { slug: 'grains-cereals', name: 'Grains', emoji: '🌾' },
  { slug: 'honey-preserves', name: 'Honey', emoji: '🍯' },
  { slug: 'herbs-spices', name: 'Herbs', emoji: '🌿' },
  { slug: 'flowers-plants', name: 'Flowers', emoji: '🌻' },
];