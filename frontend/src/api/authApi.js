import api from './axios';

const authApi = {
  login: (data) => api.post('/auth/login', data).then((r) => r.data),
  register: (data) => api.post('/auth/register', data).then((r) => r.data),
  refresh: (token) =>
    api.post('/auth/refresh', { refreshToken: token }).then((r) => r.data),
  forgotPassword: (email) =>
    api.post('/auth/forgot-password', { email }).then((r) => r.data),
  resetPassword: (data) =>
    api.post('/auth/reset-password', data).then((r) => r.data),
};

export default authApi;