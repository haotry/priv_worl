<template>
  <div class="comment-container">
    <div class="page-header">
      <h2>评论管理</h2>
    </div>

    <el-table
      :data="commentList"
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
        prop="id"
        label="ID"
        width="80">
      </el-table-column>
      <el-table-column
        prop="contentId"
        label="内容ID"
        width="100">
      </el-table-column>
      <el-table-column
        prop="userId"
        label="用户ID"
        width="100">
      </el-table-column>
      <el-table-column
        prop="userName"
        label="用户名"
        width="120">
      </el-table-column>
      <el-table-column
        prop="content"
        label="评论内容"
        min-width="250">
        <template slot-scope="scope">
          {{ formatContent(scope.row.content) }}
        </template>
      </el-table-column>
      <el-table-column
        prop="parentId"
        label="父评论ID"
        width="100">
        <template slot-scope="scope">
          {{ scope.row.parentId || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        prop="replyUserId"
        label="回复用户ID"
        width="100">
        <template slot-scope="scope">
          {{ scope.row.replyUserId || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        prop="likeCount"
        label="点赞数"
        width="80">
      </el-table-column>
      <el-table-column
        prop="createTime"
        label="发布时间"
        width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        fixed="right"
        width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)">删除</el-button>
          <el-button
            size="mini"
            type="primary"
            @click="viewContent(scope.row)">查看内容</el-button>
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

    <!-- 评论内容详情弹窗 -->
    <el-dialog title="评论详情" :visible.sync="detailDialogVisible" width="600px">
      <div v-if="currentComment">
        <p><strong>评论ID：</strong>{{ currentComment.id }}</p>
        <p><strong>内容ID：</strong>{{ currentComment.contentId }}</p>
        <p><strong>评论者：</strong>{{ currentComment.userName }} (ID: {{ currentComment.userId }})</p>
        <p v-if="currentComment.parentId"><strong>父评论ID：</strong>{{ currentComment.parentId }}</p>
        <p v-if="currentComment.replyUserId"><strong>回复用户ID：</strong>{{ currentComment.replyUserId }}</p>
        <p><strong>评论时间：</strong>{{ formatDate(currentComment.createTime) }}</p>
        <p><strong>评论内容：</strong></p>
        <div class="comment-content">{{ currentComment.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { commentApi } from '@/api'

export default {
  name: 'CommentManage',
  data() {
    return {
      loading: false,
      commentList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      detailDialogVisible: false,
      currentComment: null
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        // 这里假设后台提供了获取所有评论的接口
        // 实际使用时需要根据 API 进行调整，可能需要 contentId 参数
        const res = await commentApi.getAllComments(this.currentPage, this.pageSize)
        const { data } = res.data
        this.commentList = data.list || []
        this.total = data.total || 0
      } catch (error) {
        this.$message.error('获取评论列表失败')
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
    formatContent(content) {
      if (!content) return ''
      // 如果评论内容太长，截取前部分显示
      return content.length > 50 ? content.substring(0, 50) + '...' : content
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchData()
    },
    async handleDelete(row) {
      this.$confirm('确定要删除此评论吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await commentApi.deleteComment(row.id)
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
    viewContent(row) {
      this.currentComment = row
      this.detailDialogVisible = true
    }
  }
}
</script>

<style scoped>
.comment-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.comment-content {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-top: 10px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>