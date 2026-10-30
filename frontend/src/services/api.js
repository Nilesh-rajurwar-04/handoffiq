import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: attach JWT token and correlation ID
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('handoffiq_token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    config.headers['X-Correlation-Id'] = 'fe-' + Math.random().toString(36).substring(2, 10);
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: handle 401s gracefully
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      if (!window.location.pathname.includes('/login')) {
        localStorage.removeItem('handoffiq_token');
        localStorage.removeItem('handoffiq_user');
        window.location.href = '/login?session_expired=true';
      }
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  getMe: () => api.get('/auth/me'),
};

export const dashboardApi = {
  getSummary: () => api.get('/dashboard/summary'),
  getMetrics: () => api.get('/dashboard/summary'),
  getRecentActivities: (limit = 10) => api.get('/dashboard/activities', { params: { limit } }),
};

export const meetingsApi = {
  getAll: (params) => api.get('/meetings', { params }),
  getMeetings: (params) => api.get('/meetings', { params }),
  getById: (id) => api.get(`/meetings/${id}`),
  getMeeting: (id) => api.get(`/meetings/${id}`),
  create: (data) => api.post('/meetings', data),
  createMeeting: (data) => api.post('/meetings', data),
  updateNotes: (id, rawNotes, aiSummary) => api.patch(`/meetings/${id}/notes`, { rawNotes, aiSummary }),
  updateStatus: (id, status) => api.patch(`/meetings/${id}/status`, { status }),
  updateMeetingStatus: (id, status) => api.patch(`/meetings/${id}/status`, { status }),
  delete: (id) => api.delete(`/meetings/${id}`),
  getMeetingDecisions: (id) => api.get(`/meetings/${id}/decisions`),
  getMeetingTasks: (id) => api.get(`/meetings/${id}/tasks`),
};

export const tasksApi = {
  getAll: (params) => api.get('/tasks', { params }),
  getTasks: (params) => api.get('/tasks', { params }),
  getById: (id) => api.get(`/tasks/${id}`),
  getTask: (id) => api.get(`/tasks/${id}`),
  create: (data) => api.post('/tasks', data),
  createTask: (data) => api.post('/tasks', data),
  update: (id, data) => api.put(`/tasks/${id}`, data),
  updateStatus: (id, status, remarks) => api.patch(`/tasks/${id}/status`, { status, remarks }),
  updateTaskStatus: (id, status, remarks) => api.patch(`/tasks/${id}/status`, { status, remarks }),
  recordOutcome: (id, outcomeType, detailedNotes) => api.post(`/tasks/${id}/outcome`, { outcomeType, detailedNotes }),
  completeTask: (id, data) => api.post(`/tasks/${id}/outcome`, data),
  carryForward: (id, toMeetingId, toHandoverId, reason) => api.post(`/tasks/${id}/carry-forward`, { toMeetingId, toHandoverId, reason }),
  carryForwardTask: (id, data) => api.post(`/tasks/${id}/carry-forward`, data),
  reassign: (id, newAssigneeId, reason) => api.post(`/tasks/${id}/reassign`, { newAssigneeId, reason }),
  reassignTask: (id, newAssigneeId, reason) => api.post(`/tasks/${id}/reassign`, { newAssigneeId, reason }),
  addComment: (id, comment) => api.post(`/tasks/${id}/comments`, { comment }),
  addTaskComment: (id, data) => api.post(`/tasks/${id}/comments`, data),
};

export const handoversApi = {
  getAll: (params) => api.get('/handovers', { params }),
  getHandovers: (params) => api.get('/handovers', { params }),
  getById: (id) => api.get(`/handovers/${id}`),
  getHandover: (id) => api.get(`/handovers/${id}`),
  create: (data) => api.post('/handovers', data),
  createHandover: (data) => api.post('/handovers', data),
  getReadiness: (id) => api.get(`/handovers/${id}/readiness`),
  getReadinessScore: (id) => api.get(`/handovers/${id}/readiness`),
  accept: (id, comments) => api.post(`/handovers/${id}/accept`, { comments }),
  acknowledgeHandover: (id, data) => api.post(`/handovers/${id}/accept`, data),
  complete: (id) => api.post(`/handovers/${id}/complete`),
  completeHandover: (id) => api.post(`/handovers/${id}/complete`),
};

export const aiApi = {
  analyzeMeeting: (data) => api.post('/ai/meeting-analysis', data),
  analyzeMeetingNotes: (data) => api.post('/ai/meeting-analysis', data),
  approveMeetingIntelligence: (data) => api.post('/ai/meeting-intelligence/approve', data),
  getCatchUpBrief: (data) => api.post('/ai/catch-up-brief', data),
  generateCatchUpBrief: (data) => api.post('/ai/catch-up-brief', data),
  getHandoverSummary: (handoverId) => api.post('/ai/handover-summary', { handoverId }),
  generateHandoverReport: (data) => api.post('/ai/handover-summary', data),
  askAssistant: (data) => api.post('/ai/assistant', data),
  searchKnowledge: (data) => api.post('/ai/knowledge-search', data),
  semanticSearchKnowledge: (data) => api.post('/ai/knowledge-search', data),
};

export const documentsApi = {
  getAll: (params) => api.get('/documents', { params }),
  getDocuments: (params) => api.get('/documents', { params }),
  getById: (id) => api.get(`/documents/${id}`),
  getDocument: (id) => api.get(`/documents/${id}`),
  upload: (formData) => api.post('/documents/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  createDocument: (data) => api.post('/documents', data),
  search: (query) => api.get('/documents/search', { params: { query } }),
  download: (id) => api.get(`/documents/${id}/download`, { responseType: 'blob' }),
  delete: (id) => api.delete(`/documents/${id}`),
  deleteDocument: (id) => api.delete(`/documents/${id}`),
};

export const notificationsApi = {
  getAll: (params) => api.get('/notifications', { params }),
  getNotifications: (params) => api.get('/notifications', { params }),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markAsRead: (id) => api.patch(`/notifications/${id}/read`),
  markAllAsRead: () => api.patch('/notifications/read-all'),
  getPendingFollowUps: () => api.get('/notifications/pending-follow-ups'),
};

export const usersApi = {
  getAll: (search) => api.get('/users', { params: { search } }),
  getUsers: (params) => api.get('/users', { params }),
  getById: (id) => api.get(`/users/${id}`),
  getWorkload: () => api.get('/users/workload'),
};

export const projectsApi = {
  getAll: () => api.get('/projects'),
  getProjects: () => api.get('/projects'),
  getById: (id) => api.get(`/projects/${id}`),
  getProject: (id) => api.get(`/projects/${id}`),
  getContinuity: (id) => api.get(`/projects/${id}/continuity`),
  getTimeline: (id) => api.get(`/projects/${id}/timeline`),
};

// Aliases for singular names
export const meetingApi = meetingsApi;
export const taskApi = tasksApi;
export const handoverApi = handoversApi;
export const documentApi = documentsApi;
export const notificationApi = notificationsApi;
export const userApi = usersApi;
export const projectApi = projectsApi;

export default api;
