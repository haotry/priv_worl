<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script>
export default {
  name: 'App',
  created() {
    // 检查localStorage中是否有用户信息和token
    const token = localStorage.getItem('token')
    const userInfoStr = localStorage.getItem('userInfo')

    console.log('App启动，检查localStorage状态', { hasToken: !!token, hasUserInfo: !!userInfoStr })

    // 如果本地存储有token但Vuex中没有用户信息
    if (token && !this.$store.state.userInfo && userInfoStr) {
      try {
        // 尝试从本地存储恢复用户信息
        console.log('从localStorage恢复用户信息')
        const userInfo = JSON.parse(userInfoStr)
        console.log('从localStorage解析的用户信息:', userInfo)
        this.$store.commit('SET_USER_INFO', userInfo)
        console.log('恢复后的Vuex userInfo:', this.$store.state.userInfo)
      } catch(e) {
        console.error('解析存储的用户信息失败:', e)
        // 如果解析失败，尝试从服务器获取
        this.fetchUserInfo()
      }
    } else if (token && !this.$store.state.userInfo) {
      // 如果有token但没有用户信息，尝试从服务器获取
      this.fetchUserInfo()
    }
  },
  methods: {
    fetchUserInfo() {
      console.log('App组件从服务器获取用户信息')
      this.$store.dispatch('getUserInfo').catch(error => {
        console.error('获取用户信息失败，可能需要重新登录:', error)
      })
    }
  }
}
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
}

#app {
  height: 100%;
}

.container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 22px;
  color: #303133;
}
</style>