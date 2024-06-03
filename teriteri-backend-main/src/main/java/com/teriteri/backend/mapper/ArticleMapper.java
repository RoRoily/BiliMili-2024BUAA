package com.teriteri.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teriteri.backend.pojo.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleMapper  extends BaseMapper<Article> {
    @Select("select aid from article where status = #{status}")
    List<Integer> getArticleIdsByStatus(Integer status);
}
