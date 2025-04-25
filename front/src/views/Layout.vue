<template>
  <el-container class="layout">
    <el-aside width="220px">
      <div class="logo">弘历投教后台管理系统</div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item v-if="isAdmin" index="/dashboard">
          <i class="el-icon-s-home"></i>
          <span slot="title">控制台</span>
        </el-menu-item>

<!--        <el-submenu v-if="isAdmin" index="1">-->
<!--          <template slot="title">-->
<!--            <i class="el-icon-s-management"></i>-->
<!--            <span>课程中心管理</span>-->
<!--          </template>-->
<!--          <el-menu-item index="/category">分类管理</el-menu-item>-->
<!--          <el-menu-item index="/position">位置管理</el-menu-item>-->
<!--          <el-menu-item index="/course-relation">课程关联</el-menu-item>-->
<!--          <el-menu-item index="/courses">课程管理</el-menu-item>-->
<!--          <el-menu-item index="/course-orders">课程订单</el-menu-item>-->
<!--        </el-submenu>-->

        <el-submenu index="1-1">
          <template slot="title">
            <i class="el-icon-reading"></i>
            <span>课程专区</span>
          </template>
          <el-menu-item index="/publish-course" v-if="isTeacher">发布课程</el-menu-item>
          <el-menu-item index="/my-courses" v-if="!isTeacher">我的课程</el-menu-item>
        </el-submenu>

        <el-submenu v-if="isAdmin || isTeacher" index="2">
          <template slot="title">
            <i class="el-icon-document"></i>
            <span>内容发布</span>
          </template>
          <el-menu-item index="/publish-article">发文章</el-menu-item>
          <el-menu-item index="/publish-video">发视频</el-menu-item>
        </el-submenu>

        <el-submenu index="3">
          <template slot="title">
            <i class="el-icon-user"></i>
            <span>个人中心</span>
          </template>
          <el-menu-item v-if="isAdmin || isTeacher" index="/my-content">我的内容</el-menu-item>
          <el-menu-item index="/my-collection">我的收藏</el-menu-item>
          <el-menu-item index="/my-comment">我的评论</el-menu-item>
          <el-menu-item index="/my-like">我的点赞</el-menu-item>
        </el-submenu>


          <el-menu-item index="/user-manage" v-if="isAdmin">      <i class="el-icon-user"></i>
            <span slot="title">用户管理</span></el-menu-item>


        <el-menu-item index="/account">
          <i class="el-icon-setting"></i>
          <span slot="title">账号信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <div class="header-left">
          <span>{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <img class="avatar" :src="userAvatar" alt="avatar">
              <span>{{ displayName }}
                <el-tag v-if="isAdmin" size="mini" type="danger">管理员</el-tag>
                <el-tag v-else-if="isTeacher" size="mini" type="warning">教师</el-tag>
                <el-tag v-else size="mini" type="info">学生</el-tag>
              </span>
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="account">账号信息</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'Layout',
  data() {
    return {
      loading: false
    }
  },
  computed: {
    ...mapState(['userInfo', 'userRole', 'token']),
    activeMenu() {
      return this.$route.path
    },
    currentTitle() {
      return this.$route.meta?.title || '控制台'
    },
    isAdmin() {
      return this.userRole === 'admin'
    },
    isTeacher() {
      return this.userRole === 'teacher'
    },
    isUser() {
      return true // 所有登录的用户都是用户
    },
    // 用户显示名
    displayName() {
      console.log('计算displayName，当前userInfo:', this.userInfo)
      return this.userInfo?.name || '未知用户'
    },
    // 用户头像
    userAvatar() {
      return this.userInfo?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    }
  },
  created() {
    // 先检查Vuex中是否有用户信息
    if (!this.userInfo) {
      // 尝试从localStorage获取用户信息
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        try {
          console.log('Layout从localStorage恢复用户信息')
          const userInfo = JSON.parse(userInfoStr)
          console.log('从localStorage解析的用户信息:', userInfo)
          // 直接使用commit更新state，确保完整地恢复用户信息
          this.$store.commit('SET_USER_INFO', userInfo)
          console.log('恢复后的Vuex userInfo:', this.$store.state.userInfo)
        } catch(e) {
          console.error('解析存储的用户信息失败:', e)
          // 如果解析失败，尝试从服务器获取
          this.fetchUserInfo()
        }
      } else {
        // 没有本地存储的用户信息，尝试从服务器获取
        this.fetchUserInfo()
      }
    } else {
      console.log('Layout组件已有用户信息:', this.userInfo)
    }
  },
  methods: {
    async fetchUserInfo() {
      // 如果已经有用户信息，或者没有token，就不进行获取操作
      if (this.userInfo || !this.token) {
        console.log('已有用户信息或无token，不进行用户信息获取', {
          hasUserInfo: !!this.userInfo,
          hasToken: !!this.token,
          name: this.userInfo?.name,
        })
        return
      }

      this.loading = true
      console.log('Layout组件正在获取用户信息')
      try {
        await this.$store.dispatch('getUserInfo')
        console.log('Layout组件获取用户信息成功:', this.userInfo)
      } catch(error) {
        console.error('Layout组件获取用户信息失败:', error)
        this.$message.warning('获取最新用户信息失败，使用已有信息')
        // 不需要登出，使用登录时已获取的基本信息
      } finally {
        this.loading = false
      }
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.$store.dispatch('logout')
        this.$router.push('/login')
      } else if (command === 'account') {
        this.$router.push('/account')
      }
    }
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.el-aside {
  background-color: #304156;
  color: #fff;
  height: 100%;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  color: #fff;
  font-weight: bold;
  background-color: #2b3a4a;
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #fff;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  margin-right: 8px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>