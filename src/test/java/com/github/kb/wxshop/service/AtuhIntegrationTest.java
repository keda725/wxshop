package com.github.kb.wxshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kb.wxshop.WxshopApplication;
import com.github.kevinsawicki.http.HttpRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
public class AtuhIntegrationTest {
    @Autowired
    Environment environment;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void loginLogoutTest() throws JsonProcessingException {
        // 最开始默认情况下 访问/api/status 处于为登陆状态
        // 发送验证码
        // 带着验证码进行登陆 得到Cookie
        // 带着Cookie访问 /api/status 应该处于登陆状态
        // 调用/api/logout
        // 再次带着Cookie访问/api/status 恢复成未登陆状态
        String statusResponse = HttpRequest.get(getUrl("/api/status"))
                                           .contentType(MediaType.APPLICATION_JSON_VALUE)
                                           .accept(MediaType.APPLICATION_JSON_VALUE)
                                           .body();

        Map<String, Object> response = objectMapper.readValue(statusResponse, Map.class);

        Assertions.assertFalse((Boolean) response.get("login"));

        int responseCode = HttpRequest.post(getUrl("/api/code"))
                                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                                      .accept(MediaType.APPLICATION_JSON_VALUE)
                                      .send(objectMapper.writeValueAsString(TelVerificationServiceTest.VALID_PARAMETER))
                                      .code();
        Assertions.assertEquals(HTTP_OK, responseCode);

        Map<String, List<String>> responseHeaders = HttpRequest.post(getUrl("/api/code"))
                                                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                               .accept(MediaType.APPLICATION_JSON_VALUE)
                                                               .send(objectMapper.writeValueAsString(TelVerificationServiceTest.VALID_PARAMETER_CODE))
                                                               .headers();

        List<String> setCookie = responseHeaders.get("Set-Cookie");
        Assertions.assertNotNull(setCookie);


    }

    @Test
    public void returnHttpOKWhenParameterIsCorrect() throws JsonProcessingException {
        int responseCode = HttpRequest.post(getUrl("/api/code"))
                                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                                      .accept(MediaType.APPLICATION_JSON_VALUE)
                                      .send(objectMapper.writeValueAsString(TelVerificationServiceTest.VALID_PARAMETER))
                                      .code();

        Assertions.assertEquals(HTTP_OK, responseCode);
    }

    @Test
    public void returnHttpBadRequestWhenParameterIsCorrect() throws JsonProcessingException {
        int responseCode = HttpRequest.post(getUrl("/api/code"))
                                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                                      .accept(MediaType.APPLICATION_JSON_VALUE)
                                      .send(objectMapper.writeValueAsString(TelVerificationServiceTest.EMPTY_TEL))
                                      .code();

        Assertions.assertEquals(HTTP_BAD_REQUEST, responseCode);
    }

    private String getUrl(String apiName) {
        // 获取集成测试的端口号
        return "http://localhost:" + environment.getProperty("local.server.port") + apiName;
    }
}

