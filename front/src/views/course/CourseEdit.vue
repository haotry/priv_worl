<template>
  <div class="course-edit-container">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑课程' : '添加课程' }}</h2>
      <el-button type="default" icon="el-icon-back" @click="goBack">返回上一页</el-button>
    </div>

    <el-form :model="form" :rules="rules" ref="form" label-width="120px" class="form">
      <el-card shadow="never">
        <div slot="header" class="form-card-header">
          <span>基本信息</span>
        </div>

        <el-form-item label="选择分类" prop="menuId">
          <el-select v-model="form.menuId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="课程标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入课程标题"></el-input>
        </el-form-item>

        <el-form-item label="课程副标题" prop="subtitle">
          <el-input v-model="form.subtitle" placeholder="请输入课程副标题"></el-input>
        </el-form-item>

        <el-form-item label="课程定价" prop="credit">
          <el-input-number v-model="form.credit" :min="0" :precision="0" label="课程定价(积分)"></el-input-number>
        </el-form-item>

        <el-form-item label="上架状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="上架"
            inactive-text="下架">
          </el-switch>
        </el-form-item>
      </el-card>

      <el-card shadow="never" style="margin-top: 20px;">
        <div slot="header" class="form-card-header">
          <span>媒体信息</span>
        </div>

        <el-form-item label="课程视频" prop="video">
          <el-upload
            class="video-uploader"
            action="/api/upload/video"
            :show-file-list="false"
            :on-success="handleVideoSuccess"
            :before-upload="beforeVideoUpload">
            <video v-if="form.video" :src="form.video" class="video-preview" controls></video>
            <i v-else class="el-icon-plus video-uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">支持mp4格式，大小不超过200MB</div>
        </el-form-item>

        <el-form-item label="课程封面" prop="cover">
          <el-upload
            class="cover-uploader"
            action="/api/upload/image"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload">
            <img v-if="form.cover" :src="form.cover" class="cover-preview">
            <i v-else class="el-icon-plus cover-uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">建议尺寸: 800x450px，大小不超过2MB</div>
        </el-form-item>
      </el-card>

      <el-card shadow="never" style="margin-top: 20px;">
        <div slot="header" class="form-card-header">
          <span>详细信息</span>
        </div>

        <el-form-item label="课程简介" prop="description">
          <el-input
            type="textarea"
            :rows="5"
            placeholder="请输入课程简介"
            v-model="form.description">
          </el-input>
        </el-form-item>

        <el-form-item label="购买须知" prop="notice">
          <el-input
            type="textarea"
            :rows="5"
            placeholder="请输入购买须知"
            v-model="form.notice">
          </el-input>
        </el-form-item>
      </el-card>

      <div class="form-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确认提交</el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import { courseApi, categoryApi } from '@/api'

export default {
  name: 'CourseEdit',
  data() {
    return {
      isEdit: false,
      courseId: null,
      categories: [],
      submitLoading: false,
      form: {
        id: null,
        menuId: '',
        title: '',
        subtitle: '',
        credit: 0,
        status: 0,
        video: '',
        cover: '',
        description: '',
        notice: ''
      },
      rules: {
        menuId: [
          { required: true, message: '请选择分类', trigger: 'change' }
        ],
        title: [
          { required: true, message: '请输入课程标题', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        subtitle: [
          { max: 100, message: '长度不能超过 100 个字符', trigger: 'blur' }
        ],
        credit: [
          { required: true, message: '请设置课程定价', trigger: 'blur' }
        ],
        video: [
          { required: true, message: '请上传课程视频', trigger: 'change' }
        ],
        cover: [
          { required: true, message: '请上传课程封面', trigger: 'change' }
        ],
        description: [
          { required: true, message: '请输入课程简介', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.courseId = this.$route.params.id
    this.isEdit = !!this.courseId
    this.fetchCategories()
    if (this.isEdit) {
      this.fetchCourseDetail()
    }
  },
  methods: {
    async fetchCategories() {
      try {
        const res = await categoryApi.getCategories()
        const { data } = res.data
        this.categories = data || []
      } catch (error) {
        this.$message.error('获取分类失败')
        console.error(error)
      }
    },
    async fetchCourseDetail() {
      try {
        const jwcode = this.$store.state.userInfo?.jwcode || ''
        const res = await courseApi.getCourseDetail(this.courseId, jwcode)
        const { data } = res.data
        this.form = {
          id: data.id,
          menuId: data.menuId,
          title: data.title,
          subtitle: data.subtitle || '',
          credit: data.credit || 0,
          status: data.status,
          video: data.video,
          cover: data.cover,
          description: data.description || '',
          notice: data.notice || ''
        }
      } catch (error) {
        this.$message.error('获取课程详情失败')
        console.error(error)
      }
    },
    handleVideoSuccess(res, file) {
      if (res.code === 0) {
        this.form.video = res.data
      } else {
        this.$message.error('上传视频失败')
      }
    },
    beforeVideoUpload(file) {
      const isMP4 = file.type === 'video/mp4'
      const isLt200M = file.size / 1024 / 1024 < 200

      if (!isMP4) {
        this.$message.error('上传视频只能是 MP4 格式!')
      }
      if (!isLt200M) {
        this.$message.error('上传视频大小不能超过 200MB!')
      }
      return isMP4 && isLt200M
    },
    handleCoverSuccess(res, file) {
      if (res.code === 0) {
        this.form.cover = res.data
      } else {
        this.$message.error('上传封面失败')
      }
    },
    beforeCoverUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('上传封面只能是图片格式!')
      }
      if (!isLt2M) {
        this.$message.error('上传封面大小不能超过 2MB!')
      }
      return isImage && isLt2M
    },
    goBack() {
      this.$router.go(-1)
    },
    async submitForm() {
      this.$refs.form.validate(async valid => {
        if (!valid) {
          this.$message.warning('请完善表单信息')
          return
        }

        this.submitLoading = true
        try {
          if (this.isEdit) {
            await courseApi.updateCourse(this.form)
            this.$message.success('课程更新成功')
          } else {
            await courseApi.addCourse(this.form)
            this.$message.success('课程添加成功')
          }
          this.goBack()
        } catch (error) {
          this.$message.error(this.isEdit ? '课程更新失败' : '课程添加失败')
          console.error(error)
        } finally {
          this.submitLoading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.course-edit-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.form {
  max-width: 800px;
  margin: 0 auto;
}

.form-card-header {
  font-size: 16px;
  font-weight: 500;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.video-uploader, .cover-uploader {
  cursor: pointer;
}

.video-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.video-uploader .el-upload:hover {
  border-color: #409EFF;
}

.video-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 480px;
  height: 270px;
  line-height: 270px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
}

.video-preview {
  width: 480px;
  height: 270px;
  display: block;
}

.cover-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.cover-uploader .el-upload:hover {
  border-color: #409EFF;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 160px;
  height: 90px;
  line-height: 90px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
}

.cover-preview {
  width: 160px;
  height: 90px;
  display: block;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>