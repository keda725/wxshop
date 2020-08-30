package com.github.kb.wxshop.controller;

import com.github.kb.wxshop.entity.LoginResponse;
import com.github.kb.wxshop.service.AuthService;
import com.github.kb.wxshop.service.TelVerificationService;
import com.github.kb.wxshop.service.UserContext;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;


/**
 * @author zuojiabin
 */
@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;
    private final TelVerificationService telVerificationService;


    public AuthController(AuthService authService, TelVerificationService telVerificationService) {
        this.authService = authService;
        this.telVerificationService = telVerificationService;
    }

    @PostMapping("/code")
    public void code(@RequestBody TelAndCode telAndCode, HttpServletResponse response) {
        if (telVerificationService.verifyTelParameter(telAndCode)) {
            authService.sendVerificationCode(telAndCode.getTel());
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
    }

    @PostMapping("/login")
    public void login(@RequestBody TelAndCode telAndCode) {
        UsernamePasswordToken token = new UsernamePasswordToken(telAndCode.getTel(), telAndCode.getCode());
        token.setRememberMe(true);
        SecurityUtils.getSubject().login(token);
    }

    @PostMapping("/logout")
    public void logout() {
        SecurityUtils.getSubject().logout();
    }



    @GetMapping("/status")
    public Object loginStatus() {
        if (UserContext.getCurrentUser() == null) {
            return LoginResponse.notLogin();
        } else {
            return LoginResponse.login(UserContext.getCurrentUser());
        }
//        System.out.println(SecurityUtils.getSubject().getPrincipal());
//        return null;
    }

    public static class TelAndCode {
        private String code;
        private String tel;

        public TelAndCode(String code, String tel) {
            this.code = code;
            this.tel = tel;
        }

        public String getCode() {
            return this.code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public String getTel() {
            return this.tel;
        }

        public void setTel(final String tel) {
            this.tel = tel;
        }
    }
}
