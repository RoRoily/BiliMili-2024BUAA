package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.article.ArticleStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticleStatsController {
    @Autowired
    private ArticleStatsService articleStatsService;
    /**
     * @param aid 专栏id
     */
    @PostMapping("/article/play/visitor")
    public CustomResponse newPlayWithVisitor(@RequestParam("aid") Integer aid) {
        articleStatsService.updateStats(aid,"play",true,1);
        return new CustomResponse();
    }
}
