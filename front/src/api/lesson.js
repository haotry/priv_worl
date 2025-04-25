import axios from 'axios'

/**
 * 发布文章
 * @param {String} title - 文章标题
 * @param {String} content - 文章内容
 * @returns {Promise} - 返回发布结果
 */
/*export const articalAPI={

}*/
/**
 * 获取课程列表
 * @param params
 * @returns {Promise<AxiosResponse<any>>}
 */
export function getLessonList(params) {
  return axios.get('/lesson/list', {
    params,
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

/**
 * 获取课程详情
 * @param {Number} id - 课程ID
 * @returns {Promise} - 返回课程详情
 */
export function getLessonDetail(id) {
  return axios.get(`/hltj/lesson/detail/${id}`, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

/**
 * 购买课程
 * @param {Number} id - 课程ID
 * @returns {Promise} - 返回购买结果
 */
export function buyLesson(id) {
  return axios.post('/lesson/buy', null, {
    params: { id },
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

/**
 * 获取我的课程列表
 * @param {Object} params - 查询参数: {page, pageSize}
 * @returns {Promise} - 返回我的课程列表数据
 */
export function getMyLessons(params) {
  return axios.get('/lesson/my', {
    params,
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}