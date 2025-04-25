<template>
  <div class="relation-container">
    <div class="page-header">
      <h2>课程关联</h2>
      <el-button type="primary" @click="openDialog()">关联课程</el-button>
    </div>

    <el-table
      :data="relationList"
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
        prop="positionName"
        label="关联位置"
        min-width="150">
      </el-table-column>
      <el-table-column
        prop="courseName"
        label="课程名称"
        min-width="200">
      </el-table-column>
      <el-table-column
        prop="status"
        label="显示状态"
        width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status ? 'success' : 'info'">
            {{ scope.row.status ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="sort"
        label="排序值"
        width="80">
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

    <!-- 添加/编辑关联弹窗 -->
    <el-dialog :title="dialogType === 'add' ? '关联课程' : '编辑课程关联'" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="选择位置" prop="positionId">
          <el-select v-model="form.positionId" placeholder="请选择位置" style="width: 100%">
            <el-option
              v-for="item in positionOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关联课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="item in courseOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="显示状态" prop="status">
          <el-switch
            v-model="form.status"
            active-text="显示"
            inactive-text="隐藏">
          </el-switch>
        </el-form-item>
        <el-form-item label="排序值" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" label="排序值"></el-input-number>
          <div class="form-tip">数值越小排序越靠前</div>
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
import { relationApi, positionApi, courseApi } from '@/api'

export default {
  name: 'CourseRelation',
  data() {
    return {
      loading: false,
      submitLoading: false,
      relationList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      dialogType: 'add', // add or edit
      positionOptions: [],
      courseOptions: [],
      form: {
        id: null,
        positionId: '',
        courseId: '',
        status: true,
        sort: 0
      },
      rules: {
        positionId: [
          { required: true, message: '请选择位置', trigger: 'change' }
        ],
        courseId: [
          { required: true, message: '请选择课程', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.fetchData()
    this.fetchPositions()
    this.fetchCourses()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        // 此处使用 type=1 假设 1 代表所有推荐位置类型
        const res = await relationApi.getRelations(1, this.currentPage, this.pageSize)
        const { data } = res.data
        this.relationList = data.records || []
        this.total = data.total || 0
      } catch (error) {
        this.$message.error('获取课程关联列表失败')
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    async fetchPositions() {
      try {
        const res = await positionApi.getPositions(1, 100)
        const { data } = res.data
        this.positionOptions = data.records || []
      } catch (error) {
        this.$message.error('获取位置列表失败')
        console.error(error)
      }
    },
    async fetchCourses() {
      try {
        // 此处使用一个通用的分类ID获取所有课程
        const res = await courseApi.getCourses(0, 1, 100)
        const { data } = res.data
        this.courseOptions = data.records || []
      } catch (error) {
        this.$message.error('获取课程列表失败')
        console.error(error)
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
          positionId: row.positionId,
          courseId: row.courseId,
          status: row.status,
          sort: row.sort || 0
        }
      } else {
        this.dialogType = 'add'
      }
      this.dialogVisible = true
    },
    resetForm() {
      this.form = {
        id: null,
        positionId: '',
        courseId: '',
        status: true,
        sort: 0
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
            await relationApi.addRelation(this.form)
            this.$message.success('关联成功')
          } else {
            await relationApi.updateRelation(this.form)
            this.$message.success('修改成功')
          }
          this.dialogVisible = false
          this.fetchData()
        } catch (error) {
          this.$message.error(this.dialogType === 'add' ? '关联失败' : '修改失败')
          console.error(error)
        } finally {
          this.submitLoading = false
        }
      })
    },
    async handleDelete(row) {
      this.$confirm(`确定要删除此课程关联吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await relationApi.deleteRelation(row.id)
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
.relation-container {
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>