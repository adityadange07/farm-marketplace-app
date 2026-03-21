import api from './axios';

const cartApi = {
  get: () => api.get('/cart').then((r) => r.data.data),
  addItem: (data) => api.post('/cart/items', data).then((r) => r.data.data),
  updateQuantity: (itemId, quantity) =>
    api.put(`/cart/items/${itemId}`, { quantity }).then((r) => r.data.data),
  removeItem: (itemId) =>
    api.delete(`/cart/items/${itemId}`).then((r) => r.data.data),
  clear: () => api.delete('/cart').then((r) => r.data),
};

export default cartApi;