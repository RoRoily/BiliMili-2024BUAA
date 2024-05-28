<template>
    <div class="textUpload">
        <div class="container">
            <mavon-editor v-model="mdContent" ishljs="true" ref="md" @save="saveMd" @imgAdd="handleImgAdd" placeholder="输入你的BiliMili专栏内容..."></mavon-editor>
            <el-button class="editor-btn" type="primary" @click="submit">发布专栏</el-button>
        </div>
    </div>
</template>

<script>
import mavonEditor from 'mavon-editor';
import 'mavon-editor/dist/css/index.css'
import axios from "axios";

export default {
    name: "TextUpload",
    components: {
        'mavonEditor': mavonEditor.mavonEditor
    },
    data() {
        return {
            mdContent: ''
        };
    },
    methods: {
        async handleImgAdd(pos, $file) {
            try {
                let formData = new FormData();
                formData.append('image', $file);
                 //console.log('FormData内容:', formData);
              // 创建请求配置对象，并设置授权信息
              const config = {
                headers: {
                  'Content-Type': 'multipart/form-data',
                  Authorization: "Bearer " + localStorage.getItem("teri_token"), // 替换为您的授权信息变量
                }
              };
                // 发送请求到后端上传图片
                const response = await axios.post('/api/image/add', formData,config);
                // console.log('响应数据:', response.data);
                if (response.data.success) {
                    // 将返回的图片URL插入到Markdown内容中
                    this.$refs.md.$img2Url(pos, response.data.url);
                    alert('图片上传成功！');
                } else {
                    console.error('图片上传失败', response.data.message);
                    alert('图片上传失败！');
                }
            } catch (error) {
                console.error('图片上传异常', error);
                alert('图片上传异常！'+error );
            }
        },
        async saveMd() {
            try {
              const config = {
                headers: {
                  'Content-Type': 'multipart/form-data',
                  Authorization: "Bearer " + localStorage.getItem("teri_token"), // 替换为您的授权信息变量
                }
              };

              // 构建FormData对象
              const formData = new FormData();
              const blob = new Blob([this.mdContent], { type: 'text/markdown' });
              formData.append('content', blob, 'content.md');

                // 发送请求到后端保存Markdown文档
                const response = await axios.post('/api/article/add', formData,config);
                if (response.data.success) {
                    console.log('文档保存成功');
                    alert('文档保存成功');
                } else {
                    console.error('文档保存失败');
                    alert('文档保存失败');
                }
            } catch (error) {
                console.error('文档保存异常', error);
                alert('文档保存异常' + error);
            }
        },
        submit() {
            this.$message.success("发布成功！");
            // 发布成功后，清空编辑器的内容
            this.content = '';
            localStorage.removeItem('markdownContent');
        },
    }
}
</script>

<style scoped>
.textUpload {
    margin: auto;
}
.container {
    width: 95%;
    margin: auto;
    padding-top: 30px;
}
.title {
    padding-bottom: 30px;
    text-align: center;
    font-size: 20px;
    letter-spacing: 1px;
    color: #333;
    font-weight: 600;
}
.el-button {
    font-size: 16px;
    margin-top: 30px;
    margin-left: 30px;
    width: 130px;
    height: 40px;
}
.margin-editor {
    margin-top: 20px;
}

</style>
