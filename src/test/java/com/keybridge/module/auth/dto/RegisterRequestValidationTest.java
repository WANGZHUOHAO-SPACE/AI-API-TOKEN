package com.keybridge.module.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void acceptsStrongRegistrationData() {
        RegisterRequest request = new RegisterRequest("student2026", "Strong@2026!", "软件工程小王");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsSimpleUsernameNumericNicknameAndWeakPassword() {
        RegisterRequest request = new RegisterRequest("1234", "123456", "12345");

        assertEquals(3, validator.validate(request).size());
    }
}
