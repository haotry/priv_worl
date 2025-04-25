<template>
  <div class="publish-container">
    <div class="page-header">
      <h2>发布文章</h2>
    </div>

    <el-form :model="form" :rules="rules" ref="form" label-width="100px" class="form">
      <el-form-item label="文章标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入文章标题"></el-input>
      </el-form-item>

      <el-form-item label="封面图" prop="images">
        <div class="upload-container">
          <input
            type="file"
            ref="imageInput"
            accept="image/*"
            style="display: none"
            @change="handleImageSelect"
            multiple
          />
          <div class="image-list">
            <div
              v-for="(img, index) in localImages"
              :key="index"
              class="image-item"
            >
              <img :src="img.url" class="image-preview">
              <div class="image-actions">
                <i class="el-icon-delete" @click="removeImage(index)"></i>
              </div>
              <div v-if="img.uploading" class="upload-overlay">
                <div class="upload-progress">
                  <i class="el-icon-loading"></i>
                  <span>上传中...</span>
                </div>
              </div>
            </div>
            <div
              class="image-add"
              @click="$refs.imageInput.click()"
            >
              <i class="el-icon-plus"></i>
            </div>
          </div>
        </div>
        <div class="upload-tip">可上传多张图片，作为文章封面或内容中使用。首张图片将作为文章封面，所有图片将添加到文章内容末尾。</div>
      </el-form-item>

      <el-form-item label="正文内容" prop="content">
        <el-input
          type="textarea"
          :rows="15"
          placeholder="请输入文章正文内容"
          v-model="form.content">
        </el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">发表文章</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 上传进度提示 -->
    <el-dialog
      title="上传进度"
      :visible.sync="uploadDialogVisible"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      width="300px">
      <div class="upload-progress-dialog">
        <el-progress
          :percentage="uploadProgress"
          :format="progressFormat"
          :status="uploadProgress === 100 ? 'success' : ''">
        </el-progress>
        <p class="upload-status-text">{{ uploadStatusText }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { contentApi } from '@/api'
import { publishApi } from "@/api/publish";

export default {
  name: 'PublishArticle',
  data() {
    return {
      form: {
        title: '',
        content: ''
      },
      url:null,
      localImages: [],
      submitLoading: false,
      uploadDialogVisible: false,
      uploadProgress: 0,
      uploadStatusText: '',
      totalImages: 0,
      uploadedImages: 0,
      rules: {
        title: [
          { required: true, message: '请输入文章标题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入文章内容', trigger: 'blur' },
          { min: 10, message: '内容至少 10 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    progressFormat(percentage) {
      return percentage === 100 ? '完成' : `${percentage}%`;
    },
    handleImageSelect(e) {
      const files = e.target.files
      if (!files || files.length === 0) return

      Array.from(files).forEach(file => {
        // 检查文件类型和大小
        const isImage = file.type.startsWith('image/')
        const isLt2M = file.size / 1024 / 1024 < 2

        if (!isImage) {
          this.$message.error(`${file.name} 不是图片文件!`)
          return
        }
        if (!isLt2M) {
          this.$message.error(`${file.name} 大小超过 2MB!`)
          return
        }

        const url_data = publishApi.uploadFile(file).then(res=>{
          this.url = res.data.data
        })
        console.log(this.url)
        // const url = this.url

        // 创建本地预览URL
        const url = URL.createObjectURL(file)
        this.localImages.push({
          file,
          url,
          name: file.name,
          uploading: false
        })
      })

      // 重置文件输入，使其可以再次选择相同的文件
      this.$refs.imageInput.value = ''
    },
    removeImage(index) {
      // 释放对象URL，避免内存泄漏
      URL.revokeObjectURL(this.localImages[index].url)
      this.localImages.splice(index, 1)
    },
    async submitForm() {
      this.$refs.form.validate(async valid => {
        if (!valid) return

        if (this.localImages.length === 0) {
          this.$message.warning('请至少上传一张图片')
          return
        }

        this.submitLoading = true
        this.uploadDialogVisible = true
        this.uploadProgress = 0
        this.uploadStatusText = '准备上传图片...'
        this.totalImages = this.localImages.length
        this.uploadedImages = 0

        // try {
        //   // 上传所有图片
        //   const uploadedImages = [];
        //   for (let i = 0; i < this.localImages.length; i++) {
        //     const image = this.localImages[i];
        //     try {
        //       // 更新当前图片上传状态
        //       this.$set(this.localImages[i], 'uploading', true);
        //
        //       // 更新进度对话框
        //       this.uploadStatusText = `正在上传图片 ${i + 1}/${this.localImages.length}: ${image.name}`;
        //       this.uploadProgress = Math.floor((i / this.localImages.length) * 80); // 预留20%给发布过程
        //
        //       const uploadResponse = await publishApi.uploadFile(image.file, 'image');
        //
        //       if (uploadResponse && uploadResponse.data && uploadResponse.data.code === 200) {
        //         const imageUrl = uploadResponse.data.data;
        //         uploadedImages.push({
        //           originalUrl: image.url,
        //           serverUrl: imageUrl
        //         });
        //
        //         // 更新上传状态
        //         this.$set(this.localImages[i], 'uploading', false);
        //         this.uploadedImages++;
        //
        //         console.log(`图片${i + 1}上传成功:`, imageUrl);
        //       } else {
        //         throw new Error(uploadResponse?.data?.msg || '图片上传失败');
        //       }
        //     } catch (uploadError) {
        //       // 清除上传状态
        //       this.$set(this.localImages[i], 'uploading', false);
        //
        //       console.error(`图片${i + 1}上传失败:`, uploadError);
        //       this.$message.error(`图片${i + 1}上传失败: ${uploadError.message || '未知错误'}`);
        //
        //       this.uploadDialogVisible = false;
        //       this.submitLoading = false;
        //       return;
        //     }
        //   }

          // 使用第一张图片作为封面
          const url = this.url;

          // 处理文章内容，将本地图片URL替换为服务器图片URL
          let processedContent = this.form.content;
          //
          // // 在文章末尾添加所有上传的图片
          // if (uploadedImages.length > 0) {
          //   processedContent += '\n\n';
          //   uploadedImages.forEach((img, index) => {
          //     processedContent += `![图片${index + 1}](${img.serverUrl})\n`;
          //   });
          // }

          // 更新进度对话框
          this.uploadStatusText = '正在发布文章...';
          this.uploadProgress = 90;

          // 发布文章，带上封面URL和处理后的内容
          const response = await publishApi.publishArticle(
            this.form.title,
            processedContent,
            url
          );

          // 完成进度
          this.uploadProgress = 100;
          this.uploadStatusText = '发布成功！';

          if (response && response.data && response.data.code === 200) {
            setTimeout(() => {
              this.uploadDialogVisible = false;
              this.$message.success('文章发布成功');
              this.resetForm();
            }, 500);
          } else {
            this.uploadDialogVisible = false;
            this.$message.error(`文章发布失败: ${response?.data?.msg || '未知错误'}`);
          }
        // } catch (error) {
        //   this.uploadDialogVisible = false;
        //   this.$message.error(`文章发布失败: ${error.message || '未知错误'}`);
        //   console.error(error);
        // } finally {
        //   this.submitLoading = false;
        // }
      });
    },
    resetForm() {
      this.$refs.form.resetFields()
      // 释放所有对象URL
      this.localImages.forEach(img => URL.revokeObjectURL(img.url))
      this.localImages = []
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

.upload-container {
  margin-bottom: 10px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-item {
  position: relative;
  width: 148px;
  height: 148px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 0;
  right: 0;
  padding: 8px;
  background-color: rgba(0, 0, 0, 0.5);
  border-bottom-left-radius: 6px;
  cursor: pointer;
}

.image-actions i {
  color: #fff;
  font-size: 18px;
}

.image-add {
  width: 148px;
  height: 148px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.image-add i {
  font-size: 28px;
  color: #8c939d;
}

.image-add:hover {
  border-color: #409EFF;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 5;
}

.upload-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-progress i {
  font-size: 24px;
  color: #fff;
  margin-bottom: 10px;
  animation: rotating 2s linear infinite;
}

.upload-progress span {
  font-size: 14px;
  color: #fff;
  text-align: center;
  padding: 0 10px;
}

.upload-progress-dialog {
  padding: 20px;
}

.upload-status-text {
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
  text-align: center;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>