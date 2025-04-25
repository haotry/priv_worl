import axios from 'axios'

export  const publishApi={
    /**
     * 上传文件（图片或视频）
     * @param {File} file - 要上传的文件对象
     * @param {String} type - 文件类型，可选值：'image'或'video'
     * @returns {Promise} - 返回上传结果，包含文件URL
     */
    uploadFile(file, type = 'image') {
        // 创建FormData对象
        const formData = new FormData();
        formData.append('file', file);

        // 根据文件类型选择上传接口
        // const uploadUrl = type === 'video' ? '/upload/video' : '/upload/image';

        return axios.post('/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });
    },

    /**
     * 发布文章
     * @param {String} title - 文章标题
     * @param {String} content - 文章内容
     * @param {String} coverUrl - 封面图URL
     * @returns {Promise} - 返回发布结果
     */
    publishArticle(title, content, url) {
        return axios.post('/user/article',null, {
            params:{
                title,
                content,
                url // 添加封面图参数
            },
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        })
    },

    /**
     * 发布视频
     * @param title
     * @param content
     * @param video
     * @param url
     * @returns {Promise<AxiosResponse<any>>}
     */
    publishVideo(title,content,video,url) {
        return axios.post('/user/video',null, {
            params:{
                title,
                content,
                video,
                url
            },
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        })
    }
}
