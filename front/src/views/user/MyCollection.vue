<template>
  <div class="collection-container">
    <div class="page-header">
      <h2>我的收藏</h2>
    </div>

    <el-table
      :data="collectionList"
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
        label="标题"
        min-width="200">
        <template slot-scope="scope">
          <span v-if="scope.row.type === 'moment'">{{ formatContent(scope.row.content) }}</span>
          <span v-else>{{ scope.row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="type"
        label="类型"
        width="100">
        <template slot-scope="scope">
          <el-tag type="success" v-if="scope.row.type === 1">文章</el-tag>
          <el-tag type="warning" v-if="scope.row.type === 2">视频</el-tag>
          <el-tag type="info" v-if="scope.row.type === 3">动态</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="user.name"
        label="作者"
        width="120">
      </el-table-column>
      <el-table-column
        prop="time"
        label="收藏时间"
        width="180">
        <!-- <template slot-scope="scope">
          {{ formatDate(scope.row.time) }}
        </template> -->
      </el-table-column>
      <el-table-column
        label="操作"
        width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="viewContent(scope.row)">查看</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="cancelCollection(scope.row)">取消收藏</el-button>
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

    <!-- 内容预览弹窗 -->
    <el-dialog
      title="内容预览"
      :visible.sync="previewVisible"
      width="800px">
      <div v-if="currentContent">
        <h3>{{ currentContent.title }}</h3>
        <div v-if="currentContent.type === 'article'" class="content-preview">
          <div v-html="currentContent.content"></div>
        </div>
        <div v-else-if="currentContent.type === 'video'" class="video-preview">
          <video :src="currentContent.videoUrl" controls style="width: 100%"></video>
          <p>{{ currentContent.content }}</p>
        </div>
        <div v-else class="moment-preview">
          <p>{{ currentContent.content }}</p>
          <div v-if="currentContent.images" class="moment-images">
            <img v-for="(img, index) in currentContent.images.split(',')" :key="index" :src="img" class="moment-image">
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { contentApi } from '@/api'

export default {
  name: 'MyCollection',
  data() {
    return {
      loading: false,
      collectionList: [],
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
      contentApi.getCollections(this.currentPage, this.pageSize)
        .then(response => {
          const { data } = response.data
          this.collectionList = data.list || []
          this.total = data.total || 0
        })
        .catch(error => {
          this.$message.error('获取收藏列表失败：' + error.message)
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
      return content.length > 30 ? content.substring(0, 30) + '...' : content
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchData()
    },
    viewContent(row) {
      this.currentContent = row
      this.previewVisible = true
    },
    cancelCollection(row) {
      this.$confirm('确定取消收藏该内容吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        contentApi.collectContent(row.id, 0)
          .then(() => {
            this.$message.success('已取消收藏')
            this.fetchData()
          })
          .catch(error => {
            this.$message.error('操作失败：' + error.message)
          })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.collection-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
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

.moment-preview {
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
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