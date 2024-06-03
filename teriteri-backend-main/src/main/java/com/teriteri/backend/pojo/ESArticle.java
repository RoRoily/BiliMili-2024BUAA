package com.teriteri.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESArticle {
    private Integer aid;
    private Integer uid;
    private Integer status;
    private String title;
}
