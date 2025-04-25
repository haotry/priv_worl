<template>
  <div class="login-container">
    <div class="login-form">
      <h2>弘历投教后台管理系统</h2>
      <el-tabs v-model="loginType">
        <el-tab-pane label="学号登录" name="jwcode">
          <el-form ref="jwcodeForm" :model="jwcodeForm" :rules="jwcodeRules" class="form">
            <el-form-item prop="jwcode">
              <el-input v-model="jwcodeForm.jwcode" placeholder="请输入学号" prefix-icon="el-icon-user"></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="jwcodeForm.password" placeholder="请输入密码" prefix-icon="el-icon-lock" show-password></el-input>
            </el-form-item>

            <el-form-item label="选择角色" prop="userIdentity">
              <el-radio-group v-model="jwcodeForm.userIdentity">
                <el-radio :label="0">管理员</el-radio>
                <el-radio :label="1">普通用户</el-radio>
                <el-radio :label="2">讲师</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-button type="primary" :loading="loading" @click="handleLogin('jwcodeForm')" class="login-button">登录</el-button>
            <el-button v-if="showResetStorage" @click="clearAllStorage" type="danger" plain class="reset-button">清除浏览器缓存</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="手机号登录" name="phone">
          <el-form ref="phoneForm" :model="phoneForm" :rules="phoneRules" class="form">
            <el-form-item prop="phone">
              <el-input v-model="phoneForm.phone" placeholder="请输入手机号" prefix-icon="el-icon-mobile-phone"></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="phoneForm.password" placeholder="请输入密码" prefix-icon="el-icon-lock" show-password></el-input>
            </el-form-item>

            <el-form-item label="选择角色" prop="userIdentity">
              <el-radio-group v-model="phoneForm.userIdentity">
                <el-radio :label="0">管理员</el-radio>
                <el-radio :label="1">普通用户</el-radio>
                <el-radio :label="2">讲师</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-button type="primary" :loading="loading" @click="handleLogin('phoneForm')" class="login-button">登录</el-button>
            <el-button v-if="showResetStorage" @click="clearAllStorage" type="danger" plain class="reset-button">清除浏览器缓存</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loginType: 'jwcode',
      loading: false,
      showResetStorage: false,
      jwcodeForm: {
        jwcode: '',
        password: '',
        userIdentity: 1 // 默认为普通用户
      },
      phoneForm: {
        phone: '',
        password: '',
        userIdentity: 1 // 默认为普通用户
      },
      jwcodeRules: {
        jwcode: [
          { required: true, message: '请输入学号', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ],
        userIdentity: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ]
      },
      phoneRules: {
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ],
        userIdentity: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    // 检查URL参数中是否有clear=1，如果有则清除所有存储
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('clear') === '1') {
      this.clearAllStorage();
      // 移除URL参数
      window.history.replaceState({}, document.title, window.location.pathname);
    }

    // 组件创建时就清理一次存储，避免可能的问题
    this.clearBrowserData();
  },
  methods: {
    // 清除所有浏览器存储
    clearAllStorage() {
      // 清除localStorage
      localStorage.clear();

      // 清除sessionStorage
      sessionStorage.clear();

      // 清除所有cookie
      document.cookie.split(';').forEach(cookie => {
        const [name] = cookie.trim().split('=');
        document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
      });

      this.$message.success('浏览器缓存已清除，请重新登录');
    },

    // 仅清除认证相关数据
    clearBrowserData() {
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      localStorage.removeItem('userRole');
      // 清除JWT相关cookie
      document.cookie = 'Authorization=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    },

    handleLogin(formName) {
      this.$refs[formName].validate(async valid => {
        if (valid) {
          this.loading = true;
          // 每次登录前清除之前的数据
          this.clearBrowserData();

          try {
            const form = this.loginType === 'jwcode' ? this.jwcodeForm : this.phoneForm;

            // 添加用户角色到登录请求中
            const result = await this.$store.dispatch('login', form);
            this.$message.success('登录成功');

            // 根据角色设置不同的跳转
            const role = localStorage.getItem('userRole');
            let redirectPath = '/';
            if (role === 'admin') {
              redirectPath = '/dashboard'; // 管理员跳转到控制台
            } else if (role === 'teacher') {
              redirectPath = '/my-content'; // 教师跳转到我的内容
            } else {
              redirectPath = '/my-courses'; // 普通用户跳转到我的课程页面
            }

            // 确保在状态更新后再跳转
            this.$nextTick(() => {
              this.$router.replace(redirectPath);
            });
          } catch (error) {
            let errorMsg = '登录失败';
            this.showResetStorage = true; // 显示清除缓存按钮

            if (error.response) {
              const status = error.response.status;

              if (status === 431) {
                errorMsg = '请求头过大，请点击"清除浏览器缓存"按钮后重试';
              } else {
                errorMsg += ': ' + (error.response.data?.message || `状态码 ${status}`);
              }
            } else if (error.message) {
              errorMsg = error.message;
            }

            this.$message.error(errorMsg);
          } finally {
            this.loading = false;
          }
        }
      });
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f3f3f3;
}

.login-form {
  width: 400px;
  padding: 30px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-form h2 {
  text-align: center;
  margin-bottom: 30px;
  font-weight: 500;
  color: #303133;
}

.form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
  margin-top: 20px;
}

.reset-button {
  width: 100%;
  margin-top: 10px;
}
</style>