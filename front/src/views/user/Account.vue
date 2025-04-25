<template>
  <div class="account-container">
    <div class="page-header">
      <h2>账号信息</h2>
    </div>

    <el-form :model="form" :rules="rules" ref="form" label-width="100px" class="account-form">
      <el-form-item label="学号" prop="jwcode">
        <el-input v-model="form.jwcode" disabled></el-input>
      </el-form-item>

      <el-form-item label="用户名" prop="name">
        <el-input v-model="form.name" placeholder="请输入用户名"></el-input>
      </el-form-item>

      <el-form-item label="手机号" prop="tel">
        <el-input v-model="form.tel" disabled></el-input>
      </el-form-item>

      <el-form-item label="头像">
        <div class="avatar-container">
          <img v-if="form.avatar" :src="form.avatar" class="avatar">
          <div v-else class="avatar-placeholder">
            <i class="el-icon-user-solid"></i>
          </div>
          <div class="avatar-actions">
            <el-button type="primary" size="small" @click="$refs.avatarInput.click()">
              {{ form.avatar ? '更换头像' : '上传头像' }}
            </el-button>
            <el-button v-if="form.avatar" type="danger" size="small" @click="removeAvatar">
              清除头像
            </el-button>
          </div>
          <input
            type="file"
            ref="avatarInput"
            accept="image/*"
            style="display: none"
            @change="handleAvatarSelect"
          />
        </div>
        <div class="upload-tip">支持JPG、PNG格式，大小不超过2MB</div>
      </el-form-item>

      <el-form-item label="当前积分">
        <el-input v-model="form.credit" disabled></el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="updateProfile" :loading="submitLoading">保存修改</el-button>
      </el-form-item>
    </el-form>

    <div class="divider"></div>

    <div class="password-section">
      <h3>修改密码</h3>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px" class="password-form">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码"></el-input>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码"></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="updatePassword" :loading="passwordLoading">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { userApi } from '@/api'
import { mapState } from 'vuex'
import axios from 'axios'

export default {
  name: 'Account',
  data() {
    // 确认密码验证
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    return {
      form: {
        jwcode: '',
        name: '',
        tel: '',
        avatar: '',
        credit: 0
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      submitLoading: false,
      passwordLoading: false,
      uploadLoading: false,
      avatarFile: null,
      rules: {
        name: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ]
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入旧密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapState(['userInfo'])

  },
  created() {
    this.loadUserInfo()
  },
  methods: {
    loadUserInfo() {
      if (this.userInfo) {
        this.form = {
          jwcode: this.userInfo.jwcode || '',
          name: this.userInfo.name || '',
          tel: this.userInfo.tel || '',
          avatar: this.userInfo.avatar || '',
          credit: this.userInfo.credit || 0
        }
      } else {
        this.$store.dispatch('getUserInfo').then(data => {
          this.form = {
            jwcode: data.jwcode || '',
            name: data.name || '',
            tel: data.tel || '',
            avatar: data.avatar || '',
            credit: data.credit || 0
          }
        }).catch(() => {
          this.$message.error('获取用户信息失败')
        })
      }
      console.log("账号信息userInfo", this.userInfo);
    },

    handleAvatarSelect(e) {
      const file = e.target.files[0]
      if (!file) return

      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('上传头像图片只能是图片格式!')
        this.$refs.avatarInput.value = ''
        return
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!')
        this.$refs.avatarInput.value = ''
        return
      }

      // 创建本地预览URL
      const url = URL.createObjectURL(file)
      this.form.avatar = url
      this.avatarFile = file
      this.$refs.avatarInput.value = ''
    },

    removeAvatar() {
      this.$confirm('确定要清除头像吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (this.form.avatar && this.form.avatar.startsWith('blob:')) {
          URL.revokeObjectURL(this.form.avatar) // 释放URL对象
        }
        this.form.avatar = ''
        this.avatarFile = null
        this.$message.success('已清除头像')
      }).catch(() => {})
    },

    updateProfile() {
      this.$refs.form.validate(valid => {
        if (!valid) return

        this.submitLoading = true

        // 创建一个Promise链
        let updatePromise = Promise.resolve()

        // 首先更新用户名
        updatePromise = updatePromise.then(() => {
          return userApi.updateUserName(this.form.jwcode, this.form.name)
        })

        // 如果有新头像文件，先上传头像
        if (this.avatarFile) {
          updatePromise = updatePromise.then(() => {
            // 创建FormData对象上传文件
            const formData = new FormData()
            formData.append('file', this.avatarFile)

            return axios.post('/api/upload', formData, {
              headers: {
                'Content-Type': 'multipart/form-data'
              }
            }).then(response => {
              if (response.data.code === 0) {
                // 获取服务器返回的URL，替换本地URL
                const serverUrl = response.data.data
                return userApi.updateUserAvatar(this.form.jwcode, serverUrl)
              } else {
                return Promise.reject(new Error(response.data.msg || '头像上传失败'))
              }
            })
          })
        } else if (this.form.avatar !== this.userInfo.avatar) {
          // 如果头像改变但没有新文件(可能是已删除头像)
          updatePromise = updatePromise.then(() => {
            return userApi.updateUserAvatar(this.form.jwcode, this.form.avatar)
          })
        }

        // 完成所有更新后
        updatePromise.then(() => {
          this.$message.success('个人信息修改成功')
          this.$store.dispatch('getUserInfo')
          // 清除本地头像文件和blob URL
          if (this.form.avatar && this.form.avatar.startsWith('blob:')) {
            URL.revokeObjectURL(this.form.avatar)
          }
          this.avatarFile = null
        })
        .catch(error => {
          this.$message.error('个人信息修改失败: ' + error.message)
        })
        .finally(() => {
          this.submitLoading = false
        })
      })
    },

    updatePassword() {
      this.$refs.passwordForm.validate(valid => {
        if (!valid) return

        this.passwordLoading = true
        userApi.updatePassword(this.form.jwcode, this.passwordForm.oldPassword, this.passwordForm.newPassword)
          .then(() => {
            this.$message.success('密码修改成功')
            this.passwordForm = {
              oldPassword: '',
              newPassword: '',
              confirmPassword: ''
            }
            this.$refs.passwordForm.resetFields()
          })
          .catch(error => {
            this.$message.error('密码修改失败: ' + error.message)
          })
          .finally(() => {
            this.passwordLoading = false
          })
      })
    }
  }
}
</script>

<style scoped>
.account-container {
  padding: 20px;
  max-width: 800px;
}

.page-header {
  margin-bottom: 20px;
}

.account-form, .password-form {
  max-width: 500px;
}

.avatar-container {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.avatar, .avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  margin-right: 20px;
}

.avatar-placeholder {
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder i {
  font-size: 40px;
  color: #909399;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.divider {
  margin: 30px 0;
  border-top: 1px solid #eee;
}

.password-section h3 {
  margin-bottom: 20px;
}
</style>