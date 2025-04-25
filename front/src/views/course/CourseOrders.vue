<template>
  <div class="orders-container">
    <div class="page-header">
      <h2>课程订单</h2>
    </div>

    <el-table
      :data="orderList"
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
        prop="courseName"
        label="课程标题"
        min-width="250">
      </el-table-column>
      <el-table-column
        prop="userName"
        label="客户"
        width="120">
      </el-table-column>
      <el-table-column
        prop="userJwcode"
        label="精网号"
        width="120">
      </el-table-column>
      <el-table-column
        prop="createTime"
        label="交易时间"
        min-width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column
        prop="credit"
        label="订单金额"
        width="100">
        <template slot-scope="scope">
          {{ scope.row.credit }} 积分
        </template>
      </el-table-column>
      <el-table-column
        prop="teacherName"
        label="讲师名称"
        width="120">
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
  </div>
</template>

<script>
import { courseApi } from '@/api'

export default {
  name: 'CourseOrders',
  data() {
    return {
      loading: false,
      orderList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await courseApi.getOrders(this.currentPage, this.pageSize)
        const { data } = res.data
        this.orderList = data.records || []
        this.total = data.total || 0
      } catch (error) {
        this.$message.error('获取订单列表失败')
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
    }
  }
}
</script>

<style scoped>
.orders-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>