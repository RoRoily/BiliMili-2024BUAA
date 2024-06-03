package com.teriteri.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.FavoriteVideoMapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.mapper.VideoStatsMapper;
import com.teriteri.backend.pojo.*;
import com.teriteri.backend.service.article.ArticleService;
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

import java.util.*;
import java.util.stream.Collectors;


@RestController
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoStatsMapper videoStatsMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 更新专栏状态，包括过审、不通过、删除，其中审核相关需要管理员权限，删除可以是管理员或者投稿用户
     * @param aid 专栏ID
     * @param status 要修改的状态，1通过 2不通过 3删除
     * @return 无data返回 仅返回响应
     */
    @PostMapping("/article/change/status")
    public CustomResponse updateStatus(@RequestParam("aid") Integer aid,
                                       @RequestParam("status") Integer status) {
        try {
            return articleService.updateArticleStatus(aid, status);
        } catch (Exception e) {
            e.printStackTrace();
            return new CustomResponse(500, "操作失败", null);
        }
    }

    /**
     * 获取一个专栏的详细信息
     * @param aid 专栏aid
     * @return
     */

    @GetMapping("/article/getone") //没有创建路径
    public CustomResponse getOneArticle(@RequestParam("aid") Integer aid) {
        //查询一个articlle
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> map = articleService.getArticleWithDataById(aid);
        if (map == null) {
            customResponse.setCode(404);
            customResponse.setMessage("特丽丽没找到这个专栏QAQ");
            return customResponse;
        }

        Article article = (Article) map.get("Article");
        if (article.getStatus() != 1) {
            customResponse.setCode(404);
            customResponse.setMessage("特丽丽没找到这个专栏QAQ");
            return customResponse;
        }
        customResponse.setData(map);
        return customResponse;
    }

    /**
     * 获取专栏对应的视频
     * @param aid 专栏aid
     */
    @GetMapping("/column/video-data")
    public CustomResponse getAllVideoInfo(@RequestParam("aid") Integer aid){
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> map = articleService.getArticleWithDataById(aid);
        if (map == null) {
            customResponse.setCode(404);
            customResponse.setMessage("特丽丽没找到这个专栏QAQ");
            return customResponse;
        }
        Article article = (Article) map.get("Article");
        if (article.getStatus() != 1) {
            customResponse.setCode(404);
            customResponse.setMessage("特丽丽没找到这个专栏QAQ");
            return customResponse;
        }
        List<Integer> vids = Arrays.stream(article.getVid().split(",")).map(Integer::valueOf).collect(Collectors.toList());
        List<String> titles = new ArrayList<>();
        List<Double> durations = new ArrayList<>();
        List<Integer> playCounts = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        for(Integer vid: vids){
            QueryWrapper<Video> videoQueryWrapper = new QueryWrapper<>();
            videoQueryWrapper.eq("vid", vid);
            QueryWrapper<VideoStats> videoStatsQueryWrapper = new QueryWrapper<>();
            videoStatsQueryWrapper.eq("vid", vid);
            Video video = videoMapper.selectOne(videoQueryWrapper);
            VideoStats videoStats = videoStatsMapper.selectOne(videoStatsQueryWrapper);
            titles.add(video.getTitle());
            durations.add(video.getDuration());
            urls.add(video.getCoverUrl());
            playCounts.add(videoStats.getPlay());
        }
        Map<String,Object>dataMap = new HashMap<>();
        dataMap.put("vid",vids);
        dataMap.put("title",titles);
        dataMap.put("duration",durations);
        dataMap.put("url",urls);
        dataMap.put("playCount",playCounts);
        customResponse.setData(dataMap);
        return customResponse;
    }
}
