package com.github.kb.wxshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.kb.wxshop.WxshopApplication;
import com.github.kb.wxshop.entity.LoginResponse;
import com.github.kevinsawicki.http.HttpRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import static com.github.kb.wxshop.service.TelVerificationServiceTest.VALID_PARAMETER;
import static java.net.HttpURLConnection.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CodeIntegrationTest extends AbstractIntegrationTest{

    @Test
    public void loginLogoutTest() throws JsonProcessingException {
        String sessionId = loginAndGetCookie().cookie;

        // 带着Cookie访问 /api/status 应该处于登陆状态
        String statusResponse = doHttpRequest("/api/v1/status", true, null, sessionId).body;
        LoginResponse response = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertTrue(response.isLogin());
        Assertions.assertEquals(VALID_PARAMETER.getTel(), response.getUser().getTel());

        // 调用/api/logout
        //注销登录 也需要带Cookie
        doHttpRequest("/api/v1/logout", false, null, sessionId);

        // 再次带着Cookie访问/api/status 恢复成未登陆状态
        statusResponse = doHttpRequest("/api/v1/status", true, null, sessionId).body;

        response = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertFalse(response.isLogin());


    }

    @Test
    public void returnHttpOKWhenParameterIsCorrect() throws JsonProcessingException {
        int responseCode = HttpRequest.post(getUrl("/api/v1/code"))
                                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                                      .accept(MediaType.APPLICATION_JSON_VALUE)
                                      .send(objectMapper.writeValueAsString(VALID_PARAMETER))
                                      .code();
        Assertions.assertEquals(HTTP_OK, responseCode);
    }

    @Test
    public void returnHttpBadRequestWhenParameterIsCorrect() throws JsonProcessingException {
        int responseCode = HttpRequest.post(getUrl("/api/v1/code"))
                                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                                      .accept(MediaType.APPLICATION_JSON_VALUE)
                                      .send(objectMapper.writeValueAsString(TelVerificationServiceTest.EMPTY_TEL))
                                      .code();
        System.out.println(responseCode);

        Assertions.assertEquals(HTTP_BAD_REQUEST, responseCode);
    }

    @Test
    public void returnUnauthorizedIfNotLogin() throws JsonProcessingException {
        int responseCode = HttpRequest.post(getUrl("/api/v1/any"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .send(objectMapper.writeValueAsString(TelVerificationServiceTest.EMPTY_TEL))
                .code();

        Assertions.assertEquals(HTTP_UNAUTHORIZED, responseCode);
    }

    @Test
    public void return404IfGoodsToDeletedNotExist() throws JsonProcessingException {
//        String cookie = loginAndGetCookie();
//        HttpResponse response = doHttpRequest(
//                "/api/v1/goods/12345678",
//                "DELETE",
//                null,
//                cookie);
    }


}

