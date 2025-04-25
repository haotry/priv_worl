import axios from 'axios'

/**
 * 获取用户列表
 * @param {Object} params - 查询参数: {page, pageSize, userIdentity}
 * @returns {Promise} - 返回用户列表数据
 */
export function getUserList(params) {
  return axios.get('/user/list', {
    params,
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

/**
 * 创建新用户
 * @param {Object} data - 用户数据
 * @returns {Promise} - 返回创建结果
 */
export function createUser(data) {
  return axios.post('/user/add', data, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

/**
 * 更新用户信息
 * @param {Object} data - 用户数据
 * @returns {Promise} - 返回更新结果
 */
export function updateUser(data) {
  return axios.put('/user/update', data, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

/**
 * 删除用户
 * @param {number|string} id - 用户ID
 * @returns {Promise} - 返回删除结果
 */
export function deleteUser(id) {
  return axios.delete(`/user/delete/${id}`, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

/**
 * 获取用户详情
 * @param {String} jwcode - 用户学号
 * @returns {Promise} - 返回用户详情
 */
export function getUserDetail(jwcode) {
  return axios.get('/user/info', {
    params: { jwcode },
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

/**
 * 修改用户积分
 * @param {number|string} id - 用户ID
 * @param {number} credit - 积分值
 * @returns {Promise} - 返回修改结果
 */
export function updateUserCredit(id, credit) {
  return axios.put(`/credit/${id}`, { credit }, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

/**
 * 更新用户角色
 * @param {String} jwcode - 用户学号
 * @param {Number} userIdentity - 用户角色(0=管理员，1=普通用户，2=讲师)
 * @returns {Promise} - 返回更新结果
 */
export function updateUserRole(jwcode, userIdentity) {
  return axios.post('/user/role', {
    jwcode,
    userIdentity
  }, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}