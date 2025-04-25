<template>
  <div class="content-manage">
    <header>
      <h2>内容管理</h2>
    </header>

    <div class="content-filter">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="文章" name="article"></el-tab-pane>
        <el-tab-pane label="视频" name="video"></el-tab-pane>
        <el-tab-pane label="待审核" name="pending"></el-tab-pane>
        <el-tab-pane label="已拒绝" name="rejected"></el-tab-pane>
      </el-tabs>
    </div>

    <el-table
      :data="contentList"
      v-loading="loading"
      border
      style="width: 100%"
    >
      <el-table-column type="index" width="50" label="序号"></el-table-column>
      <el-table-column prop="title" label="标题" min-width="180"></el-table-column>
      <el-table-column prop="type" label="类型" width="100">
        <template slot-scope="scope">
          <el-tag type="success" v-if="scope.row.type === 'article'">文章</el-tag>
          <el-tag type="warning" v-else-if="scope.row.type === 'video'">视频</el-tag>
          <el-tag v-else>其他</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="author" label="作者" min-width="120"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag type="success" v-if="scope.row.status === 'approved'">已通过</el-tag>
          <el-tag type="warning" v-else-if="scope.row.status === 'pending'">待审核</el-tag>
          <el-tag type="danger" v-else-if="scope.row.status === 'rejected'">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleView(scope.row)"
            plain
          >查看</el-button>
          <el-button
            size="mini"
            type="success"
            @click="handleApprove(scope.row)"
            v-if="scope.row.status === 'pending'"
          >审核通过</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleReject(scope.row)"
            v-if="scope.row.status === 'pending'"
          >拒绝</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
      ></el-pagination>
    </div>

    <!-- 拒绝原因弹窗 -->
    <el-dialog
      title="拒绝原因"
      :visible.sync="rejectDialogVisible"
      width="500px"
    >
      <el-form :model="rejectForm" ref="rejectForm">
        <el-form-item
          label="拒绝原因"
          prop="reason"
          :rules="[{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]"
        >
          <el-input
            type="textarea"
            v-model="rejectForm.reason"
            :rows="4"
            placeholder="请输入拒绝原因"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="rejectDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmReject" :loading="submitLoading">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 内容预览弹窗 -->
    <el-dialog
      title="内容预览"
      :visible.sync="previewDialogVisible"
      width="800px"
    >
      <h3>{{ currentContent.title }}</h3>
      <div v-if="currentContent.type === 'article'" class="content-preview">
        <div v-html="currentContent.content"></div>
      </div>
      <div v-else-if="currentContent.type === 'video'" class="video-preview">
        <video :src="currentContent.videoUrl" controls style="width: 100%"></video>
        <p>{{ currentContent.content }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { contentApi } from '@/api'

export default {
  name: 'ContentManage',
  data() {
    return {
      activeTab: 'all',
      loading: false,
      contentList: [],
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      rejectDialogVisible: false,
      previewDialogVisible: false,
      rejectForm: {
        reason: '',
        contentId: null
      },
      currentContent: {},
      submitLoading: false
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.loading = true

      // Get filter status based on active tab
      let status = null
      if (this.activeTab === 'pending') {
        status = 'pending'
      } else if (this.activeTab === 'rejected') {
        status = 'rejected'
      }

      // Get content type based on active tab
      let type = null
      if (this.activeTab === 'article') {
        type = 'article'
      } else if (this.activeTab === 'video') {
        type = 'video'
      }

      contentApi.getContents(this.pagination.currentPage, this.pagination.pageSize, type, status)
        .then(response => {
          this.contentList = response.data.list
          this.pagination.total = response.data.total
        })
        .catch(error => {
          this.$message.error('获取内容列表失败：' + error.message)
        })
        .finally(() => {
          this.loading = false
        })
    },
    handleTabClick() {
      this.pagination.currentPage = 1
      this.fetchData()
    },
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.fetchData()
    },
    handleCurrentChange(page) {
      this.pagination.currentPage = page
      this.fetchData()
    },
    formatDate(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    handleView(row) {
      this.currentContent = row
      this.previewDialogVisible = true
    },
    handleApprove(row) {
      this.$confirm('确定审核通过该内容吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.submitLoading = true
        contentApi.approveContent(row.id)
          .then(() => {
            this.$message.success('操作成功')
            this.fetchData()
          })
          .catch(error => {
            this.$message.error('操作失败：' + error.message)
          })
          .finally(() => {
            this.submitLoading = false
          })
      }).catch(() => {})
    },
    handleReject(row) {
      this.rejectForm.contentId = row.id
      this.rejectForm.reason = ''
      this.rejectDialogVisible = true
    },
    confirmReject() {
      this.$refs.rejectForm.validate(valid => {
        if (valid) {
          this.submitLoading = true
          contentApi.rejectContent(this.rejectForm.contentId, this.rejectForm.reason)
            .then(() => {
              this.$message.success('操作成功')
              this.rejectDialogVisible = false
              this.fetchData()
            })
            .catch(error => {
              this.$message.error('操作失败：' + error.message)
            })
            .finally(() => {
              this.submitLoading = false
            })
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定删除该内容吗? 此操作不可恢复!', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.submitLoading = true
        contentApi.deleteContent(row.id)
          .then(() => {
            this.$message.success('删除成功')
            this.fetchData()
          })
          .catch(error => {
            this.$message.error('删除失败：' + error.message)
          })
          .finally(() => {
            this.submitLoading = false
          })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.content-manage {
  padding: 20px;
}

header {
  margin-bottom: 20px;
}

.content-filter {
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
</style>