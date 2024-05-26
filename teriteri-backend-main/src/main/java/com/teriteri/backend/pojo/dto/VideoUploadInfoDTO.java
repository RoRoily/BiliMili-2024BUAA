package com.teriteri.backend.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoUploadInfoDTO {
    private Integer uid;
    private String hash;
    private String title;
    private Integer type;
    private Integer auth;
    private Double duration;
    private String mcId;//主分区
    private String scId;//子分区
    private String tags;//标签
    private String descr;//描述
    private String coverUrl;//封面url
}
