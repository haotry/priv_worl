<template>
  <div class="category-container">
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openDialog()">添加分类</el-button>
    </div>

    <el-table
      :data="categoryList"
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
        label="分类名称"
        min-width="150">
      </el-table-column>
      <el-table-column
        prop="icon"
        label="图标"
        min-width="150">
        <template slot-scope="scope">
          <img v-if="scope.row.icon" :src="scope.row.icon" class="category-icon">
          <span v-else>无图标</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="createTime"
        label="上传时间"
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

    <!-- 添加/编辑分类弹窗 -->
    <el-dialog :title="dialogType === 'add' ? '添加分类' : '修改分类'" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称"></el-input>
        </el-form-item>
        <el-form-item label="分类图标" prop="icon">
          <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleIconSuccess"
            :before-upload="beforeIconUpload">
            <img v-if="form.icon" :src="form.icon" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">提 交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { categoryApi } from '@/api'

export default {
  name: 'CategoryManage',
  data() {
    return {
      loading: false,
      submitLoading: false,
      categoryList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      dialogType: 'add', // add or edit
      form: {
        id: null,
        name: '',
        icon: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入分类名称', trigger: 'blur' },
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
        const res = await categoryApi.getCategories(this.currentPage, this.pageSize)
        const { data } = res.data
        this.categoryList = data.records || []
        this.total = data.total || 0
      } catch (error) {
        this.$message.error('获取分类列表失败')
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
          icon: row.icon
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
        icon: ''
      }
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
    },
    handleIconSuccess(res, file) {
      if (res.code === 0) {
        this.form.icon = res.data
      } else {
        this.$message.error('上传失败')
      }
    },
    beforeIconUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        this.$message.error('上传图标只能是 JPG/PNG 格式!')
      }
      if (!isLt2M) {
        this.$message.error('上传图标大小不能超过 2MB!')
      }
      return isJPG && isLt2M
    },
    async submitForm() {
      this.$refs.form.validate(async valid => {
        if (!valid) return

        this.submitLoading = true
        try {
          if (this.dialogType === 'add') {
            await categoryApi.addCategory(this.form)
            this.$message.success('添加成功')
          } else {
            await categoryApi.updateCategory(this.form)
            this.$message.success('修改成功')
          }
          this.dialogVisible = false
          this.fetchData()
        } catch (error) {
          this.$message.error(this.dialogType === 'add' ? '添加失败' : '修改失败')
          console.error(error)
        } finally {
          this.submitLoading = false
        }
      })
    },
    async handleDelete(row) {
      this.$confirm(`确定要删除分类 "${row.name}" 吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await categoryApi.deleteCategory(row.id)
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
.category-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.category-icon {
  max-height: 40px;
  max-width: 40px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
}
</style>