package org.example.catplatform.module.adoption.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.catplatform.module.adoption.dto.ReviewDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;
import org.example.catplatform.module.adoption.dto.ApplyDTO;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdoptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // 插入测试用户（志愿者角色）
        jdbcTemplate.execute("INSERT INTO user (id, username, password, role) VALUES (100, 'testvolunteer66', '{noop}123', 'VOLUNTEER')");
        // 插入测试猫咪（外键依赖）
        jdbcTemplate.execute("INSERT INTO cat (id, name) VALUES (1, '测试猫')");
        // 插入待初审的领养申请
        jdbcTemplate.execute("INSERT INTO adoption_application (id, user_id, cat_id, status) VALUES (1, 100, 1, 0)");
        // 插入状态为已通过的申请（用于测试无效状态）
        jdbcTemplate.execute("INSERT INTO adoption_application (id, user_id, cat_id, status) VALUES (2, 100, 1, 1)");
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
//        jdbcTemplate.execute("DELETE FROM adoption_application WHERE id IN (1,2)");
//        jdbcTemplate.execute("DELETE FROM cat WHERE id = 1");
//        jdbcTemplate.execute("DELETE FROM user WHERE id = 100");
    }

    // 测试提交领养申请
    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "customUserDetailsService")
    void testApply_Success() throws Exception {
        // 1️⃣ 准备测试数据（DTO对象）
        ApplyDTO dto = new ApplyDTO();
        dto.setCatId(1L);
        dto.setApplicationData("{\"name\":\"张三\"}");

        // 2️⃣ 发送HTTP请求
        mockMvc.perform(post("/api/adoptions/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                // 3️⃣ 验证响应结果
                .andExpect(status().isOk())           // HTTP状态码200
                .andExpect(jsonPath("$.code").value(200))      // 业务code
                .andExpect(jsonPath("$.message").value("申请提交成功")); // 提示信息
    }

//    @Test
//    @WithUserDetails(value = "testvolunteer", userDetailsServiceBeanName = "customUserDetailsService")
//    void testFirstReview_Success() throws Exception {
//        ReviewDTO reviewDTO = new ReviewDTO();
//        reviewDTO.setRemark("初审通过");
//
//        mockMvc.perform(put("/api/adoptions/1/first-review?status=1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(reviewDTO)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.message").value("操作成功"));
//    }
//
//    @Test
//    @WithUserDetails(value = "testvolunteer", userDetailsServiceBeanName = "customUserDetailsService")
//    void testFirstReview_InvalidStatus() throws Exception {
//        ReviewDTO reviewDTO = new ReviewDTO();
//        reviewDTO.setRemark("初审通过");
//
//        mockMvc.perform(put("/api/adoptions/2/first-review?status=1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(reviewDTO)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(500))
//                .andExpect(jsonPath("$.message").value("只能初审待初审的申请"));
//    }
}