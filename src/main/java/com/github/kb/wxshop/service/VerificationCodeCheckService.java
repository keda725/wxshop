package com.github.kb.wxshop.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zuojiabin
 */
@Service
public class VerificationCodeCheckService {
    private Map<String, String> telNumberToCorrectCode = new ConcurrentHashMap<>();

    public void addCode(final String tel, final String correctCode) {
        this.telNumberToCorrectCode.put(tel, correctCode);
    }
}
