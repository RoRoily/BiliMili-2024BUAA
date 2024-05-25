package com.teriteri.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teriteri.backend.pojo.ArticleStats;
import com.teriteri.backend.pojo.VideoStats;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleStatsMapper extends BaseMapper<ArticleStats> {
}
