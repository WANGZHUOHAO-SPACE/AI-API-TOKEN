package com.keybridge.module.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangePasswordRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void acceptsStrongNewPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("Old@Password1", "New@Password2");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsWeakNewPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("Old@Password1", "123456");

        assertFalse(validator.validate(request).isEmpty());
    }
}
