package org.example.catplatform.module.community.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Long postId;
    private String content;
}