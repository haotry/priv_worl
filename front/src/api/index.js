import axios from 'axios'

// Course category API
export const categoryApi = {
  // Get all categories
  getCategories() {
    return axios.get('/lesson/menu', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // Add a new category
  addCategory(data) {
    return axios.post('/lesson/menu/add', data)
  },

  // Update a category
  updateCategory(data) {
    return axios.post('/lesson/menu/update', data)
  },

  // Delete a category
  deleteCategory(id) {
    return axios.post('/lesson/menu/delete', null, {
      params: { id }
    })
  }
}

// Course recommendation position API
export const positionApi = {
  // Get all positions
  getPositions(page = 1, pageSize = 10) {
    return axios.get('/lesson/bar', {
      params: { page, pageSize }
    })
  },

  // Add a new position
  addPosition(data) {
    return axios.post('/lesson/bar/add', data)
  },

  // Update a position
  updatePosition(data) {
    return axios.post('/lesson/bar/update', data)
  },

  // Delete a position
  deletePosition(id) {
    return axios.post('/lesson/bar/delete', null, {
      params: { id }
    })
  }
}

// Course relation API
export const relationApi = {
  // Get related courses
  getRelations(type, page = 1, pageSize = 10) {
    return axios.get('/lesson/recommend', {
      params: { type, page, pageSize }
    })
  },

  // Add a new relation
  addRelation(data) {
    return axios.post('/lesson/recommend/add', data)
  },

  // Update a relation
  updateRelation(data) {
    return axios.post('/lesson/recommend/update', data)
  },

  // Delete a relation
  deleteRelation(id) {
    return axios.post('/lesson/recommend/delete', null, {
      params: { id }
    })
  }
}

// Course API
export const courseApi = {
  // Get all courses
  getAllCourses(menuId,pageSize,page){
    return axios.get('/lesson/list', {
      params: { menuId,pageSize,page },
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },


  /**
   * Get courses menu
   * @returns {Promise<AxiosResponse<any>>}
   */
  getCoursesMenu(){
    return axios.get('/lesson/menu', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // Get course list
  getCourses(menuId, page = 1, pageSize = 10) {
    return axios.get('/lesson/menu/list', {
      params: { menuId, page, pageSize }
    })
  },

  // Get teacher's courses
  getTeacherCourses(page = 1, pageSize = 10) {
    return axios.get('/lesson/teacher/list', {
      params: { page, pageSize },
    })
  },

  // Get course detail
  getCourseDetail(id, jwcode) {
    return axios.get('/lesson/detail', {
      params: { id, jwcode }
    })
  },

  // Add a new course
  addCourse(data) {
    return axios.post('/lesson/add', data)
  },

  // Update a course
  updateCourse(data) {
    return axios.post('/lesson/update', data)
  },

  // Delete a course
  deleteCourse(id) {
    return axios.post('/lesson/delete', null, {
      params: { id }
    })
  },

  // Approve a course
  approveCourse(id) {
    return axios.post('/lesson/approve', null, {
      params: { id }
    })
  },

  // Reject a course
  rejectCourse(id, reason) {
    return axios.post('/lesson/reject', null, {
      params: { id, reason }
    })
  },

  // Get course orders
  getOrders(page = 1, pageSize = 10) {
    return axios.get('/user/order', {
      params: { page, pageSize }
    })
  }
}

// User API
export const userApi = {
  // Get all users
  getUsers(page = 1, pageSize = 10) {
    return axios.get('/allusers', {
      params: { page, pageSize }
    })
  },

  updatePassword(jwcode, oldpas, newpas){
    return axios.post('/user/password', null, {
      params: { jwcode, oldpas, newpas }
    })
  },

  // Update user info
  updateUserName(jwcode, name) {
    return axios.post('/user/name', null, {
      params: { jwcode, name }
    })
  },

  // Update user avatar
  updateUserAvatar(jwcode, avatar) {
    return axios.post('/user/avatar', null, {
      params: { jwcode, avatar }
    })
  },

  // Delete a user
  deleteUser(jwcode) {
    return axios.post('/user/delete', null, {
      params: { jwcode }
    })
  },

  // Set user as teacher
  setTeacher(jwcode, isTeacher) {
    return axios.post('/user/set-teacher', null, {
      params: { jwcode, isTeacher }
    })
  },

  // Add credit to user
  addCredit(jwcode, credit) {
    return axios.post('/user/credit', null, {
      params: { jwcode, credit }
    })
  }
}

// Content API
export const contentApi = {
  // Get all content
  getContents(page = 1, pageSize = 10) {
    return axios.get('/followShare', {
      params: { page, pageSize },
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // Get user's content
  getUserContents(jwcode, page = 1, pageSize = 10) {
    return axios.get('/user/moment/list', {
      params: { jwcode, page, pageSize },
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // Get allUser's article
  getAllUserArticle(page,pageSize){
    return axios.get('/articles', {
      params: { pageSize: 10,page: 1 },
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // Get allUser's video
  getAllUserVideo(pageSize,page){
    return axios.get('/recommend/knowledge', {
      params: { pageSize: 10,page: 1 },
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // Get content details based on content type
  getContentDetail(jwcode,id) {
    // 文章
      return axios.get('/article/detail', {
            params: {jwcode,id },
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          },
      );
  },

  // Publish article
  publishArticle(title, content) {
    return axios.post('/user/article', null, {
      params: { title, content },
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // Publish video
  publishVideo(title, content, video, cover) {
    return axios.post('/user/video', null, {
      params: { title, content, video, cover },
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // Delete content
  deleteContent(key,id) {
    return axios.post(`/user/${key}/delete`, null, {
      params: { id }
    })
  },

  // Approve content
  approveContent(id) {
    return axios.post('/content/approve', null, {
      params: { id }
    })
  },

  // Reject content
  rejectContent(id) {
    return axios.post('/content/reject', null, {
      params: { id }
    })
  },

  // Get user's likes
  getLikes(page = 1, pageSize = 10) {
    return axios.get('/user/zan/list', {
      params: { page, pageSize }
    })
  },

  // Like/unlike content
  likeContent(id, status) {
    return axios.post('/zan', null, {
      params: { id, status }
    })
  },

  // Get user's collections
  getCollections(page = 1, pageSize = 10) {
    return axios.get('/user/collect/list', {
      params: { page, pageSize }
    })
  },

  // Collect/uncollect content
  collectContent(id, status) {
    return axios.post('/collect', null, {
      params: { id, status }
    })
  }
}

// Comment API
export const commentApi = {
  // Get comments for content
  getComments(id, page = 1, pageSize = 10) {
    return axios.get('/comment/list', {
      params: { id, page, pageSize }
    })
  },

  // Get all comments for admin
  getAllComments(page = 1, pageSize = 10) {
    return axios.get('/admin/comment/list', {
      params: { page, pageSize }
    })
  },

  // Add a comment
  addComment(id, content, replyId, replySubId) {
    return axios.post('/reply', null, {
      params: { id, content, replyId, replySubId }
    })
  },

  // Delete a comment
  deleteComment(id) {
    return axios.post('/comment/delete', null, {
      params: { id }
    })
  },
  // 我的评价列表
  getUserComments(id,page = 1, pageSize = 10){
    return axios.get('/comment/list', {
      params: { id,page, pageSize }
    })
  },
  // 获取当前用户的所有评论
  getMyComments(page = 1, pageSize = 10) {
    return axios.get('/comment/my', {
      params: { page, pageSize }
    })
  }
}