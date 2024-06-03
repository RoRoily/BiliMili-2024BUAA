package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.article.ArticleReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticleReviewController {
    @Autowired
    private ArticleReviewService articleReviewService;
    /**
     * 获取所有要审核的专栏数量
     *
     */
    @GetMapping("/review/article/total")
    public CustomResponse getTotalArticle(@RequestParam("astatus") Integer status) {
        return articleReviewService.getTotalByStatus(status);
    }

    /**
     * 审核 分页查询对应状态专栏
     * @param status 状态 0待审核 1通过 2未通过
     * @param page  当前页
     * @param quantity  每页的数量
     * @return
     */
    @GetMapping("/review/video/getpage")
    public CustomResponse getVideos(@RequestParam("astatus") Integer status,
                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                    @RequestParam(value = "quantity", defaultValue = "10") Integer quantity) {
        return articleReviewService.getArticlesByPage(status, page, quantity);
    }
}
