import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '控制台', role: ['admin', 'teacher'] }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('../views/course/CategoryManage.vue'),
        meta: { title: '分类管理', role: ['admin'] }
      },
      {
        path: 'position',
        name: 'Position',
        component: () => import('../views/course/PositionManage.vue'),
        meta: { title: '位置管理', role: ['admin'] }
      },
      {
        path: 'course-relation',
        name: 'CourseRelation',
        component: () => import('../views/course/CourseRelation.vue'),
        meta: { title: '课程关联', role: ['admin'] }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('../views/course/CourseManage.vue'),
        meta: { title: '课程管理', role: ['admin'] }
      },
      {
        path: 'course-edit/:id?',
        name: 'CourseEdit',
        component: () => import('../views/course/CourseEdit.vue'),
        meta: { title: '编辑课程', role: ['admin', 'teacher'] }
      },
      {
        path: 'course-orders',
        name: 'CourseOrders',
        component: () => import('../views/course/CourseOrders.vue'),
        meta: { title: '课程订单', role: ['admin'] }
      },
      {
        path: 'publish-course',
        name: 'PublishCourse',
        component: () => import('../views/course/PublishCourse.vue'),
        meta: { title: '发布课程', role: ['teacher'] }
      },
      {
        path: 'my-courses',
        name: 'MyCourses',
        component: () => import('../views/course/MyCourses.vue'),
        meta: { title: '我的课程', role: ['admin', 'user'] }
      },
      {
        path: 'publish-article',
        name: 'PublishArticle',
        component: () => import('../views/content/PublishArticle.vue'),
        meta: { title: '发布文章', role: ['admin', 'teacher'] }
      },
      {
        path: 'publish-video',
        name: 'PublishVideo',
        component: () => import('../views/content/PublishVideo.vue'),
        meta: { title: '发布视频', role: ['admin', 'teacher'] }
      },
      {
        path: 'my-content',
        name: 'MyContent',
        component: () => import('../views/content/MyContent.vue'),
        meta: { title: '我的内容', role: ['admin', 'teacher'] }
      },
      {
        path: 'my-collection',
        name: 'MyCollection',
        component: () => import('../views/user/MyCollection.vue'),
        meta: { title: '我的收藏', role: ['admin', 'teacher', 'user'] }
      },
      {
        path: 'my-comment',
        name: 'MyComment',
        component: () => import('../views/user/MyComment.vue'),
        meta: { title: '我的评论', role: ['admin', 'teacher', 'user'] }
      },
      {
        path: 'my-like',
        name: 'MyLike',
        component: () => import('../views/user/MyLike.vue'),
        meta: { title: '我的点赞', role: ['admin', 'teacher', 'user'] }
      },
      {
        path: 'content-manage',
        name: 'ContentManage',
        component: () => import('../views/content/ContentManage.vue'),
        meta: { title: '内容管理', role: ['admin'] }
      },
      {
        path: 'comment-manage',
        name: 'CommentManage',
        component: () => import('../views/content/CommentManage.vue'),
        meta: { title: '评论管理', role: ['admin'] }
      },
      {
        path: 'user-manage',
        name: 'UserManage',
        component: () => import('../views/user/UserManage.vue'),
        meta: { title: '用户管理', role: ['admin'] }
      },
      {
        path: 'account',
        name: 'Account',
        component: () => import('../views/user/Account.vue'),
        meta: { title: '账号信息', role: ['admin', 'teacher', 'user'] }
      }
    ]
  }
]

const router = new VueRouter({
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole') || 'user'

  console.log('路由守卫 - 导航到:', to.path)
  console.log('路由守卫 - token状态:', token ? '已存在' : '不存在')
  console.log('路由守卫 - 用户角色:', userRole)

  if (to.path === '/login') {
    // 已登录用户访问登录页面，重定向到对应首页
    if (token) {
      console.log('路由守卫 - 已登录用户访问登录页面，重定向到对应首页')
      const homePath = getHomePathByRole(userRole)
      next(homePath)
    } else {
      console.log('路由守卫 - 未登录用户访问登录页面，直接通过')
      next()
    }
  } else if (to.path === '/') {
    // 根路径重定向到对应角色的首页
    if (token) {
      console.log('路由守卫 - 根路径重定向到对应角色的首页')
      const homePath = getHomePathByRole(userRole)
      next(homePath)
    } else {
      console.log('路由守卫 - 未登录用户访问根路径，重定向到登录页面')
      next('/login')
    }
  } else {
    if (!token) {
      console.log('路由守卫 - 无token，重定向到登录页面')
      next('/login')
    } else {
      // 检查路由是否需要特定角色
      if (to.meta.role && !to.meta.role.includes(userRole)) {
        console.log('路由守卫 - 权限不足，重定向到角色对应的首页')
        const homePath = getHomePathByRole(userRole)
        next(homePath)
      } else {
        console.log('路由守卫 - 验证通过，允许导航')
        next()
      }
    }
  }
})

// 根据角色获取对应的首页路径
function getHomePathByRole(role) {
  switch (role) {
    case 'admin':
      return '/dashboard'
    case 'teacher':
      return '/my-content'
    case 'user':
    default:
      return '/my-courses'
  }
}

export default router