import axios from 'axios';

const BASE_URL = '/api/v1';

const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

// Attach JWT token to every request
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 — redirect to login
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('access_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ---- Auth ----
export const authApi = {
  login: (data) => apiClient.post('/auth/login', data),
  register: (data) => apiClient.post('/auth/register', data),
};

// ---- Public articles ----
export const articleApi = {
  getAll: (params) => apiClient.get('/public/articles', { params }),
  getBySlug: (slug) => apiClient.get(`/public/articles/${slug}`),
  getBreaking: () => apiClient.get('/public/articles/breaking'),
  getFeatured: () => apiClient.get('/public/articles/featured'),
  getByCategory: (slug, params) => apiClient.get(`/public/categories/${slug}/articles`, { params }),
  getByTag: (slug, params) => apiClient.get(`/public/tags/${slug}/articles`, { params }),
  search: (params) => apiClient.get('/public/search', { params }),
};

// ---- Categories ----
export const categoryApi = {
  getAll: () => apiClient.get('/public/categories'),
};

// ---- CMS ----
export const cmsApi = {
  getMyArticles: (params) => apiClient.get('/cms/articles', { params }),
  createArticle: (data) => apiClient.post('/cms/articles', data),
  updateArticle: (id, data) => apiClient.put(`/cms/articles/${id}`, data),
  submitForReview: (id) => apiClient.patch(`/cms/articles/${id}/submit`),
  publish: (id) => apiClient.patch(`/cms/articles/${id}/publish`),
  schedule: (id, scheduledAt) => apiClient.patch(`/cms/articles/${id}/schedule`, null, { params: { scheduledAt } }),
  reject: (id, note) => apiClient.patch(`/cms/articles/${id}/reject`, null, { params: { note } }),
  archive: (id) => apiClient.patch(`/cms/articles/${id}/archive`),
  toggleBreaking: (id) => apiClient.patch(`/cms/articles/${id}/breaking`),
  uploadMedia: (formData) => apiClient.post('/cms/media/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
};

// ---- Admin ----
export const adminApi = {
  getUsers: (params) => apiClient.get('/admin/users', { params }),
  updateUser: (id, data) => apiClient.put(`/admin/users/${id}`, data),
  deactivateUser: (id) => apiClient.patch(`/admin/users/${id}/deactivate`),
  getPendingComments: (params) => apiClient.get('/admin/comments/pending', { params }),
  approveComment: (id) => apiClient.patch(`/admin/comments/${id}/approve`),
  rejectComment: (id) => apiClient.patch(`/admin/comments/${id}/reject`),
};

export default apiClient;
