package de.otto.prototype.service;

import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import org.hibernate.validator.HibernateValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PasswordServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private PasswordService testee;

    @Before
    public void setUp() throws Exception {
        LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.setProviderClass(HibernateValidator.class);
        validatorFactory.afterPropertiesSet();

        testee = new PasswordService(userService, validatorFactory);
    }

    @Test
    public void shouldReturnUpdatedUser() throws Exception {
        final long userId = 1234L;
        final String password = "somePassword";
        final User userToUpdate = User.builder().id(userId).lastName("Mustermann").login(Login.builder().build()).build();
        final User updatedUser = User.builder().id(userId).lastName("Mustermann").login(Login.builder().password(password).build()).build();
        when(userService.findOne(userId)).thenReturn(of(userToUpdate));
        when(userService.update(updatedUser)).thenReturn(updatedUser);

        final User persistedUser = testee.update(userId, password);
        assertThat(persistedUser.getLogin().getPassword(), is(password));
        assertThat(persistedUser.getId(), is(userId));
        verify(userService, times(1)).findOne(userId);
        verify(userService, times(1)).update(updatedUser);
        verifyNoMoreInteractions(userService);
    }

    @Test(expected = NotFoundException.class)
    public void shouldReturnNotFoundExceptionIfIdIsNull() throws Exception {

        try {
            testee.update(null, "somePassword");
        } catch (InvalidUserException e) {
            assertThat(e.getMessage(), is("id not found"));
            verifyNoMoreInteractions(userService);
            throw e;
        }
    }

    @Test(expected = NotFoundException.class)
    public void shouldReturnNotFoundExceptionIfUnknownId() throws Exception {
        final long userId = 1234L;
        when(userService.findOne(userId)).thenReturn(empty());

        try {
            testee.update(userId, "somePassword");
        } catch (InvalidUserException e) {
            assertThat(e.getMessage(), is("id not found"));
            verifyNoMoreInteractions(userService);
            throw e;
        }
    }

    @Test
    public void shouldReturnFalseForInsecurePassword() {
        Boolean result = testee.checkPassword("unsec");
        assertThat(result, is(false));
    }

    @Test
    public void shouldReturnTrueForSecurePassword() {
        Boolean result = testee.checkPassword("securePassword");
        assertThat(result, is(true));
    }

}