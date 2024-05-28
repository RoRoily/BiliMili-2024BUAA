package com.teriteri.backend.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleUploadDTO {
    private Integer uid;
    //private String title;
    private MultipartFile content;
    //private Integer type;
    //private Integer auth;
   // private Double duration;
   // private String mcId;
    //private String scId;
    //private String tags;
    //private String descr;
    //private String coverUrl;
}