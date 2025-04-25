<template>
  <div class="position-container">
    <div class="page-header">
      <h2>位置管理</h2>
      <el-button type="primary" @click="openDialog()">创建位置</el-button>
    </div>

    <el-table
      :data="positionList"
      border
      v-loading="loading"
      style="width: 100%">
      <el-table-column
        type="index"
        label="序号"
        width="60"
        align="center">
      </el-table-column>
      <el-table-column
        prop="name"
        label="位置名称"
        min-width="150">
      </el-table-column>
      <el-table-column
        prop="courseCount"
        label="关联课程"
        min-width="100">
        <template slot-scope="scope">
          {{ scope.row.courseCount || 0 }} 个课程
        </template>
      </el-table-column>
      <el-table-column
        prop="status"
        label="显示状态"
        min-width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status ? 'success' : 'info'">
            {{ scope.row.status ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="createTime"
        label="创建时间"
        min-width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        fixed="right"
        width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="openDialog(scope.row)">修改</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      class="pagination">
    </el-pagination>

    <!-- 添加/编辑位置弹窗 -->
    <el-dialog :title="dialogType === 'add' ? '创建位置' : '修改位置'" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="位置名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入位置名称，如：重磅上新"></el-input>
        </el-form-item>
        <el-form-item label="显示状态" prop="status">
          <el-switch
            v-model="form.status"
            active-text="显示"
            inactive-text="隐藏">
          </el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { positionApi } from '@/api'

export default {
  name: 'PositionManage',
  data() {
    return {
      loading: false,
      submitLoading: false,
      positionList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      dialogType: 'add', // add or edit
      form: {
        id: null,
        name: '',
        status: true
      },
      rules: {
        name: [
          { required: true, message: '请输入位置名称', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await positionApi.getPositions(this.currentPage, this.pageSize)
        const { data } = res.data
        this.positionList = data.records || []
        this.total = data.total || 0
      } catch (error) {
        this.$message.error('获取位置列表失败')
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    formatDate(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchData()
    },
    openDialog(row) {
      this.resetForm()
      if (row) {
        this.dialogType = 'edit'
        this.form = {
          id: row.id,
          name: row.name,
          status: row.status
        }
      } else {
        this.dialogType = 'add'
      }
      this.dialogVisible = true
    },
    resetForm() {
      this.form = {
        id: null,
        name: '',
        status: true
      }
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
    },
    async submitForm() {
      this.$refs.form.validate(async valid => {
        if (!valid) return

        this.submitLoading = true
        try {
          if (this.dialogType === 'add') {
            await positionApi.addPosition(this.form)
            this.$message.success('创建成功')
          } else {
            await positionApi.updatePosition(this.form)
            this.$message.success('修改成功')
          }
          this.dialogVisible = false
          this.fetchData()
        } catch (error) {
          this.$message.error(this.dialogType === 'add' ? '创建失败' : '修改失败')
          console.error(error)
        } finally {
          this.submitLoading = false
        }
      })
    },
    async handleDelete(row) {
      this.$confirm(`确定要删除位置 "${row.name}" 吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await positionApi.deletePosition(row.id)
          this.$message.success('删除成功')
          this.fetchData()
        } catch (error) {
          this.$message.error('删除失败')
          console.error(error)
        }
      }).catch(() => {
        // 取消删除
      })
    }
  }
}
</script>

<style scoped>
.position-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>