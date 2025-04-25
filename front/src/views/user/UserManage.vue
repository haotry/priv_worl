<template>
  <div class="user-manage">
    <div class="filter-container">
<!--      <el-input v-model="listQuery.username" placeholder="用户名" style="width: 200px;" class="filter-item" @keyup.enter.native="handleFilter" />
      <el-input v-model="listQuery.phone" placeholder="手机号" style="width: 200px;" class="filter-item" @keyup.enter.native="handleFilter" />-->
      <el-select v-model="listQuery.role" placeholder="角色" clearable style="width: 120px" class="filter-item">
        <el-option v-for="item in roleOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
      <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">
        搜索
      </el-button>
<!--      <el-button class="filter-item" style="margin-left: 10px;" type="primary" icon="el-icon-plus" @click="handleCreate">
        添加用户
      </el-button>-->
    </div>

    <el-table
      :key="tableKey"
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column label="ID" prop="id" align="center" width="80">
        <template slot-scope="{row}">
          <span>{{ row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column label="学号/工号" min-width="120px">
        <template slot-scope="{row}">
          <span>{{ row.jwcode }}</span>
        </template>
      </el-table-column>
      <el-table-column label="姓名" min-width="120px">
        <template slot-scope="{row}">
          <span>{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="手机号" min-width="120px">
        <template slot-scope="{row}">
          <span>{{ row.tel }}</span>
        </template>
      </el-table-column>
      <el-table-column label="角色" min-width="100px">
        <template slot-scope="{row}">
          <el-tag type="danger" v-if="row.user_identity === 0">管理员</el-tag>
          <el-tag type="success" v-else-if="row.user_identity === 2">讲师</el-tag>
          <el-tag type="info" v-else>普通用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="积分" align="center" width="100">
        <template slot-scope="{row}">
          <span>{{ row.credit }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="150px" align="center" prop="create_time">
        <template slot-scope="{row}">
          <span>{{ row.create_time | formatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button v-if="row.user_identity !== 0" type="primary" size="mini" @click="handleUpdate(row)">
            修改角色
          </el-button>
<!--          <el-button v-if="row.user_identity !== 0" size="mini" type="danger" @click="handleDelete(row)">
            删除
          </el-button>-->
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container" v-show="total>0">
      <el-pagination
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="listQuery.page"
        :page-sizes="[10, 20, 30, 50]"
        :page-size="listQuery.limit"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>

    <!-- 用户修改角色对话框 -->
    <el-dialog title="修改用户角色" :visible.sync="dialogFormVisible">
      <el-form ref="dataForm" :rules="rules" :model="temp" label-position="left" label-width="100px" style="width: 400px; margin-left:50px;">
        <el-form-item label="学号/工号">
          <el-input v-model="temp.jwcode" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="temp.name" disabled />
        </el-form-item>
        <el-form-item label="当前角色">
          <el-tag type="danger" v-if="temp.user_identity === 0">管理员</el-tag>
          <el-tag type="success" v-else-if="temp.user_identity === 2">讲师</el-tag>
          <el-tag type="info" v-else>普通用户</el-tag>
        </el-form-item>
        <el-form-item label="新角色" prop="newRole">
          <el-select v-model="temp.newRole" placeholder="请选择角色">
            <el-option label="管理员" :value=0></el-option>
            <el-option label="普通用户" :value=1></el-option>
            <el-option label="讲师" :value=2></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="updateData()">
          确认
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, updateUserRole } from '@/api/user'

export default {
  name: 'UserManage',
  filters: {
    formatDate(time) {
      if (time) {
        const date = new Date(time)
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
      }
      return ''
    }
  },
  data() {
    return {
      tableKey: 0,
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 10,
        username: '',
        phone: '',
        role: ''
      },
      roleOptions: [
        { key: 'admin', display_name: '管理员' },
        { key: 'teacher', display_name: '讲师' },
        { key: 'user', display_name: '普通用户' }
      ],
      temp: {
        id: 0,
        jwcode: '',
        name: '',
        tel: '',
        user_identity: 1,
        newRole: null
      },
      dialogFormVisible: false,
      dialogStatus: '',
      rules: {
        newRole: [{ required: true, message: '请选择新角色', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.listLoading = true
      const params = {
        userIdentity: this.listQuery.role === 'admin' ? 0 : this.listQuery.role === 'teacher' ? 2 : this.listQuery.role === 'user' ? 1 : undefined,
        page: this.listQuery.page,
        pageSize: this.listQuery.limit
      }

      getUserList(params).then(response => {
        const { data } = response.data
        this.list = data.list
        this.total = data.total
        this.listLoading = false
      }).catch(error => {
        console.error('获取用户列表失败:', error)
        this.listLoading = false
        this.$message.error('获取用户列表失败')
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleSizeChange(val) {
      this.listQuery.limit = val
      this.getList()
    },
    handleCurrentChange(val) {
      this.listQuery.page = val
      this.getList()
    },
    resetTemp() {
      this.temp = {
        id: 0,
        jwcode: '',
        name: '',
        tel: '',
        user_identity: 1,
        newRole: null
      }
    },
    handleCreate() {
      this.resetTemp()
      this.dialogStatus = 'create'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    handleUpdate(row) {
      this.temp = {
        id: row.id,
        jwcode: row.jwcode,
        name: row.name,
        user_identity: row.user_identity,
        newRole: row.user_identity // 初始化 newRole 为当前角色
      };
      this.temp.newRole = row.user_identity
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    updateData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          updateUserRole(this.temp.jwcode, this.temp.newRole).then(() => {
            this.dialogFormVisible = false
            this.$message.success('用户角色更新成功')
            this.getList()
          }).catch(error => {
            console.error('更新用户角色失败:', error)
            this.$message.error('更新用户角色失败')
          })
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该用户吗?', '警告', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.warning('删除用户功能未实现')
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.user-manage {
  padding: 20px;

  .filter-container {
    padding-bottom: 10px;
    .filter-item {
      display: inline-block;
      vertical-align: middle;
      margin-bottom: 10px;
      margin-right: 10px;
    }
  }

  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
}
</style>