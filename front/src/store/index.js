import Vue from 'vue'
import Vuex from 'vuex'
import axios from 'axios'

Vue.use(Vuex)

// 从localStorage获取持久化的用户信息
const getUserInfoFromStorage = () => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    try {
      return JSON.parse(storedUserInfo)
    } catch (e) {
      console.error('解析存储的用户信息失败:', e)
      return null
    }
  }
  return null
}

export default new Vuex.Store({
  state: {
    userInfo: getUserInfoFromStorage(),
    token: localStorage.getItem('token') || '',
    userRole: localStorage.getItem('userRole') || 'user'
  },
  mutations: {
    SET_TOKEN(state, token) {
      console.log('设置token:', token)
      // 检查token长度，避免存储过长的token
      if (token && token.length > 2000) {
        console.warn('Token过长，不保存到localStorage')
        state.token = token
        // 只存储在内存中，不写入localStorage
      } else {
        state.token = token
        localStorage.setItem('token', token)
      }
    },
    SET_USER_INFO(state, userInfo) {
      console.log('设置用户信息，接收到的完整数据:', userInfo)
      // 确保所有必要字段都存在
      state.userInfo = {
        ...userInfo,
        tel: userInfo.tel || '',
        name: userInfo.name || '未知用户',
        avatar: userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
        role: userInfo.role || 'user',
        userIdentity: userInfo.userIdentity !== undefined ? userInfo.userIdentity : 1
      }

      // 根据userIdentity设置角色
      if (userInfo.userIdentity !== undefined) {
        state.userRole = userIdentityToRole(userInfo.userIdentity)
      } else {
        state.userRole = userInfo.role || 'user'
      }

      // 持久化存储用户信息
      localStorage.setItem('userInfo', JSON.stringify(state.userInfo))
      localStorage.setItem('userRole', state.userRole)

      console.log('更新后的state.userInfo:', state.userInfo)
      console.log('更新后的用户角色:', state.userRole)
    },
    CLEAR_USER(state) {
      console.log('清除用户信息')
      state.token = ''
      state.userInfo = null
      state.userRole = 'user'
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userRole')
    }
  },
  actions: {
    async login({ commit }, credentials) {
      console.log('登录action开始, 凭证:', credentials)
      try {
        // 清除所有之前的数据，以确保干净的登录
        commit('CLEAR_USER')

        // 准备请求配置
        const config = {
          headers: {
            'Content-Type': 'application/json',
            // 明确移除Authorization头
            'Authorization': undefined
          },
          // 确保不会自动带上之前的token
          withCredentials: false
        }

        // 移除不必要的大字段，减少请求大小
        const leanCredentials = { ...credentials }

        let response
        if (credentials.jwcode) {
          console.log('使用学号登录')
          response = await axios.post('/login/jwcode', leanCredentials, config)
        } else {
          console.log('使用手机号登录')
          response = await axios.post('/login/phone', leanCredentials, config)
        }

        console.log('登录响应:', response)
        const result = response.data.data

        // 检查token大小
        if (result.token && result.token.length > 1000) {
          console.warn('收到的token过大:', result.token.length)
          // 使用更短的标记，只为当前会话临时存储token
          const shortToken = result.token.substring(0, 1000)
          commit('SET_TOKEN', shortToken)
        } else {
          commit('SET_TOKEN', result.token)
        }

        // 提取用户数据
        const userData = result.hluser || result

        // 获取用户角色
        let userIdentity = userData.user_identity !== undefined
          ? userData.user_identity
          : (credentials.userIdentity || 1)

        const role = userIdentityToRole(userIdentity)

        // 更新用户信息
        commit('SET_USER_INFO', {
          id: userData.id,
          tel: userData.tel,
          jwcode: userData.jwcode,
          name: userData.name,
          avatar: userData.avatar,
          credit: userData.credit,
          userIdentity: userIdentity,
          role: role
        })

        return Promise.resolve(result)
      } catch (error) {
        console.error('登录失败:', error)

        // 专门处理431错误
        if (error.response && error.response.status === 431) {
          console.error('请求头过大错误，尝试清理存储')
          commit('CLEAR_USER')

          // 显示特定错误消息
          return Promise.reject(new Error('登录失败: 请求头过大，请清除浏览器缓存后重试'))
        }

        return Promise.reject(error)
      }
    },
    async getUserInfo({ commit, state }) {
      try {
        if (!state.token) return Promise.reject('No token')
        console.log('获取用户信息，当前token:', state.token)

        const response = await axios.get('/user/info', {
          params: { jwcode: state.userInfo?.jwcode || '' },
          headers: {
            'Authorization': `Bearer ${state.token}`
          }
        })

        console.log('获取用户信息响应:', response)
        const result = response.data.data
        console.log('获取用户信息原始返回数据:', result)

        // 更新用户信息
        commit('SET_USER_INFO', {
          id: result.id,
          tel: result.tel,
          jwcode: result.jwcode,
          name: result.name,
          avatar: result.avatar,
          credit: result.credit,
          role: result.isAdmin ? 'admin' : (result.isTeacher ? 'teacher' : 'user')
        })

        return Promise.resolve(result)
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return Promise.reject(error)
      }
    },
    logout({ commit }) {
      commit('CLEAR_USER')
    }
  },
  modules: {}
})

// 在store对象外部定义帮助函数
function determineUserRole(user) {
  // 首先检查是否从前端设置了明确的角色标识
  if (user.userIdentity !== undefined) {
    return userIdentityToRole(user.userIdentity)
  }

  // 检查多种可能的角色标识字段
  if (user.isAdmin === true || user.admin === true || user.role === 'admin') {
    return 'admin'
  }
  if (user.isTeacher === true || user.teacher === true || user.role === 'teacher') {
    return 'teacher'
  }
  // 根据jwcode判断
  if (user.jwcode === 'admin') {
    return 'admin'
  }
  if (user.jwcode && user.jwcode.startsWith('T')) {
    return 'teacher'
  }
  return 'user'
}

// 用户角色标识转换为角色字符串
function userIdentityToRole(userIdentity) {
  switch (parseInt(userIdentity)) {
    case 0:
      return 'admin'
    case 2:
      return 'teacher'
    case 1:
    default:
      return 'user'
  }
}