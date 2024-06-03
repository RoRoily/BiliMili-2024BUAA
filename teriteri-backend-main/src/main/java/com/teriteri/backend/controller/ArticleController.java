package com.teriteri.backend.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.ArticleMapper;
import com.teriteri.backend.mapper.FavoriteVideoMapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.mapper.VideoStatsMapper;
import com.teriteri.backend.pojo.*;
import com.teriteri.backend.service.article.ArticleService;
import com.teriteri.backend.service.comment.CommentService;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.service.video.VideoService;
import com.teriteri.backend.utils.RedisUtil;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.video.VideoStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

    @Autowired
    private OSS ossClient;

    @Autowired
    private CommentService commentService;
    @Autowired
    private ArticleMapper articleMapper;

    @Value("${oss.bucket}")
    private String OSS_BUCKET;

    @Value("${oss.bucketUrl}")
    private String OSS_BUCKET_URL;

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

        Article article = (Article) map.get("article");
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

    /**
     * 接收aid，返回文章的contentUrl,标题，coverUrl
     * 以下是传输参数
     * @param aid   对应视频ID
     * @return  文件
     * */
    @GetMapping("/article/get")

    public CustomResponse getArticleById(@RequestParam("aid") Integer aid
    ) {
        CustomResponse customResponse = new CustomResponse();
        Article article = null;
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("aid", aid).ne("status", 3);
        article = articleMapper.selectOne(queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("coverUrl", article.getCoverUrl());
        map.put("contentUrl",article.getContentUrl());
        map.put("title",article.getTitle());
        customResponse.setData(map);
        return customResponse;
    }

    /**
     * 接收aid，返回文章的contentUrl,标题，coverUrl
     * 以下是传输参数
     * @param aid   对应视频ID
     * @return  文件
     *
     *
     * */
    @GetMapping("/column/markdown")
    public CustomResponse getArticleContentByVid(@RequestParam("aid") Integer aid) {
        CustomResponse customResponse = new CustomResponse();
        Article article = null;
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("aid", aid).ne("status", 3);
        article = articleMapper.selectOne(queryWrapper);
        if (article == null) {
            customResponse.setCode(404);
            customResponse.setMessage("Article not found");
            return customResponse;
        }
        String contentUrl = article.getContentUrl();
        String bucketName = OSS_BUCKET; // 请根据实际情况修改
        String key = contentUrl.replace(OSS_BUCKET_URL, ""); // 获取对象的key

        try {
            OSSObject ossObject = ossClient.getObject(bucketName, key);
            BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            Map<String, Object> map = new HashMap<>();
            map.put("coverUrl", article.getCoverUrl());
            map.put("content", content.toString());
            map.put("title", article.getTitle());

            customResponse.setData(map);
            customResponse.setCode(200);
            customResponse.setMessage("Success");
        } catch (Exception e) {
            customResponse.setCode(500);
            customResponse.setMessage("Failed to retrieve content from OSS");
        }

        return customResponse;
    }

    /**
     * 接收aid，收藏这个aid下的所有关联视频
     * 以下是传输参数
     * @param aid   对应视频ID
     * @return  文件
     * */
    @GetMapping("/column/favoriteVideo")
    public CustomResponse favoriteRelatedVideo(@RequestParam("aid") Integer aid
    ) {
        CustomResponse customResponse = new CustomResponse();
        Article article = null;
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("aid", aid).ne("status", 3);
        article = articleMapper.selectOne(queryWrapper);
        Map<String, Object> map = new HashMap<>();
        if(article == null){
            return new CustomResponse(500,"未找到文章对应的aid",null);
        }
        List<Integer> videoList = new ArrayList<>();
        String[] videos = article.getVid().split(",");
        for (String s : videos) {
            try {
                videoList.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                // 处理可能的转换异常
                System.err.println("Invalid number format: " + s);
            }
        }
        //依次收藏视频
        for(Integer vid:videoList){
        }
        map.put("coverUrl", article.getCoverUrl());
        map.put("contentUrl",article.getContentUrl());
        map.put("title",article.getTitle());
        customResponse.setData(map);
        return customResponse;
    }
}
