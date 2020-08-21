package com.github.kb.wxshop.controller;

import com.github.kb.wxshop.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zuojiabin
 */
@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;


    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/code")
    public void code(@RequestBody final TelAndCode telAndCode) {
        this.authService.sendVerificationCode(telAndCode.getTel());

    }

    @PostMapping("/login")
    public void login() {
        
    }

    public static class TelAndCode {
        private String code;
        private String Tel;

        public String getCode() {
            return this.code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public String getTel() {
            return this.Tel;
        }

        public void setTel(final String tel) {
            this.Tel = tel;
        }
    }
}
