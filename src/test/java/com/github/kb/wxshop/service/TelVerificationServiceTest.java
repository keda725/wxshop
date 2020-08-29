package com.github.kb.wxshop.service;

import com.github.kb.wxshop.controller.AuthController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class TelVerificationServiceTest {
    private static AuthController.TelAndCode VALID_PARAMETER = new AuthController.TelAndCode(null, "13800000000");

    @Test
    public void returnTrueIfValid() {
        Assertions.assertTrue(new TelVerificationService().verifyTelParameter(VALID_PARAMETER));
    }

}
