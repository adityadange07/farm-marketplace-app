import api from './axios';

const reviewApi = {
  create: (data) => api.post('/reviews', data).then((r) => r.data.data),
  getProductReviews: (productId, params) =>
    api.get(`/products/${productId}/reviews`, { params }).then((r) => r.data),
};

export default reviewApi;