<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="dashboard-card">
          <div slot="header" class="card-header">
            <span>用户统计</span>
          </div>
          <div class="card-content">
            <div class="card-item">
              <i class="el-icon-user card-icon"></i>
              <div class="item-info">
                <div class="item-value">{{ userCount }}</div>
                <div class="item-label">用户总数</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="dashboard-card">
          <div slot="header" class="card-header">
            <span>内容统计</span>
          </div>
          <div class="card-content">
            <div class="card-item">
              <i class="el-icon-document card-icon"></i>
              <div class="item-info">
                <div class="item-value">{{ contentCount }}</div>
                <div class="item-label">内容总数</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="dashboard-card">
          <div slot="header" class="card-header">
            <span>课程统计</span>
          </div>
          <div class="card-content">
            <div class="card-item">
              <i class="el-icon-reading card-icon"></i>
              <div class="item-info">
                <div class="item-value">{{ courseCount }}</div>
                <div class="item-label">课程总数</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="welcome-card">
      <div class="welcome-message">
        <h2>欢迎使用课程中心管理系统</h2>
        <p>{{ welcomeText }}</p>
      </div>
    </el-card>

    <!-- 调试信息卡片 -->
    <el-card shadow="hover" class="debug-card">
      <div slot="header" class="card-header">
        <span>当前用户信息（调试用）</span>
      </div>
      <div class="debug-info">
        <pre>{{ userInfoDebug }}</pre>
      </div>
    </el-card>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import {contentApi, courseApi, userApi} from "@/api";

export default {
  name: 'Dashboard',
  data() {
    return {
      userCount: 0,
      contentCount: 0,
      courseCount: 0
    }
  },
  computed: {
    ...mapState(['userInfo', 'userRole']),
    welcomeText() {
      const userRole = this.userRole
      if (userRole === 'admin') {
        return '您可以管理系统中的课程、分类、用户等所有内容。'
      } else if (userRole === 'teacher') {
        return '您可以发布课程、文章和视频，管理您的内容。'
      } else {
        return '欢迎使用本系统，您可以浏览和管理相关内容。'
      }
    },
    userInfoDebug() {
      return {
        从Vuex获取的用户信息: this.userInfo,
        从Vuex获取的用户角色: this.userRole,
        从localStorage获取的用户信息: JSON.parse(localStorage.getItem('userInfo') || '{}'),
        从localStorage获取的用户角色: localStorage.getItem('userRole') || '未设置'
      }
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      // 模拟获取数据
      this.userCount =await userApi.getUsers(1,10).then(res => {
        console.log('用户总数 '+res.data.data.list.length)
        return res.data.data.list.length
      })
      const videoTotal = await contentApi.getAllUserVideo(1,10).then(res => {
        console.log('视频总数 '+res.data.data.list.length)
        return res.data.data.list.length
      })
      this.contentCount = await contentApi.getAllUserArticle(1,10).then(res => {
        console.log('文章总数 '+res.data.data.list.length)
        return res.data.data.list.length+videoTotal
      })
        this.courseCount = await courseApi.getAllCourses(0,10).then(res => {
          console.log('课程总数 ' + res.data.data.list.length)
          return res.data.data.list.length
        })
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-card {
  height: 160px;
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 500;
}

.card-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-item {
  display: flex;
  align-items: center;
}

.card-icon {
  font-size: 40px;
  margin-right: 20px;
  color: #409EFF;
}

.item-info {
  text-align: center;
}

.item-value {
  font-size: 30px;
  font-weight: bold;
  color: #303133;
}

.item-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.welcome-card {
  margin-top: 20px;
}

.welcome-message {
  padding: 20px;
  text-align: center;
}

.welcome-message h2 {
  margin-bottom: 15px;
  color: #303133;
}

.welcome-message p {
  color: #606266;
  font-size: 16px;
}

.debug-card {
  margin-top: 20px;
}

.debug-info {
  padding: 20px;
}
</style>