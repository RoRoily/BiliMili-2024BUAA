package com.teriteri.backend.controller;

import com.teriteri.backend.mapper.FavoriteVideoMapper;
import com.teriteri.backend.service.article.ArticleService;
import com.teriteri.backend.pojo.Article;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.service.video.VideoService;
import com.teriteri.backend.utils.RedisUtil;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.video.VideoStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


@RestController
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    //@Autowired
    //private FavoriteVideoMapper favoriteVideoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @GetMapping("/article/getone") //没有创建路径
    public CustomResponse getOneArticle(@RequestParam("aid") Integer aid) {
        //查询一个articlle
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> map = articleService.getArticleWithDataById(aid);
        if (map == null) {
            customResponse.setCode(404);
            customResponse.setMessage("特丽丽没找到个视频QAQ");
            return customResponse;
        }

        Article article = (Article) map.get("Article");
        //Video video = (Video) map.get("video");
        if (article.getStatus() != 1) {
            customResponse.setCode(404);
            customResponse.setMessage("特丽丽没找到个视频QAQ");
            return customResponse;
        }
        customResponse.setData(map);
        return customResponse;
    }
}
