package de.otto.prototype.validation;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(DataProviderRunner.class)
public class SecurePasswordValidatorTest {

    private SecurePasswordValidator testee;

    @DataProvider
    public static Object[][] invalidPasswordProvider() {
        return new Object[][]{
                {"111"},
                {"11111111111111111111111111111111"},
                {"invalid"},
                {""},
                {null},
        };
    }

    @Before
    public void setup() {
        testee = new SecurePasswordValidator();
        testee.initialize(implementAnnotationInterface("[0-9]+"));
    }

    private SecurePassword implementAnnotationInterface(String pattern) {
        AnnotationDescriptor<SecurePassword> descriptor = new AnnotationDescriptor<>(SecurePassword.class);
        descriptor.setValue("pattern", pattern);
        return AnnotationFactory.create(descriptor);
    }

    @Test
    @UseDataProvider("invalidPasswordProvider")
    public void shouldValidateUnsecurePasswords(String unsecurePassword) {
        assertThat(testee.isValid(unsecurePassword, null), is(false));
    }

    @Test
    public void shouldValidateSecurePassword() {
        testee.initialize(implementAnnotationInterface(".*"));
        assertThat(testee.isValid("securePassword", null), is(true));
    }
}
