<template>
  <div class="my-courses-container">
    <div class="page-header">
      <h2>课程管理</h2>
<!--      <el-button-->
<!--        v-if="isTeacher"-->
<!--        type="primary"-->
<!--        @click="publishCourse">发布新课程</el-button>-->
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabChange">
      <el-tab-pane label="已购课程" name="purchased">
        <el-table
            :data="purchasedCourses"
            v-loading="loading"
            border
            style="width: 100%">
          <el-table-column
              type="index"
              label="序号"
              width="60"
              align="center">
          </el-table-column>
          <el-table-column
              prop="lesson_name"
              label="课程标题"
              min-width="200">
          </el-table-column>
          <el-table-column
              prop="price"
              label="花费积分"
              width="100">
          </el-table-column>
          <el-table-column
              prop="create_time"
              label="购买时间"
              width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.create_time) }}
            </template>
          </el-table-column>
          <!--          <el-table-column
                      label="操作"
                      fixed="right"
                      width="100">
                      <template slot-scope="scope">
                        <el-button
                          size="mini"
                          type="primary"
                          @click="viewPurchasedCourse(scope.row)">学习</el-button>
                      </template>
                    </el-table-column>-->
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="isAdmin" label="已发布课程" name="published">
        <el-table
          :data="publishedCourses"
          v-loading="loading"
          border
          style="width: 100%">
          <el-table-column
            type="index"
            label="序号"
            width="60"
            align="center">
          </el-table-column>
          <el-table-column
            prop="title"
            label="课程标题"
            min-width="200">
          </el-table-column>
          <el-table-column
            prop="menu_id"
            label="分类"
            width="100">
            <template slot-scope="scope">
              {{ getCategoryName(scope.row.menu_id) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="price"
            label="价格(积分)"
            width="100">
          </el-table-column>
          <el-table-column
            prop="participant_count"
            label="学习人数"
            width="100">
          </el-table-column>
          <el-table-column
            prop="status"
            label="状态"
            width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : (scope.row.status === 0 ? 'info' : 'danger')">
                {{ scope.row.status === 1 ? '已上架' : (scope.row.status === 0 ? '审核中' : '已上架') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column

            label="发布时间"
            width="180" prop="create_time">
            <template slot-scope="scope">
              {{ formatDate(scope.row.create_time) }}
            </template>
          </el-table-column>
<!--          <el-table-column
            label="操作"
            fixed="right"
            width="150">
            <template slot-scope="scope">
&lt;!&ndash;              <el-button
                size="mini"
                type="primary"
                @click="viewCourse(scope.row)">查看</el-button>&ndash;&gt;
              <el-button
                size="mini"
                type="success"
                @click="editCourse(scope.row)">编辑</el-button>
            </template>
          </el-table-column>-->
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <div class="pagination">
      <el-pagination
        v-if="total > 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { getLessonList, getMyLessons } from '@/api/lesson'
import { mapState } from 'vuex'
import {courseApi} from "@/api";

export default {
  name: 'MyCourses',
  data() {
    return {
      activeTab: 'purchased',
      loading: false,
      publishedCourses: [],
      purchasedCourses: [],
      categories: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  computed: {
    ...mapState(['userInfo', 'userRole']),
    isAdmin() {
      return this.userRole === 'admin'
    }
  },
  created() {
    this.fetchCategories()
    this.fetchData()
  },
  methods: {
    async fetchCategories() {
      /*try {
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
      }*/
      this.categories=await courseApi.getCoursesMenu().then((res)=>{
        console.log('服务器返回的课程分类 '+res.data.data)
        return res.data.data
      })
    },
    getCategoryName(menuId) {
      const category = this.categories.find(c => c.id === menuId)
      return category ? category.name : '未分类'
    },
    async fetchData() {
      this.loading = true

      try {
        if (this.activeTab === 'published' && this.isAdmin) {
          // 获取教师发布的课程
          await this.fetchPublishedCourses()
        } else {
          // 获取已购课程
          await this.fetchPurchasedCourses()
        }
      } catch (error) {
        this.$message.error('获取课程数据失败')
        console.error(error)
      } finally {
        this.loading = false
      }
    },

    async fetchPublishedCourses() {
      try {
        // 使用新API获取教师发布的课程
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize
          // 可以添加筛选条件，如果API支持的话
        }

        const response = await getLessonList(params)
        const { data } = response.data
        this.publishedCourses = data.list || []
        this.total = data.total || 0
      } catch (error) {
        console.error('获取已发布课程失败', error)
        throw error
      }
    },

    async fetchPurchasedCourses() {
      try {
        // 使用新API获取普通用户购买的课程
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize
        }

        const response = await getMyLessons(params)
        const { data } = response.data
        console.log('已购课程数据 '+data.list)
        this.purchasedCourses = data.list || []
        this.total = data.total || 0
      } catch (error) {
        console.error('获取已购课程失败', error)
        throw error
      }
    },
    handleTabChange(tab) {
      this.currentPage = 1
      this.fetchData()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchData()
    },
    publishCourse() {
      this.$router.push('/publish-course')
    },
    editCourse(row) {
      this.$router.push(`/course-edit/${row.id}`)
    },
    viewCourse(row) {
      // 跳转到课程详情页
      window.open(`/#/course/${row.id}`)
    },
    viewPurchasedCourse(row) {
      // 跳转到课程学习页面
      window.open(`/#/course/${row.lessonId}`)
    },
    formatDate(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.my-courses-container {
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