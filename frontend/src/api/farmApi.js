import api from './axios';

const farmApi = {
  getById: (id) => api.get(`/farms/${id}`).then((r) => r.data.data),
  getProducts: (id, params) =>
    api.get(`/farms/${id}/products`, { params }).then((r) => r.data),
  getNearby: (lat, lng, radiusKm) =>
    api.get('/farms/nearby', { params: { latitude: lat, longitude: lng, radiusKm } })
      .then((r) => r.data.data),
  create: (data) => api.post('/farms', data).then((r) => r.data.data),
  update: (data) => api.put('/farms', data).then((r) => r.data.data),
};

export default farmApi;