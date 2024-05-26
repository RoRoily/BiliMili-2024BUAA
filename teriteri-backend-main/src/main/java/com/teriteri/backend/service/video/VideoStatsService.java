package com.teriteri.backend.service.video;

import com.teriteri.backend.pojo.VideoStats;

public interface VideoStatsService {
    /**
     * 根据视频ID查询视频常变数据
     * @param vid 视频ID
     * @return 视频数据统计
     */
    VideoStats getVideoStatsById(Integer vid);

    /**
     * 更新指定字段
     * @param vid   视频ID
     * @param column    对应数据库的列名
     * @param increase  是否增加，true则增加 false则减少
     * @param count 增减数量 一般是1，只有投币可以加2
     */
    void updateStats(Integer vid, String column, boolean increase, Integer count);

    /**
     * 同时更新点赞和点踩
     * @param vid   视频ID
     * @param addGood   是否点赞，true则good+1&bad-1，false则good-1&bad+1
     */
    void updateGoodAndBad(Integer vid, boolean addGood);

    /**
     * 更新视频的硬币数
     * @param vid   视频ID
     * @param coin   硬币个数
     */
    void updateCoin(Integer vid, Integer coin);

    /**
     * 更新新数据时，将原数据存入旧数据
     * @param vid   视频ID
     */
    void updateOld(Integer vid);

    /**
     * 定时更新旧值
     * @param vid   视频ID
     */
    void updateOldTiming(Integer vid);

    /**
     * 更新用户之间的关注，要求用户1关注用户2
     * @param uid1   用户1ID
     * @param uid2   硬币个数
     */
    void updateFollow(Integer uid1, Integer uid2);
}
