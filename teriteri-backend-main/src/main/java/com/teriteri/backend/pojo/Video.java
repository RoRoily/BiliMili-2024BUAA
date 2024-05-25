package com.teriteri.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Video {
    @TableId(type = IdType.AUTO) //MyBatis-Plus注解，指定aid字段为表的主键，且主键类型为自增。
    private Integer vid; //文章的唯一标识码
    private Integer uid;
    private String title;
    private Integer type; //可暂不实现
    private Integer auth;
    private Double duration;
    private String mcId; //主分区id
    private String scId; //子分区id
    private String tags;
    //private String descr; //描述
    private String coverUrl; //封面文件对应的字符串
    private String VideoUrl; //文章文件对应的字符串
    private Integer status;     // 0审核中 1通过审核 2打回整改（指投稿信息不符） 3视频违规删除（视频内容违规）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai") //指定deleteDate字段的JSON格式和时区
    private Date uploadDate; //上传时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date deleteDate; //删除时间
}
