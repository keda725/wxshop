package com.github.kb.wxshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kb.wxshop.WxshopApplication;
import com.github.kb.wxshop.entity.LoginResponse;
import com.github.kevinsawicki.http.HttpRequest;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static com.github.kb.wxshop.service.TelVerificationServiceTest.VALID_PARAMETER;
import static com.github.kb.wxshop.service.TelVerificationServiceTest.VALID_PARAMETER_CODE;
import static java.net.HttpURLConnection.HTTP_OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class AbstractIntegrationTest {
    @Autowired
    Environment environment;

    @Value("${spring.datasource.url}")
    private String databaseUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    @BeforeEach
    public void setUp() {
        //在每个测试开始前 执行一次flyway:clean flyway:migrate
        ClassicConfiguration conf = new ClassicConfiguration();
        conf.setDataSource(databaseUrl, username, password);
        Flyway flyway = new Flyway(conf);
        flyway.clean();
        flyway.migrate();

    }

    public String getUrl(String apiName) {
        // 获取集成测试的端口号
        return "http://localhost:" + environment.getProperty("local.server.port") + apiName;
    }

    public String loginAndGetCookie() throws JsonProcessingException {
        // 最开始默认情况下 访问/api/status 处于为登陆状态
        String statusResponse = doHttpRequest("/api/v1/status", true, null, null).body;
        LoginResponse response = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertFalse(response.isLogin());

        // 发送验证码
        int responseCode = doHttpRequest("/api/v1/code", false, VALID_PARAMETER, null).code;
        Assertions.assertEquals(HTTP_OK, responseCode);

        // 带着验证码进行登陆 得到Cookie
        Map<String, List<String>> responseHeaders = doHttpRequest("/api/v1/login", false, VALID_PARAMETER_CODE, null).headers;
        List<String> setCookie = responseHeaders.get("Set-Cookie");
        String sessionId = getSessionIdFromSetCookie(setCookie.stream().filter(cookie->cookie.contains("JSESSIONID"))
                .findFirst()
                .get());
        return sessionId;
    }

    public static ObjectMapper objectMapper = new ObjectMapper();

    public class HttpResponse {
        public int code;
        public String body;
        Map<String, List<String>> headers;

        HttpResponse(int code, String body, Map<String, List<String>> headers) {
            this.code = code;
            this.body = body;
            this.headers = headers;
        }
    }

    public HttpResponse doHttpRequest(String apiName, boolean isGet, Object requestBody, String cookie) throws JsonProcessingException {
        HttpRequest request = isGet ? HttpRequest.get(getUrl(apiName)) : HttpRequest.post(getUrl(apiName));
        if (cookie != null) {
            request.header("Cookie", cookie);
        }
        request.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE);
        if (requestBody != null) {
            request.send(objectMapper.writeValueAsString(requestBody));
        }

        return new HttpResponse(request.code(), request.body(), request.headers());
    }

    protected String getSessionIdFromSetCookie(String setCookie) {
        //JSESSIONID=974b0c15-dd10-4d91-b3e6-e32fd73fde68; Path=/; HttpOnly; SameSite=lax -> JSESSIONID=974b0c15-dd10-4d91-b3e6-e32fd73fde68;
        int semiColonIndex = setCookie.indexOf(";");
        return setCookie.substring(0, semiColonIndex);
    }
}
