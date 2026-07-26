import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080/api/v1',
  withCredentials: true, // Crucial for sending cookies to the backend
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;
