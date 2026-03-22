import api from './axios';

const productApi = {
  getAll: (params) => api.get('/products', { params }).then((r) => r.data),
  getBySlug: (slug) => api.get(`/products/${slug}`).then((r) => r.data.data),
  getFeatured: () => api.get('/products/featured').then((r) => r.data.data),
  getNearby: (lat, lng, radiusKm) =>
    api.get('/products/nearby', { params: { latitude: lat, longitude: lng, radiusKm } })
      .then((r) => r.data.data),
  getFarmerProducts: (params) =>
    api.get('/products', { params: { ...params, myProducts: true } }).then((r) => r.data),
  create: (formData) =>
    api.post('/products', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then((r) => r.data.data),
  update: (id, formData) =>
    api.put(`/products/${id}`, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then((r) => r.data.data),
  delete: (id) => api.delete(`/products/${id}`).then((r) => r.data),
};

export default productApi;