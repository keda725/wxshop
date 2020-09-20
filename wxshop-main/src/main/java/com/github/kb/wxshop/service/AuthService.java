package com.github.kb.wxshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zuojiabin
 */
@Service
public class AuthService {
    private final UserService userService;
    private final VerificationCodeCheckService verificationCodeCheckService;
    private final SmsCodeService smsCodeService;

    @Autowired
    public AuthService(final UserService userService, final VerificationCodeCheckService verificationCodeCheckService, final SmsCodeService smsCodeService) {
        this.userService = userService;
        this.verificationCodeCheckService = verificationCodeCheckService;
        this.smsCodeService = smsCodeService;
    }

    public void sendVerificationCode(final String tel) {
        this.userService.createUserIfNotExists(tel);
        final String correctCode = this.smsCodeService.sendSmsCode(tel);
        this.verificationCodeCheckService.addCode(tel, correctCode);


    }
}
