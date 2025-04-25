import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import axios from 'axios'

Vue.config.productionTip = false

// Use Element UI
Vue.use(ElementUI)

// Configure axios
axios.defaults.baseURL = '/api'
axios.defaults.maxHeadersLength = 4096; // 减小请求头大小限制
axios.defaults.timeout = 10000; // 设置超时时间
axios.defaults.headers.common['Cache-Control'] = 'no-cache'; // 减少缓存问题

// 清除所有可能导致问题的localStorage项
localStorage.removeItem('token');
localStorage.removeItem('userInfo');
localStorage.removeItem('userRole');
console.log('已清除所有存储的认证信息');

// 移除所有cookie
document.cookie.split(';').forEach(cookie => {
  const [name] = cookie.trim().split('=');
  document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
});

axios.interceptors.request.use(config => {
  // 核心改变：对于登录请求不添加Authorization头
  const isLoginRequest = config.url && (
    config.url.includes('/login/jwcode') ||
    config.url.includes('/login/phone')
  );

  if (isLoginRequest) {
    console.log('登录请求，不添加Authorization头');
    // 确保登录请求不带任何认证头
    delete config.headers.Authorization;
    return config;
  }

  const token = localStorage.getItem('token');
  if (token) {
    // 严格限制token长度
    if (token.length > 1000) {
      console.warn('Token过长，不添加到请求头，长度:', token.length);
      localStorage.removeItem('token');
      store.commit('CLEAR_USER');
      setTimeout(() => router.push('/login'), 0);
      return Promise.reject(new Error('Token too large'));
    }
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
}, error => {
  console.error('请求拦截器错误:', error);
  return Promise.reject(error);
})

axios.interceptors.response.use(
  response => {
    console.log('响应数据:', {
      url: response.config.url,
      status: response.status,
      data: response.data
    })
    return response
  },
  error => {
    console.error('响应错误:', error)
    if (error.response) {
      console.error('错误状态:', error.response.status)
      console.error('错误数据:', error.response.data)

      if (error.response.status === 401) {
        console.log('未授权，清除token并跳转登录页')
        localStorage.removeItem('token')
        localStorage.removeItem('userRole')
        router.push('/login')
      }
    }
    return Promise.reject(error)
  }
)

Vue.prototype.$http = axios

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')