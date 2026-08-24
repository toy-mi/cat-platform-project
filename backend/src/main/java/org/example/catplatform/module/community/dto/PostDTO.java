package org.example.catplatform.module.community.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PostDTO {
    private String title;
    private String content;
    private String images;          // 图片URL列表，可JSON
    private String locationDesc;
    private BigDecimal latitude;
    private BigDecimal longitude;
}