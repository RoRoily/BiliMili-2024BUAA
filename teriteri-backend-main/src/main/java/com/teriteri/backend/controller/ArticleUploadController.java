package com.teriteri.backend.controller;

import com.teriteri.backend.mapper.ArticleMapper;
import com.teriteri.backend.pojo.Article;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.dto.ArticleUploadDTO;
import com.teriteri.backend.service.article.ArticleUploadService;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.utils.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ArticleUploadController {
    @Autowired
    private ArticleUploadService articleUploadService;
    @Autowired
    private OssUtil ossUtil;

    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private ArticleMapper articleMapper;



    @PostMapping("/image/add")
    public CustomResponse addImage(
            @RequestParam("image") MultipartFile image

    ){
        try {
            String url = ossUtil.uploadImage(image,"articleCover");
            return new CustomResponse(200,url,null);
        } catch (Exception e) {
            e.printStackTrace();
            return new CustomResponse(500, "封面上传失败", null);
        }

    }

    /**
     * 添加文章投稿
     * @param title 投稿标题
     * @param cover  文章封面
     * @param content  文章内容的markdown文件
     * @return  响应对象
     */
    @PostMapping("/article/add/all")
    public CustomResponse addAllArticle(
            @RequestParam("cover") MultipartFile cover,
            //@RequestParam("cover") MultipartFile cover,
            //@RequestParam("title") String title,
            @RequestParam("title") String title,
            @RequestParam("vid") String vid,
            @RequestParam("content") MultipartFile content
            //@RequestParam("type") Integer type,
            //@RequestParam("auth") Integer auth,
            //@RequestParam("duration") Double duration,
            //@RequestParam("mcid") String mcid,
            //@RequestParam("scid") String scid,
            //@RequestParam("tags") String tags,
            //@RequestParam("descr") String descr
    ) {
        //ArticleUploadDTO articleUploadDTO = new ArticleUploadDTO(null, title, content,type, auth, duration, mcid, scid, tags, descr, null);
        ArticleUploadDTO articleUploadDTO = new ArticleUploadDTO(null, content);
        try {
            //return articleUploadService.addArticle(articleUploadDTO);
            String url = ossUtil.uploadArticle(content);
            String url2 = ossUtil.uploadImage(cover,"articleCover");
            Article article = new Article();
            article.setTitle(title);
            article.setVid(vid);
            article.setContentUrl(url);
            article.setCoverUrl(url2);
            article.setStatus(0);
            article.setUid(currentUser.getUserId()); // 假设 currentUser 对象可以获取当前用户的 ID
            articleMapper.insert(article);
            return new CustomResponse(200,"文章上传成功",article.getAid().toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new CustomResponse(500, "封面上传失败", null);
        }
    }
}
