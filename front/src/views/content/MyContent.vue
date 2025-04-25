<template>
  <div class="content-container">
    <div class="page-header">
      <h2>我的内容</h2>
    </div>

    <div class="filter-container">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="动态" name="moment"></el-tab-pane>
        <el-tab-pane label="文章" name="article"></el-tab-pane>
        <el-tab-pane label="视频" name="video"></el-tab-pane>
      </el-tabs>
    </div>

    <el-table
      :data="contentList"
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
        label="标题"
        min-width="250">
        <template v-slot="scope">
          <div class="ellipsis" :title="scope.row.type === 3 ? scope.row.content : scope.row.title">
            {{ scope.row.type === 3 ? scope.row.content : scope.row.title }}
          </div>
        </template>
      </el-table-column>
      <el-table-column
        prop="type"
        label="类型"
        width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.type === 1 ? 'success' :scope.row.type===2?'primary':'warning'">
            {{ scope.row.type === 3 ? '动态' :scope.row.type===2? '视频':'文章' }}
          </el-tag>
        </template>
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
        prop="updateTime"
        label="更新时间"
        width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        fixed="right"
        width="150"
        v-if="this.$store.state.userInfo.userIdentity === 0"
      >
        <template slot-scope="scope">
<!--          <el-button
            size="mini"
            type="primary"
            @click="handleEdit(scope.row)">编辑</el-button>-->
          <el-button
            size="mini"
            type="danger" v-if="scope.row.type!==3"
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
  </div>
</template>

<script>
import { contentApi } from '@/api'

export default {
  name: 'MyContent',
  data() {
    return {
      loading: false,
      contentList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      activeTab: 'all'
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        // 获取服务器数据
        let serverData = []
        try {
          const jwcode = this.$store.state.userInfo?.jwcode || ''
          console.log('获取内容，使用的jwcode:', jwcode)

          const res = await contentApi.getUserContents(jwcode, this.currentPage, this.pageSize)
          console.log('获取服务器内容响应:', res)

          const { data } = res.data
          console.log('服务器提取的内容数据:', data)

          // 检查数据结构
          if (data && Array.isArray(data.records)) {
            serverData = data.records
          } else if (data && Array.isArray(data.list)) {
            serverData = data.list
          } else if (data && Array.isArray(data)) {
            serverData = data
          }
        } catch (error) {
          console.error('获取服务器内容失败:', error)
        }

        // 获取本地存储的视频和文章
        const localVideos = JSON.parse(localStorage.getItem('localVideos') || '[]')
        const localArticles = JSON.parse(localStorage.getItem('localArticles') || '[]')
        console.log('本地视频:', localVideos)
        console.log('本地文章:', localArticles)

        const articles = await  contentApi.getAllUserArticle(this.pageSize,this.currentPage)
        console.log('获取服务器内容响应:article  ', articles.data.data.list)
        const videos = await contentApi.getAllUserVideo(this.pageSize,this.currentPage)
        console.log('获取服务器内容响应:video  ', videos.data.data.list)
        // const videosRes = videos.data.data.list.filter((item)=>{
        //   item.type=2
        // })
        // console.log('获取服务器内容响应:videoRes  ', videosRes)

        // 合并数据
        const allData = [...serverData, ...videos.data.data.list, ...localVideos,  ...articles.data.data.list,...localArticles]
        console.log('合并后的全部数据:', allData)

        // 处理过滤
        this.processContentList(allData)
        this.total = allData.length
      } catch (error) {
        this.$message.error('获取内容列表失败')
        console.error('获取内容错误详情:', error)
        this.contentList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    // 处理内容列表
    processContentList(records) {
      console.log('处理内容列表，原始记录:', records)
      // 根据当前标签过滤内容
      this.contentList = records.filter(item => {
        if (this.activeTab === 'all') return true
        if (this.activeTab === 'moment' && item.type === 3) return true
        if (this.activeTab === 'article' && item.type === 1) return true
        if (this.activeTab === 'video' && item.type === 2) return true
        return false
      }).map(item => {
        console.log('处理内容项:', item)
        // 确保所有必要字段都存在
        return {
          ...item,
          id: item.id || Date.now(),
          title: item.title || '无标题',
          type: item.type || 1, // 默认文章类型
          createTime: item.createTime || Date.now(),
          updateTime: item.updateTime || item.createTime || Date.now(),
          isLocal: !!(item.videoUrl || item.coverUrl)
        }
      })
      // 按创建时间倒序排列
      this.contentList.sort((a, b) => b.createTime - a.createTime)
      console.log('处理后的内容列表:', this.contentList)
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
    handleTabClick() {
      this.currentPage = 1
      this.fetchData()
    },
    async handleDelete(row) {
      console.log('准备删除内容:', row)
      this.$confirm(`确定要删除该${row.type === 1 ? '文章' : '视频'}吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 检查是否是本地内容
          if (row.isLocal) {
            console.log('删除本地内容')
            const storageKey = row.type === 1 ? 'localArticles' : 'localVideos'
            const localItems = JSON.parse(localStorage.getItem(storageKey) || '[]')
            const updatedItems = localItems.filter(item => item.id !== row.id)
            localStorage.setItem(storageKey, JSON.stringify(updatedItems))
            this.$message.success('删除成功')
            this.fetchData()
            return
          }

          // 删除服务器内容
          const key = row.type === 1 ? 'article' : 'video'
          console.log('开始删除服务器内容，ID:', row.id)
          const res = await contentApi.deleteContent(key,row.id)
          console.log('删除内容响应:', res)

          this.$message.success('删除成功')
          // 重新获取数据
          this.fetchData()
        } catch (error) {
          console.error('删除内容失败:', error)
          if (error.response) {
            console.error('删除错误响应:', error.response.status, error.response.data)
            this.$message.error(`删除失败: ${error.response.status} ${error.response.data.message || ''}`)
          } else {
            this.$message.error('删除失败: ' + error.message)
          }
        }
      }).catch(() => {
        console.log('用户取消删除操作')
        // 取消删除
      })
    },
    async handleEdit(row) {
      // 检查是否是本地内容
      if (row.isLocal) {
        this.$message.warning('本地内容暂不支持编辑，请重新上传')
        return
      }

      // 根据内容类型跳转到不同的编辑页面
      if (row.type === 1) {
        // 文章
        this.$router.push({
          path: '/publish-article',
          query: { id: row.id }
        })
      } else {
        // 视频
        this.$router.push({
          path: '/publish-video',
          query: { id: row.id }
        })
      }
    },
  }
}
</script>

<style scoped>
.content-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.ellipsis {
  white-space: nowrap; /* 防止内容换行 */
  overflow: hidden; /* 隐藏超出部分 */
  text-overflow: ellipsis; /* 显示省略号 */
  max-width: 100%; /* 限制最大宽度 */
  cursor: pointer; /* 鼠标悬停时显示为手型 */
}
</style>