import api from './axios';

const orderApi = {
  create: (data) =>
    api.post('/orders', data).then((res) => res.data.data),

  getMyOrders: (params) =>
    api.get('/orders', { params }).then((res) => res.data),

  getById: (id) =>
    api.get(`/orders/${id}`).then((res) => res.data.data),

  // ── Farmer ───────────────────────
  getFarmerOrders: (params) =>
    api.get('/farmer/orders', { params }).then((res) => res.data),

  updateStatus: (id, data) =>
    api.put(`/farmer/orders/${id}/status`, data).then((res) => res.data.data),
};

export default orderApi;