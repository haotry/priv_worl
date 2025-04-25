<template>
  <div class="my-comment-container">
    <div class="page-header">
      <h2>我的评论</h2>
    </div>

    <el-table
      :data="commentList"
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
        prop="content"
        label="评论内容"
        min-width="250">
        <template slot-scope="scope">
          {{ formatContent(scope.row.content) }}
        </template>
      </el-table-column>
      <el-table-column
        prop="contentTitle"
        label="内容标题"
        min-width="150">
        <template slot-scope="scope">
          {{ scope.row.content_title || '未知内容' }}
        </template>
      </el-table-column>
      <el-table-column
        prop="author"
        label="内容作者"
        width="120">
        <template slot-scope="scope">
          {{ scope.row.author || '未知作者' }}
        </template>
      </el-table-column>
      <el-table-column
        prop="content_type"
        label="内容类型"
        width="100">
        <template slot-scope="scope">
          <el-tag type="success" v-if="scope.row.content_type === 1">文章</el-tag>
          <el-tag type="warning" v-if="scope.row.content_type === 2">视频</el-tag>
          <el-tag type="info" v-if="scope.row.content_type === 3">动态</el-tag>
          <el-tag v-if="scope.row.content_type === 0 || scope.row.content_type === null">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="time"
        label="评论时间"
        width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.time) }}
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="viewTargetContent(scope.row)">查看源内容</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="deleteComment(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>

    <!-- 源内容预览弹窗 -->
    <el-dialog
      title="源内容预览"
      :visible.sync="previewVisible"
      width="800px">
      <div v-if="currentContent">
        <h3>{{ currentContent.title }}</h3>
        <p class="content-author">作者: {{ currentContent.author || '未知作者' }}</p>
        <div v-if="currentContent.type === 'article'" class="content-preview">
          <div v-html="currentContent.content"></div>
        </div>
        <div v-else-if="currentContent.type === 'video'" class="video-preview">
          <video :src="currentContent.videoUrl" controls style="width: 100%"></video>
          <p>{{ currentContent.content }}</p>
        </div>
        <div v-else-if="currentContent.type === 'moment'" class="moment-preview">
          <p>{{ currentContent.content }}</p>
          <div v-if="currentContent.images" class="moment-images">
            <img v-for="(img, index) in currentContent.images.split(',')" :key="index" :src="img" class="moment-image">
          </div>
        </div>
        <div v-else class="unknown-content-preview">
          <el-alert
            title="内容不存在"
            type="info"
            description="该内容可能已被删除或无法访问"
            show-icon>
          </el-alert>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { commentApi, contentApi } from '@/api'

export default {
  name: 'MyComment',
  data() {
    return {
      loading: false,
      commentList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      previewVisible: false,
      currentContent: null
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.loading = true
      // 使用新创建的获取当前用户评论列表的API
      commentApi.getMyComments(this.currentPage, this.pageSize)
        .then(response => {
          const { data } = response.data
          this.commentList = data.list || []
          this.total = data.total || 0
        })
        .catch(error => {
          this.$message.error('获取评论列表失败：' + error.message)
        })
        .finally(() => {
          this.loading = false
        })
    },
    formatDate(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    formatContent(content) {
      if (!content) return ''
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
    viewTargetContent(row) {
      // 处理未知内容类型
      if (row.content_type === 0 || row.content_type === null) {
        this.currentContent = {
          title: row.content_title || '未知内容',
          content: '该内容不存在或已被删除',
          type: 'unknown',
          author: row.author || '未知作者'
        };
        this.previewVisible = true;
        return;
      }

      // 获取评论所在的源内容
      contentApi.getContentDetail(this.$store.state.userInfo.jwcode,row.content_id)
        .then(response => {
          const { data } = response.data;
          this.currentContent = {
            ...data,
            type: row.content_type === 1 ? 'article' : row.content_type === 2 ? 'video' : 'moment',
            author: row.author || '未知作者'
          };
          this.previewVisible = true;
        })
        .catch(error => {
          this.$message.error('获取内容详情失败：' + error.message);
        });
    },
    deleteComment(row) {
      this.$confirm('确定删除此评论吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        commentApi.deleteComment(row.id)
          .then(() => {
            this.$message.success('删除成功')
            this.fetchData()
          })
          .catch(error => {
            this.$message.error('删除失败：' + error.message)
          })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.my-comment-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.content-author {
  color: #606266;
  font-size: 14px;
  margin-top: -10px;
  margin-bottom: 15px;
}

.content-preview {
  max-height: 500px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.video-preview {
  max-height: 600px;
  overflow-y: auto;
}

.moment-preview, .unknown-content-preview {
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.unknown-content-preview {
  margin-top: 10px;
}

.moment-images {
  display: flex;
  flex-wrap: wrap;
  margin-top: 10px;
}

.moment-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  margin-right: 10px;
  margin-bottom: 10px;
  border-radius: 4px;
}
</style>