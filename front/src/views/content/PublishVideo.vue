<template>
  <div class="publish-container">
    <div class="page-header">
      <h2>发布视频</h2>
    </div>

    <el-form :model="form" :rules="rules" ref="form" label-width="100px" class="form">
      <el-form-item label="视频标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入视频标题"></el-input>
      </el-form-item>

      <el-form-item label="上传视频" prop="video">
        <div class="video-uploader">
          <input
            type="file"
            ref="videoInput"
            accept="video/mp4"
            style="display: none"
            @change="handleVideoChange"
          />
          <div
            class="video-container"
            @click="$refs.videoInput.click()"
          >
            <video v-if="videoUrl" :src="videoUrl" class="video-preview" controls></video>
            <i v-else class="el-icon-plus video-uploader-icon"></i>
          </div>
          <div v-if="form.videoFile" class="file-info">
            <span>{{ form.videoFile.name }}</span>
            <el-button type="text" icon="el-icon-delete" @click="removeVideo"></el-button>
          </div>
          <div class="upload-tip">支持mp4格式，大小不超过200MB，本地预览</div>
        </div>
      </el-form-item>

      <el-form-item label="视频封面" prop="cover">
        <div class="cover-uploader">
          <input
            type="file"
            ref="coverInput"
            accept="image/*"
            style="display: none"
            @change="handleCoverChange"
          />
          <div
            class="cover-container"
            @click="$refs.coverInput.click()"
          >
            <img v-if="coverUrl" :src="coverUrl" class="cover-preview">
            <i v-else class="el-icon-plus cover-uploader-icon"></i>
          </div>
          <div v-if="form.coverFile" class="file-info">
            <span>{{ form.coverFile.name }}</span>
            <el-button type="text" icon="el-icon-delete" @click="removeCover"></el-button>
          </div>
          <div class="upload-tip">建议尺寸: 800x450px，大小不超过2MB，本地预览</div>
        </div>
      </el-form-item>

      <el-form-item label="视频简介" prop="content">
        <el-input
          type="textarea"
          :rows="6"
          placeholder="请输入视频简介"
          v-model="form.content">
        </el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">发表视频</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { contentApi } from '@/api'
import { publishApi } from "@/api/publish";

export default {
  name: 'PublishVideo',
  data() {
    return {
      form: {
        title: '',
        content: '',
        video: '',
        cover: '',
        videoFile: null,
        coverFile: null
      },
      url:null,
      videoUrl: '',
      coverUrl: '',
      submitLoading: false,
      rules: {
        title: [
          { required: true, message: '请输入视频标题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入视频简介', trigger: 'blur' },
          { min: 10, message: '简介至少 10 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleVideoChange(e) {
      const file = e.target.files[0]
      if (!file) return

      const isMP4 = file.type === 'video/mp4'
      const isLt200M = file.size / 1024 / 1024 < 200

      if (!isMP4) {
        this.$message.error('上传视频只能是 MP4 格式!')
        return
      }
      if (!isLt200M) {
        this.$message.error('上传视频大小不能超过 200MB!')
        return
      }

      // 保存文件对象并创建本地预览URL
      this.form.videoFile = file
      this.form.video = 'local_video_' + Date.now() // 使用本地占位符
      this.videoUrl = URL.createObjectURL(file)

      // 通知表单验证更新
      this.$refs.form.validateField('video')
    },
    removeVideo() {
      this.form.videoFile = null
      this.form.video = ''
      this.videoUrl = ''
      if (this.$refs.videoInput) {
        this.$refs.videoInput.value = ''
      }
      // 通知表单验证更新
      this.$refs.form.validateField('video')
    },
    handleCoverChange(e) {
      const file = e.target.files[0]
      if (!file) return

      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('上传封面只能是图片格式!')
        return
      }
      if (!isLt2M) {
        this.$message.error('上传封面大小不能超过 2MB!')
        return
      }
      const url_data = publishApi.uploadFile(file).then(res=>{
        this.url = res.data.data
      })
      console.log(this.url)
      // 保存文件对象并创建本地预览URL
      this.form.coverFile = file
      this.form.cover = 'local_cover_' + Date.now() // 使用本地占位符
      this.coverUrl = URL.createObjectURL(file)

      // 通知表单验证更新
      this.$refs.form.validateField('cover')
    },
    removeCover() {
      this.form.coverFile = null
      this.form.cover = ''
      this.coverUrl = ''
      if (this.$refs.coverInput) {
        this.$refs.coverInput.value = ''
      }
      // 通知表单验证更新
      this.$refs.form.validateField('cover')
    },
    async submitForm() {
      this.$refs.form.validate(async valid => {
        if (!valid) return

        if (!this.form.videoFile) {
          this.$message.error('请上传视频文件')
          return
        }

        if (!this.form.coverFile) {
          this.$message.error('请上传视频封面')
          return
        }

        this.submitLoading = true
        try {
          // 1. 上传视频文件
          let videoUrl = '';
          try {
            const videoUploadResponse = await publishApi.uploadFile(this.form.videoFile, 'video');
            if (videoUploadResponse && videoUploadResponse.data && videoUploadResponse.data.code === 200) {
              videoUrl = videoUploadResponse.data.data;
              console.log('视频上传成功:', videoUrl);
            } else {
              throw new Error(videoUploadResponse?.data?.msg || '视频上传失败');
            }
          } catch (uploadError) {
            console.error('视频上传失败:', uploadError);
            this.$message.error(`视频上传失败: ${uploadError.message || '未知错误'}`);
            this.submitLoading = false;
            return;
          }

          // 2. 上传封面图片
          // let coverUrl = '';
          // try {
          //   const coverUploadResponse = await publishApi.uploadFile(this.form.coverFile, 'image');
          //   if (coverUploadResponse && coverUploadResponse.data && coverUploadResponse.data.code === 200) {
          //     coverUrl = coverUploadResponse.data.data;
          //     console.log('封面图上传成功:', coverUrl);
          //   } else {
          //     throw new Error(coverUploadResponse?.data?.msg || '封面图上传失败');
          //   }
          // } catch (uploadError) {
          //   console.error('封面图上传失败:', uploadError);
          //   this.$message.error(`封面图上传失败: ${uploadError.message || '未知错误'}`);
          //   this.submitLoading = false;
          //   return;
          // }
          const url = this.url
          // 3. 发布视频，带上视频和封面的URL
          const response = await publishApi.publishVideo(
            this.form.title,
            this.form.content,
            videoUrl,
            url
          );

          if (response && response.data && response.data.code === 200) {
            this.$message.success('视频发布成功');
            this.resetForm();
          } else {
            this.$message.error(`视频发布失败: ${response?.data?.msg || '未知错误'}`);
          }
        } catch (error) {
          this.$message.error(`视频发布失败: ${error.message || '未知错误'}`);
          console.error(error);
        } finally {
          this.submitLoading = false;
        }
      });
    },
    resetForm() {
      this.$refs.form.resetFields()
      this.removeVideo()
      this.removeCover()
    }
  }
}
</script>

<style scoped>
.publish-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.form {
  max-width: 800px;
}

.video-container {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  margin-bottom: 10px;
}

.video-container:hover {
  border-color: #409EFF;
}

.video-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 480px;
  height: 270px;
  line-height: 270px;
  text-align: center;
  display: block;
}

.video-preview {
  width: 480px;
  height: 270px;
  display: block;
}

.cover-container {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  margin-bottom: 10px;
}

.cover-container:hover {
  border-color: #409EFF;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 160px;
  height: 90px;
  line-height: 90px;
  text-align: center;
  display: block;
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

.file-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 5px;
}
</style>