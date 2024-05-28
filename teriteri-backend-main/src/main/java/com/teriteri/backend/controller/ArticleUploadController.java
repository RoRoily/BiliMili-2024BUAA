package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.dto.ArticleUploadDTO;
import com.teriteri.backend.service.article.ArticleUploadService;
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
     * 添加视频投稿
     * @param title 投稿标题
     * @param type  视频类型 1自制 2转载
     * @param auth  作者声明 0不声明 1未经允许禁止转载
     * @param duration 视频总时长
     * @param mcid  主分区ID
     * @param scid  子分区ID
     * @param tags  标签
     * @param descr 简介
     * @return  响应对象
     */
    @PostMapping("/article/add")
    public CustomResponse addArticle(
            //@RequestParam("cover") MultipartFile cover,
            //@RequestParam("title") String title,
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
            return new CustomResponse(200,url,null);
        } catch (Exception e) {
            e.printStackTrace();
            return new CustomResponse(500, "封面上传失败", null);
        }
    }
}
