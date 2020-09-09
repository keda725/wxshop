package com.github.kb.wxshop.service;

import com.github.kb.wxshop.controller.AuthController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class TelVerificationServiceTest {
    public static AuthController.TelAndCode VALID_PARAMETER = new AuthController.TelAndCode(null, "12345678901");
    public static AuthController.TelAndCode VALID_PARAMETER_CODE = new AuthController.TelAndCode("000000", "12345678901");
    public static AuthController.TelAndCode EMPTY_TEL = new AuthController.TelAndCode(null, null);

    @Test
    public void returnTrueIfValid() {
        Assertions.assertTrue(new TelVerificationService().verifyTelParameter(VALID_PARAMETER));
    }


    @Test
    public void returnFalseIfNoTel() {
        Assertions.assertFalse(new TelVerificationService().verifyTelParameter(EMPTY_TEL));
        Assertions.assertFalse(new TelVerificationService().verifyTelParameter(null));
    }
}
