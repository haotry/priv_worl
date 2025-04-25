<template>
  <div class="user-manage">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>用户详情管理</span>
      </div>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="学号/工号">
          <el-input v-model="searchForm.jwcode" placeholder="请输入学号/工号"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchUser">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 用户信息卡片 -->
      <div v-if="currentUser.id" class="user-info-card">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="avatar-container">
              <el-avatar :size="100" :src="currentUser.avatar">
                <img src="https://lfjf.rzfwq.com/jtzy/Product/pcjingwang/images/userimg.png"/>
              </el-avatar>
            </div>
          </el-col>
          <el-col :span="18">
            <el-descriptions title="用户信息" :column="2" border>
              <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
              <el-descriptions-item label="用户名">{{ currentUser.name }}</el-descriptions-item>
              <el-descriptions-item label="学号/工号">{{ currentUser.jwcode || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
              <el-descriptions-item label="角色">
                <el-tag v-if="currentUser.role === 'admin'" type="danger">管理员</el-tag>
                <el-tag v-else-if="currentUser.role === 'teacher'" type="warning">教师</el-tag>
                <el-tag v-else type="success">学生</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="积分">{{ currentUser.credit || 0 }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ currentUser.createTime }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ currentUser.updateTime }}</el-descriptions-item>
            </el-descriptions>

            <div class="action-buttons">
              <el-button type="primary" @click="openEditDialog">编辑信息</el-button>
              <el-button type="warning" @click="openResetPasswordDialog">重置密码</el-button>
              <el-button type="danger" @click="confirmDeleteUser">删除用户</el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <div v-else-if="searched" class="no-user-found">
        <el-empty description="未找到用户信息"></el-empty>
      </div>
    </el-card>

    <!-- 编辑用户信息对话框 -->
    <el-dialog title="编辑用户信息" :visible.sync="editDialogVisible" width="40%">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="100px">
        <el-form-item label="用户名" prop="name">
          <el-input v-model="editForm.name"></el-input>
        </el-form-item>
        <el-form-item label="学号/工号" prop="jwcode">
          <el-input v-model="editForm.jwcode"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="admin"></el-option>
            <el-option label="教师" value="teacher"></el-option>
            <el-option label="学生" value="user"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="积分" prop="credit">
          <el-input-number v-model="editForm.credit" :min="0"></el-input-number>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitUserEdit">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog title="重置用户密码" :visible.sync="resetPasswordDialogVisible" width="30%">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="resetPasswordDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitPasswordReset">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'UserManage',
  data() {
    // 密码确认校验
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    return {
      searchForm: {
        jwcode: '',
        phone: ''
      },
      currentUser: {},
      searched: false,

      editDialogVisible: false,
      editForm: {
        id: '',
        name: '',
        jwcode: '',
        phone: '',
        role: '',
        credit: 0
      },
      editRules: {
        name: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ]
      },

      resetPasswordDialogVisible: false,
      passwordForm: {
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    searchUser() {
      const params = {}
      if (this.searchForm.jwcode) {
        params.jwcode = this.searchForm.jwcode
      }
      if (this.searchForm.phone) {
        params.phone = this.searchForm.phone
      }

      if (Object.keys(params).length === 0) {
        this.$message.warning('请至少输入一个搜索条件')
        return
      }

      axios.get('/api/admin/user', { params })
        .then(response => {
          this.currentUser = response.data
          this.searched = true
        })
        .catch(error => {
          console.error('搜索用户失败:', error)
          this.$message.error('搜索用户失败')
          this.currentUser = {}
          this.searched = true
        })
    },

    resetSearch() {
      this.searchForm.jwcode = ''
      this.searchForm.phone = ''
      this.currentUser = {}
      this.searched = false
    },

    openEditDialog() {
      this.editForm = {
        id: this.currentUser.id,
        name: this.currentUser.name,
        jwcode: this.currentUser.jwcode || '',
        phone: this.currentUser.phone,
        role: this.currentUser.role,
        credit: this.currentUser.credit || 0
      }
      this.editDialogVisible = true
    },

    submitUserEdit() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          axios.put(`/api/admin/users/${this.editForm.id}`, this.editForm)
            .then(() => {
              this.$message.success('用户信息更新成功')
              this.editDialogVisible = false
              this.searchUser() // 重新获取用户信息
            })
            .catch(error => {
              console.error('更新用户信息失败:', error)
              this.$message.error('更新用户信息失败')
            })
        }
      })
    },

    openResetPasswordDialog() {
      this.passwordForm.newPassword = ''
      this.passwordForm.confirmPassword = ''
      this.resetPasswordDialogVisible = true
    },

    submitPasswordReset() {
      this.$refs.passwordForm.validate(valid => {
        if (valid) {
          axios.put(`/api/admin/users/${this.currentUser.id}/password`, {
            password: this.passwordForm.newPassword
          })
            .then(() => {
              this.$message.success('密码重置成功')
              this.resetPasswordDialogVisible = false
            })
            .catch(error => {
              console.error('密码重置失败:', error)
              this.$message.error('密码重置失败')
            })
        }
      })
    },

    confirmDeleteUser() {
      this.$confirm('此操作将永久删除该用户, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete(`/api/admin/users/${this.currentUser.id}`)
          .then(() => {
            this.$message.success('删除用户成功')
            this.resetSearch()
          })
          .catch(error => {
            console.error('删除用户失败:', error)
            this.$message.error('删除用户失败')
          })
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    }
  }
}
</script>

<style scoped>
.user-manage {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
.user-info-card {
  margin-top: 20px;
}
.avatar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}
.action-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: flex-start;
  gap: 10px;
}
.no-user-found {
  margin-top: 40px;
  text-align: center;
}
</style>