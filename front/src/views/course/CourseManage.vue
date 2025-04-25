<template>
  <div class="course-container">
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button type="primary" @click="addCourse">添加课程</el-button>
    </div>

    <el-table
      :data="courseList"
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
        prop="category"
        label="分类"
        width="120">
        <template slot-scope="scope">
          {{ getCategoryName(scope.row.menuId) }}
        </template>
      </el-table-column>
      <el-table-column
        prop="title"
        label="课程标题"
        min-width="200">
      </el-table-column>
      <el-table-column
        prop="teacher"
        label="讲师"
        width="120">
      </el-table-column>
      <el-table-column
        prop="credit"
        label="定价(积分)"
        width="120">
      </el-table-column>
      <el-table-column
        prop="status"
        label="上架状态"
        width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : (scope.row.status === 0 ? 'info' : 'danger')">
            {{ scope.row.status === 1 ? '已上架' : (scope.row.status === 0 ? '未上架' : '已拒绝') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        fixed="right"
        width="220">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="editCourse(scope.row)">编辑</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)">删除</el-button>
          <template v-if="scope.row.isTeacherCourse && scope.row.status === 0">
            <el-button
              size="mini"
              type="success"
              @click="handleApprove(scope.row)">通过</el-button>
            <el-button
              size="mini"
              type="warning"
              @click="handleReject(scope.row)">拒绝</el-button>
          </template>
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

    <!-- 拒绝原因弹窗 -->
    <el-dialog title="拒绝原因" :visible.sync="rejectDialogVisible" width="500px">
      <el-form :model="rejectForm">
        <el-form-item label="拒绝原因" label-width="100px">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入拒绝原因"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rejectDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmReject" :loading="submitLoading">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getLessonList } from '@/api/lesson'

export default {
  name: 'CourseManage',
  data() {
    return {
      loading: false,
      submitLoading: false,
      courseList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      categories: [],
      rejectDialogVisible: false,
      rejectForm: {
        id: null,
        reason: ''
      }
    }
  },
  created() {
    this.fetchData()
    this.fetchCategories()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        // 使用新的课程接口获取数据
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize,
          menuId: 0 // 0表示获取所有课程
        }

        const response = await getLessonList(params)
        const { data } = response.data
        this.courseList = data.records || []
        this.total = data.total || 0
      } catch (error) {
        this.$message.error('获取课程列表失败')
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    async fetchCategories() {
      try {
        // 暂时注释，需要实现分类接口
        // const res = await categoryApi.getCategories(1, 100)
        // const { data } = res.data
        // this.categories = data.records || []

        // 临时使用模拟数据
        this.categories = [
          { id: 1, name: '编程基础' },
          { id: 2, name: 'Web开发' },
          { id: 3, name: '移动开发' },
          { id: 4, name: '数据科学' },
          { id: 5, name: '人工智能' }
        ]
      } catch (error) {
        console.error('获取分类失败', error)
      }
    },
    getCategoryName(menuId) {
      const category = this.categories.find(c => c.id === menuId)
      return category ? category.name : '未分类'
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchData()
    },
    addCourse() {
      this.$router.push('/course-edit')
    },
    editCourse(row) {
      this.$router.push(`/course-edit/${row.id}`)
    },
    async handleDelete(row) {
      this.$confirm(`确定要删除课程 "${row.title}" 吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await courseApi.deleteCourse(row.id)
          this.$message.success('删除成功')
          this.fetchData()
        } catch (error) {
          this.$message.error('删除失败')
          console.error(error)
        }
      }).catch(() => {
        // 取消删除
      })
    },
    async handleApprove(row) {
      try {
        await courseApi.approveCourse(row.id)
        this.$message.success('课程审核通过')
        this.fetchData()
      } catch (error) {
        this.$message.error('操作失败')
        console.error(error)
      }
    },
    handleReject(row) {
      this.rejectForm.id = row.id
      this.rejectForm.reason = ''
      this.rejectDialogVisible = true
    },
    async confirmReject() {
      if (!this.rejectForm.reason) {
        this.$message.warning('请输入拒绝原因')
        return
      }

      this.submitLoading = true
      try {
        await courseApi.rejectCourse(this.rejectForm.id, this.rejectForm.reason)
        this.$message.success('已拒绝该课程')
        this.rejectDialogVisible = false
        this.fetchData()
      } catch (error) {
        this.$message.error('操作失败')
        console.error(error)
      } finally {
        this.submitLoading = false
      }
    }
  }
}
</script>

<style scoped>
.course-container {
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