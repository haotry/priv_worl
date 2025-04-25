<template>
  <div class="publish-course-container">
    <div class="page-header">
      <h2>发布课程</h2>
    </div>

    <el-form :model="form" :rules="rules" ref="form" label-width="100px" class="form">
      <el-form-item label="课程标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入课程标题"></el-input>
      </el-form-item>

      <el-form-item label="课程分类" prop="menu_id">
        <el-select v-model="form.menu_id" placeholder="请选择课程分类">
          <el-option
            v-for="item in categories"
            :key="item.id"
            :label="item.name"
            :value="item.name">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="课程介绍" prop="content">
        <el-input
          type="textarea"
          :rows="5"
          placeholder="请输入课程介绍"
          v-model="form.content">
        </el-input>
      </el-form-item>

      <el-form-item label="个人介绍" prop="intro">
        <el-input
          type="textarea"
          :rows="3"
          placeholder="请简单介绍您的教学经验和专业背景"
          v-model="form.intro">
        </el-input>
      </el-form-item>

      <el-form-item label="视频链接" prop="video">
        <div class="video-uploader-wrapper">
          <div class="video-uploader">
            <input
              type="file"
              ref="videoInput"
              accept="video/*"
              style="display: none"
              @change="handleVideoSelect"
            />
            <div class="video-preview" v-if="form.video && localVideoUrl">
              <video :src="localVideoUrl" controls style="width: 100%; max-height: 300px;"></video>
              <div class="video-actions">
                <i class="el-icon-delete" @click="removeVideo"></i>
              </div>
            </div>
            <div
              v-else
              class="video-add"
              @click="$refs.videoInput.click()"
            >
              <i class="el-icon-plus"></i>
              <div>点击上传视频</div>
            </div>
          </div>
          <div class="upload-tip">支持mp4格式，大小不超过200MB</div>
        </div>
      </el-form-item>

      <el-form-item label="课程封面" prop="cover">
        <div class="cover-uploader-wrapper">
          <div class="cover-uploader">
            <input
              type="file"
              ref="coverInput"
              accept="image/*"
              style="display: none"
              @change="handleCoverSelect"
            />
            <div class="cover-preview" v-if="form.cover">
              <img :src="form.cover" class="cover-image">
              <div class="cover-actions">
                <i class="el-icon-delete" @click="removeCover"></i>
              </div>
            </div>
            <div
              v-else
              class="cover-add"
              @click="$refs.coverInput.click()"
            >
              <i class="el-icon-plus"></i>
              <div>点击上传封面</div>
            </div>
          </div>
          <div class="upload-tip">建议尺寸: 800x450px，大小不超过2MB</div>
        </div>
      </el-form-item>

      <el-form-item label="课程价格" prop="price">
        <el-input-number v-model="form.price" :min="0" :max="10000" label="积分价格"></el-input-number>
        <span class="price-tip">积分</span>
      </el-form-item>

      <el-form-item label="购买须知" prop="purchase_notes">
        <el-input
          type="textarea"
          :rows="3"
          placeholder="请输入购买须知"
          v-model="form.purchase_notes">
        </el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="loading">发布</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { courseApi, categoryApi } from '@/api'
import axios from 'axios'
import { mapState } from 'vuex'
import {publishApi} from "@/api/publish";

export default {
  name: 'PublishCourse',
  data() {
    return {
      form: {
        title: '',
        menu_id: null,
        content: '',
        intro: '',
        image:'',
        video: '',
        cover: '',
        price: 0,
        purchase_notes: ''
      },
      localVideoUrl: '',
      videoFile: null,
      coverFile: null,
      categories: [],
      loading: false,
      rules: {
        title: [
          { required: true, message: '请输入课程标题', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        menu_id: [
          { required: true, message: '请选择课程分类', trigger: 'change' }
        ],
        content: [
          { required: true, message: '请输入课程介绍', trigger: 'blur' }
        ],
        intro: [
          { required: true, message: '请输入个人介绍', trigger: 'blur' }
        ],
        price: [
          { required: true, message: '请设置课程价格', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapState(['userInfo'])
  },
  created() {
    this.fetchCategories()
  },
  methods: {
    async fetchCategories() {
      try {
        const res = await categoryApi.getCategories()
        this.categories = res.data.data || []
        console.log('课程分类列表 '+ JSON.stringify(res.data.data))

      } catch (error) {
        this.$message.error('获取课程分类失败')
        console.error(error)
      }
    },
    handleVideoSelect(e) {
      const file = e.target.files[0]
      if (!file) return

      const isMP4 = file.type === 'video/mp4'
      const isLt200M = file.size / 1024 / 1024 < 200

      if (!isMP4) {
        this.$message.error('上传视频只能是 MP4 格式!')
        this.$refs.videoInput.value = ''
        return
      }
      if (!isLt200M) {
        this.$message.error('上传视频大小不能超过 200MB!')
        this.$refs.videoInput.value = ''
        return
      }
      const url_data = publishApi.uploadFile(file).then(res=>{
        console.log("视频上传成功",res)
        this.video = res.data.data
        console.log("视频上传成功2",this.video)
      })
      // 创建本地预览URL
      this.localVideoUrl = URL.createObjectURL(file)
      this.form.video = '待上传'
      this.videoFile = file
      this.$refs.videoInput.value = ''
    },
    removeVideo() {
      if (this.localVideoUrl) {
        URL.revokeObjectURL(this.localVideoUrl)
      }
      this.localVideoUrl = ''
      this.form.video = ''
      this.videoFile = null
    },
    handleCoverSelect(e) {
      const file = e.target.files[0]
      if (!file) return

      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('上传封面只能是图片格式!')
        this.$refs.coverInput.value = ''
        return
      }
      if (!isLt2M) {
        this.$message.error('上传封面大小不能超过 2MB!')
        this.$refs.coverInput.value = ''
        return
      }
      const url_data = publishApi.uploadFile(file).then(res=>{
        this.image = res.data.data
      })
      console.log(this.url)

      // 创建本地预览URL
      this.form.cover = URL.createObjectURL(file)
      this.coverFile = file
      this.$refs.coverInput.value = ''
    },
    removeCover() {
      if (this.form.cover && this.form.cover.startsWith('blob:')) {
        URL.revokeObjectURL(this.form.cover)
      }
      this.form.cover = ''
      this.coverFile = null
    },
    async submitForm() {
      this.$refs.form.validate(async valid => {
        if (!valid) return

        this.loading = true
        try {
          /*// 上传视频文件
          if (this.videoFile) {
            const videoFormData = new FormData()
            videoFormData.append('file', this.videoFile)
            const videoRes = await axios.post('/api/upload/video', videoFormData, {
              headers: { 'Content-Type': 'multipart/form-data' }
            })
            if (videoRes.data.code === 0) {
              this.form.video = videoRes.data.data
            } else {
              throw new Error('视频上传失败')
            }
          } else if (!this.form.video) {
            this.$message.warning('请上传课程视频')
            this.loading = false
            return
          }

          // 上传封面文件
          if (this.coverFile) {
            const coverFormData = new FormData()
            coverFormData.append('file', this.coverFile)
            const coverRes = await axios.post('/api/upload/image', coverFormData, {
              headers: { 'Content-Type': 'multipart/form-data' }
            })
            if (coverRes.data.code === 0) {
              this.form.cover = coverRes.data.data
            } else {
              throw new Error('封面上传失败')
            }
          }*/

          // 补充教师信息
          this.form.author = this.userInfo.name
          this.form.avatar = this.userInfo.avatar || ''

          // 跳过文件上传，直接使用null值
          this.form.video = null
          this.form.cover = null

          // 转换请求数据
          const courseData = {
            title: this.form.title,
            menu_id: this.categories.filter(c => c.name === this.form.menu_id)[0].id,
            content: this.form.content,
            intro: this.form.intro,
            video: this.video,
            image:this.image,
            cover: this.form.cover,
            price: this.form.price,
            purchase_notes: this.form.purchase_notes,
            author: this.form.author,
            avatar: this.form.avatar
          }

          // 直接提交课程信息
          const response = await axios.post('/lesson/add', courseData)
          console.log('API响应:', response)

          // 检查响应中是否包含data和code字段，以及code是否为0（成功）
          if (response.data && (response.data.code === 0 || response.data.code === 200 || response.data === 'success')) {
            this.$message.success('课程提交成功')
            this.resetForm()
          } else {
            // 如果响应不符合预期格式，但仍然是成功的HTTP响应
            if (response.status >= 200 && response.status < 300) {
              this.$message.success('课程提交成功')
              this.resetForm()
            } else {
              throw new Error(response.data?.msg || '课程提交失败')
            }
          }
        } catch (error) {
          this.$message.error('提交失败: ' + error.message)
          console.error(error)
        } finally {
          this.loading = false
        }
      })
    },
    resetForm() {
      this.$refs.form.resetFields()
      if (this.localVideoUrl) {
        URL.revokeObjectURL(this.localVideoUrl)
      }
      if (this.form.cover && this.form.cover.startsWith('blob:')) {
        URL.revokeObjectURL(this.form.cover)
      }
      this.localVideoUrl = ''
      this.videoFile = null
      this.coverFile = null
      this.form = {
        title: '',
        menu_id: '',
        content: '',
        intro: '',
        video: '',
        cover: '',
        price: 0,
        purchase_notes: ''
      }
    }
  }
}
</script>

<style scoped>
.publish-course-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.form {
  max-width: 800px;
}

.video-uploader-wrapper, .cover-uploader-wrapper {
  width: 100%;
}

.video-uploader, .cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  margin-bottom: 5px;
}

.video-add, .cover-add {
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
}

.video-add i, .cover-add i {
  font-size: 28px;
  margin-bottom: 10px;
}

.video-preview, .cover-preview {
  position: relative;
}

.video-actions, .cover-actions {
  position: absolute;
  top: 5px;
  right: 5px;
  background-color: rgba(0, 0, 0, 0.5);
  color: #fff;
  border-radius: 4px;
  padding: 5px;
  cursor: pointer;
}

.cover-image {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #606266;
  margin-top: 5px;
}

.price-tip {
  margin-left: 10px;
  color: #606266;
}
</style>