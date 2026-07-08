import axios from 'axios'

// Create axios instance
const request = axios.create({
  // Use Vite proxy
  baseURL: '/api',
  timeout: 60000,
  // If using cookies/session, uncomment the following line
  // withCredentials: true,
})

// Request Interceptor
request.interceptors.request.use(
  (config) => {
    // Add token from localStorage
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    console.log(`[Request] ${config.method.toUpperCase()} ${config.url}`, config.data)

    // Add timestamp for GET requests to prevent caching
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }
    return config
  },
  (error) => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// Response Interceptor
request.interceptors.response.use(
  (response) => {
    console.log(`[Response] ${response.config.url}`, response.status, response.data)
    // Return data directly if exists, otherwise return response
    return response.data ?? response
  },
  (error) => {
    // Handle errors
    const status = error?.response?.status
    const message = error?.response?.data?.message || error.message || '请求失败'

    if (status === 401 || status === 403) {
      // Handle Unauthorized/Forbidden
      // Optional: Clear token and redirect to login
      // localStorage.removeItem('token')
      // window.location.href = '/login'
      console.warn('Access Denied:', status)

      // Dispatch event for UI handling
      window.dispatchEvent(new CustomEvent('auth:unauthorized'))

      return Promise.reject(new Error('没有权限访问'))
    }

    // Global error event
    window.dispatchEvent(new CustomEvent('api:error', { detail: { message } }))

    return Promise.reject(error)
  }
)

// Request retry decorator
export function withRetry(requestFn, retries = 3, delay = 1000) {
  return async (...args) => {
    for (let i = 0; i < retries; i++) {
      try {
        return await requestFn(...args)
      } catch (error) {
        if (i === retries - 1) throw error
        await new Promise(resolve => setTimeout(resolve, delay * (i + 1)))
      }
    }
  }
}

// Cancel all pending requests
export function cancelAllRequests() {
  // Implementation for simple axios instance:
  // Since we removed complex cancel token logic for simplicity, 
  // we can't easily cancel specifically tracked requests.
  // This function is kept for API compatibility.
  console.warn('cancelAllRequests is not fully implemented in this simplified version')
}

export default request
