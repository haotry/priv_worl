// 假设这是前端Vue项目的一个API服务文件
// src/api/content.js

import request from '@/utils/request'

// 删除文章
export function deleteArticle(id) {
  return request({
    url: '/hltj/user/article/delete',
    method: 'post',
    params: { id }
  })
}

// 删除视频
export function deleteVideo(id) {
  return request({
    url: '/hltj/user/video/delete',
    method: 'post',
    params: { id }
  })
}

// 删除评论
export function deleteComment(id) {
  return request({
    url: '/hltj/comment/delete',
    method: 'post',
    params: { id }
  })
}

// 前端Vue组件示例 - 文章列表组件
// src/views/my-articles/index.vue

<template>
  <div class="article-list">
    <el-table :data="articles" style="width: 100%">
      <el-table-column prop="title" label="标题"></el-table-column>
      <el-table-column prop="time" label="发布时间"></el-table-column>
      <el-table-column prop="likeCount" label="点赞数"></el-table-column>
      <el-table-column label="操作" width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { deleteArticle } from '@/api/content'

export default {
  name: 'MyArticles',
  data() {
    return {
      articles: []
    }
  },
  methods: {
    handleDelete(row) {
      this.$confirm('确认删除这篇文章?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteArticle(row.id).then(response => {
          if (response.code === 200) {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            // 从列表中移除已删除的文章
            const index = this.articles.findIndex(item => item.id === row.id)
            if (index !== -1) {
              this.articles.splice(index, 1)
            }
          } else {
            this.$message.error(response.msg || '删除失败')
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    }
  }
}
</script>

// 前端Vue组件示例 - 视频管理组件
// src/views/my-videos/index.vue

<template>
  <div class="video-list">
    <el-table :data="videos" style="width: 100%">
      <el-table-column prop="title" label="标题"></el-table-column>
      <el-table-column label="封面" width="180">
        <template slot-scope="scope">
          <img :src="scope.row.cover" style="max-height: 50px;">
        </template>
      </el-table-column>
      <el-table-column prop="createtime" label="发布时间"></el-table-column>
      <el-table-column label="操作" width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { deleteVideo } from '@/api/content'

export default {
  name: 'MyVideos',
  data() {
    return {
      videos: []
    }
  },
  methods: {
    handleDelete(row) {
      this.$confirm('确认删除这个视频?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteVideo(row.id).then(response => {
          if (response.code === 200) {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            // 从列表中移除已删除的视频
            const index = this.videos.findIndex(item => item.id === row.id)
            if (index !== -1) {
              this.videos.splice(index, 1)
            }
          } else {
            this.$message.error(response.msg || '删除失败')
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    }
  }
}
</script>

// 前端Vue组件示例 - 评论管理组件
// src/views/my-comments/index.vue

<template>
  <div class="comment-list">
    <el-table :data="comments" style="width: 100%">
      <el-table-column prop="content" label="评论内容" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="time" label="发布时间"></el-table-column>
      <el-table-column label="操作" width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { deleteComment } from '@/api/content'

export default {
  name: 'MyComments',
  data() {
    return {
      comments: []
    }
  },
  methods: {
    handleDelete(row) {
      this.$confirm('确认删除这条评论?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteComment(row.id).then(response => {
          if (response.code === 200) {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            // 从列表中移除已删除的评论
            const index = this.comments.findIndex(item => item.id === row.id)
            if (index !== -1) {
              this.comments.splice(index, 1)
            }
          } else {
            this.$message.error(response.msg || '删除失败')
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    }
  }
}
</script>