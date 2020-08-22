package com.github.kb.wxshop.service;

import org.springframework.stereotype.Service;

@Service
public class MockSmsCodeService implements SmsCodeService {
    //TODO:1,暴力破解 2,流量控制


    @Override
    public String sendSmsCode(final String tel) {
        return "000000";
    }
}
