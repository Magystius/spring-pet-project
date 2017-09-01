package de.otto.prototype.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

class SecurePasswordValidatorTest {

    @Mock
    private SecurePassword securePassword;

    private SecurePasswordValidator testee;

    @BeforeEach
    void setup() {
        initMocks(this);
        given(securePassword.pattern()).willReturn("[0-9]+");
        testee = new SecurePasswordValidator();
        testee.initialize(securePassword);
    }

    @ParameterizedTest(name = "{index} -> {0}")
    @ValueSource(strings = {"111", "11111111111111111111111111111111", "invalid", ""})
    @DisplayName("should return false when unsecure password is")
    void shouldValidateInsecurePasswords(String insecurePassword) {
        assertThat(testee.isValid(insecurePassword, null), is(false));
    }

    @Test
    @DisplayName("should return false for null password")
    void shouldValidateNullPassword() {
        assertThat(testee.isValid(null, null), is(false));
    }

    @Test
    @DisplayName("should return true for a secure password")
    void shouldValidateSecurePassword() {
        given(securePassword.pattern()).willReturn(".*");
        testee.initialize(securePassword);

        assertThat(testee.isValid("securePassword", null), is(true));
    }
}
