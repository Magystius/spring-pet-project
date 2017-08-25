package de.otto.prototype.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import de.otto.prototype.controller.handlers.ControllerValidationHandler;
import de.otto.prototype.controller.representation.UserValidationEntryRepresentation;
import de.otto.prototype.controller.representation.UserValidationRepresentation;
import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.of;
import static de.otto.prototype.controller.UserController.URL_USER;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(DataProviderRunner.class)
public class UserControllerTest {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final Locale LOCALE = LocaleContextHolder.getLocale();

    private static final String validUserId = "someUserId";

    private static final Login validLogin =
            Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
    private static final Login validLoginWithId =
            Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
    private static final User validMinimumUser =
            User.builder().lastName("Mustermann").firstName("Max").age(30).login(validLogin).build();
    private static final User validMinimumUserWithId =
            User.builder().id(validUserId).lastName("Mustermann").firstName("Max").age(30).login(validLoginWithId).build();

    private static MessageSource messageSource;

    private MockMvc mvc;

    private UserService userService;

    @DataProvider
    public static Object[][] invalidNewUserProvider() {
        return new Object[][]{
                {validMinimumUser.toBuilder().id(validUserId).build(), buildUVRep(of(buildUVERep("error.id.new")))},
                {validMinimumUser.toBuilder().firstName("a").build(), buildUVRep(of(buildUVERep("error.name.range")))},
                {validMinimumUser.toBuilder().firstName("").build(), buildUVRep(of(buildUVERep("error.name.empty"), buildUVERep("error.name.range")))},
                {validMinimumUser.toBuilder().secondName("a").build(), buildUVRep(of(buildUVERep("error.name.range")))},
                {validMinimumUser.toBuilder().lastName("a").build(), buildUVRep(of(buildUVERep("error.name.range")))},
                {validMinimumUser.toBuilder().lastName("").build(), buildUVRep(of(buildUVERep("error.name.empty"), buildUVERep("error.name.range")))},
                {validMinimumUser.toBuilder().age(15).build(), buildUVRep(of(buildUVERep("error.age.young")))},
                {validMinimumUser.toBuilder().age(200).build(), buildUVRep(of(buildUVERep("error.age.old")))},
                {validMinimumUser.toBuilder().login(validLogin.toBuilder().mail("keineMail").build()).build(), buildUVRep(of(buildUVERep("error.mail.invalid")))},
                {validMinimumUser.toBuilder().login(validLogin.toBuilder().password("").build()).build(), buildUVRep(of(buildUVERep("error.password.empty"), buildUVERep("error.password")))},
                {validMinimumUser.toBuilder().bio("<script>alert(\"malicious code\")</script>").build(), buildUVRep(of(buildUVERep("error.bio.invalid")))}
        };
    }

    @DataProvider
    public static Object[][] invalidExistingUserProvider() {
        return new Object[][]{
                {validMinimumUserWithId.toBuilder().id(null).build(), buildUVRep(of((buildUVERep("error.id.existing"))))},
                {validMinimumUserWithId.toBuilder().firstName("a").build(), buildUVRep(of(buildUVERep("error.name.range")))},
                {validMinimumUserWithId.toBuilder().firstName("").build(), buildUVRep(of(buildUVERep("error.name.range"), buildUVERep("error.name.empty")))},
                {validMinimumUserWithId.toBuilder().secondName("a").build(), buildUVRep(of(buildUVERep("error.name.range")))},
                {validMinimumUserWithId.toBuilder().lastName("a").build(), buildUVRep(of(buildUVERep("error.name.range")))},
                {validMinimumUserWithId.toBuilder().lastName("").build(), buildUVRep(of(buildUVERep("error.name.range"), buildUVERep("error.name.empty")))},
                {validMinimumUserWithId.toBuilder().age(15).build(), buildUVRep(of(buildUVERep("error.age.young")))},
                {validMinimumUserWithId.toBuilder().age(200).build(), buildUVRep(of(buildUVERep("error.age.old")))},
                {validMinimumUserWithId.toBuilder().login(validLoginWithId.toBuilder().mail("keineMail").build()).build(), buildUVRep(of(buildUVERep("error.mail.invalid")))},
                {validMinimumUserWithId.toBuilder().login(validLoginWithId.toBuilder().password("").build()).build(), buildUVRep(of(buildUVERep("error.password.empty"), buildUVERep("error.password")))},
                {validMinimumUserWithId.toBuilder().bio("<script>alert(\"malicious code\")</script>").build(), buildUVRep(of(buildUVERep("error.bio.invalid")))}
        };
    }

    private static UserValidationEntryRepresentation buildUVERep(String msgCode) {
        initMessageSource();
        String msg = messageSource.getMessage(msgCode, null, LOCALE);
        return UserValidationEntryRepresentation.builder().attribute("user").errorMessage(msg).build();
    }

    private static UserValidationRepresentation buildUVRep(List<UserValidationEntryRepresentation> errors) {
        return UserValidationRepresentation.builder().errors(errors).build();
    }

    private static void initMessageSource() {
        if (messageSource == null) {
            ReloadableResourceBundleMessageSource messageBundle = new ReloadableResourceBundleMessageSource();
            messageBundle.setBasename("classpath:messages/messages");
            messageBundle.setDefaultEncoding("UTF-8");
            messageSource = messageBundle;
        }
    }

    private void assertUserRepresentation(String responseBody, User expectedUser) {
        DocumentContext parsedResponse = JsonPath.parse(responseBody);
        assertThat(parsedResponse.read("$.content.id"), is(expectedUser.getId()));
        assertThat(parsedResponse.read("$.content.firstName"), is(expectedUser.getFirstName()));
        assertThat(parsedResponse.read("$.content.secondName"), is(expectedUser.getSecondName()));
        assertThat(parsedResponse.read("$.content.lastName"), is(expectedUser.getLastName()));
        assertThat(parsedResponse.read("$.content.age"), is(expectedUser.getAge()));
        assertThat(parsedResponse.read("$.content.vip"), is(expectedUser.isVip()));
        assertThat(parsedResponse.read("$.content.login.mail"), is(expectedUser.getLogin().getMail()));
        assertThat(parsedResponse.read("$.content.login.password"), is(expectedUser.getLogin().getPassword()));
        assertThat(parsedResponse.read("$.content.bio"), is(expectedUser.getBio()));
        assertThat(parsedResponse.read("$.links[0].href"), containsString("/user/" + expectedUser.getId()));
    }

    @Before
    public void init() {
        initMessageSource();
        userService = mock(UserService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setHandlerExceptionResolvers(createExceptionResolver())
                .build();
    }

    @Test
    public void shouldReturnEmptyListIfNoUsersOnGetAll() throws Exception {
        when(userService.findAll()).thenReturn(Stream.of());

        mvc.perform(get(URL_USER).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService, times(1)).findAll();
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnListOfUsersOnGetAll() throws Exception {
        final Supplier<Stream<User>> sup = () -> Stream.of(User.builder().id("someId").lastName("Mustermann").build());
        when(userService.findAll()).thenReturn(sup.get());

        MvcResult result = mvc.perform(get(URL_USER)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
        assertThat(parsedResponse.read("$.content"), is(notNullValue()));
        assertThat(parsedResponse.read("$.content[0].lastName"), is("Mustermann"));
        assertThat(parsedResponse.read("$.links[0].href"), containsString("/user"));
        assertThat(parsedResponse.read("$.links[1].href"), containsString("/user/someId"));
        assertThat(parsedResponse.read("$.total"), is(1));

        verify(userService, times(1)).findAll();
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnAllLinksIfGetUserFromMiddlePosition() throws Exception {
        when(userService.findOne(validUserId)).thenReturn(Optional.of(validMinimumUserWithId));
        when(userService.findAll()).thenReturn(Stream.of(validMinimumUserWithId.toBuilder().id("first").build(),
                validMinimumUserWithId.toBuilder().id("someUserId").build(),
                validMinimumUserWithId.toBuilder().id("last").build()));

        MvcResult result = mvc.perform(get(URL_USER + "/" + validUserId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
        assertThat(parsedResponse.read("$.links[0].href"), containsString("/user/someUserId"));
        assertThat(parsedResponse.read("$.links[1].href"), containsString("/user/first"));
        assertThat(parsedResponse.read("$.links[2].href"), containsString("/user/first"));
        assertThat(parsedResponse.read("$.links[3].href"), containsString("/user/last"));

        verify(userService, times(1)).findAll();
        verify(userService, times(1)).findOne(validUserId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnSelfAndStartIfOnlyOne() throws Exception {
        when(userService.findOne(validUserId)).thenReturn(Optional.of(validMinimumUserWithId));
        when(userService.findAll()).thenReturn(Stream.of(validMinimumUserWithId.toBuilder().id("someUserId").build()));

        MvcResult result = mvc.perform(get(URL_USER + "/" + validUserId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
        assertThat(parsedResponse.read("$.links[0].href"), containsString("/user/someUserId"));
        assertThat(parsedResponse.read("$.links[1].href"), containsString("/user/someUserId"));

        verify(userService, times(1)).findAll();
        verify(userService, times(1)).findOne(validUserId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnPrevIfLastUser() throws Exception {
        when(userService.findOne(validUserId)).thenReturn(Optional.of(validMinimumUserWithId));
        when(userService.findAll()).thenReturn(Stream.of(validMinimumUserWithId.toBuilder().id("first").build(),
                validMinimumUserWithId.toBuilder().id("someUserId").build()));

        MvcResult result = mvc.perform(get(URL_USER + "/" + validUserId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
        assertThat(parsedResponse.read("$.links[0].href"), containsString("/user/someUserId"));
        assertThat(parsedResponse.read("$.links[1].href"), containsString("/user/first"));
        assertThat(parsedResponse.read("$.links[2].href"), containsString("/user/first"));

        verify(userService, times(1)).findAll();
        verify(userService, times(1)).findOne(validUserId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnNextIfFirstUser() throws Exception {
        when(userService.findOne(validUserId)).thenReturn(Optional.of(validMinimumUserWithId));
        when(userService.findAll()).thenReturn(Stream.of(validMinimumUserWithId.toBuilder().id("someUserId").build(),
                validMinimumUserWithId.toBuilder().id("last").build()));

        MvcResult result = mvc.perform(get(URL_USER + "/" + validUserId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
        assertThat(parsedResponse.read("$.links[0].href"), containsString("/user/someUserId"));
        assertThat(parsedResponse.read("$.links[1].href"), containsString("/user/someUserId"));
        assertThat(parsedResponse.read("$.links[2].href"), containsString("/user/last"));

        verify(userService, times(1)).findAll();
        verify(userService, times(1)).findOne(validUserId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnAUserAndETagHeaderIfDifferentETagGetOne() throws Exception {
        when(userService.findOne(validUserId)).thenReturn(Optional.of(validMinimumUserWithId));
        final User user = validMinimumUserWithId.toBuilder().id("someUserId").build();
        when(userService.findAll()).thenReturn(Stream.of(user));

        MvcResult result = mvc.perform(get(URL_USER + "/" + validUserId)
                .accept(MediaType.APPLICATION_JSON)
                .header(IF_NONE_MATCH, "someDifferentETag"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertUserRepresentation(result.getResponse().getContentAsString(), validMinimumUserWithId);
        final String eTagHeader = result.getResponse().getHeader(ETAG);
        assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(user.getETag()));

        verify(userService, times(1)).findOne(validUserId);
        verify(userService, times(1)).findAll();
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnNoUserIfETagMatches() throws Exception {
        when(userService.findOne(validUserId)).thenReturn(Optional.of(validMinimumUserWithId));
        final User user = validMinimumUserWithId.toBuilder().id("someUserId").build();
        when(userService.findAll()).thenReturn(Stream.of(user));

        mvc.perform(get(URL_USER + "/" + validUserId)
                .accept(MediaType.APPLICATION_JSON)
                .header(IF_NONE_MATCH, user.getETag()))
                .andDo(print())
                .andExpect(status().isNotModified())
                .andExpect(header().string(ETAG, user.getETag()))
                .andExpect(content().string(""));

        verify(userService, times(1)).findOne(validUserId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturn404IfUserNotFoundOnGetOne() throws Exception {
        when(userService.findOne(validUserId)).thenReturn(Optional.empty());

        mvc.perform(get(URL_USER + "/" + validUserId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findOne(validUserId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldCreateUserAndReturnItsLocationOnPost() throws Exception {
        final User userToPersist = validMinimumUser;
        final User persistedUser = userToPersist.toBuilder().id(validUserId).build();
        when(userService.create(userToPersist)).thenReturn(persistedUser);
        when(userService.findAll()).thenReturn(Stream.of(persistedUser));

        MvcResult result = mvc.perform(post(URL_USER)
                .contentType(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(userToPersist)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString(URL_USER + "/" + validUserId)))
                .andReturn();

        assertUserRepresentation(result.getResponse().getContentAsString(), persistedUser);

        verify(userService, times(1)).create(userToPersist);
        verify(userService, times(1)).findAll();
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldUpdateUserOnPut() throws Exception {
        final User updatedUser = validMinimumUserWithId.toBuilder().build();
        when(userService.update(updatedUser)).thenReturn(updatedUser);
        when(userService.findAll()).thenReturn(Stream.of(updatedUser));

        MvcResult result = mvc.perform(put(URL_USER + "/" + validUserId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(updatedUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertUserRepresentation(result.getResponse().getContentAsString(), updatedUser);

        verify(userService, times(1)).update(updatedUser);
        verify(userService, times(1)).findAll();
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnNotFoundIfIDsDifferOnPut() throws Exception {
        final User updatedUser = validMinimumUserWithId.toBuilder().build();

        Long differentId = 9999L;
        mvc.perform(put(URL_USER + "/" + differentId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(updatedUser)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturNotFoundIfIdNotFoundOnPut() throws Exception {
        final User updatedUser = validMinimumUserWithId.toBuilder().build();
        when(userService.update(updatedUser)).thenThrow(new NotFoundException("id not found"));

        mvc.perform(put(URL_USER + "/" + validUserId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(updatedUser)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).update(updatedUser);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnBadRequestIfInvalidMailOnPut() throws Exception {
        final User userToPersist = validMinimumUserWithId.toBuilder().login(validLoginWithId.toBuilder().mail("max.mustermann@web.de").build()).build();
        String errorMsg = "only mails by otto allowed";
        String errorCause = "buasiness";
        UserValidationEntryRepresentation returnedError = UserValidationEntryRepresentation.builder().attribute(errorCause).errorMessage(errorMsg).build();
        when(userService.update(userToPersist)).thenThrow(new InvalidUserException(userToPersist, errorCause, errorMsg));

        final MvcResult result = mvc.perform(put(URL_USER + "/" + validUserId)
                .contentType(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(userToPersist)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userService, times(1)).update(userToPersist);
        UserValidationRepresentation returnedErrors = GSON.fromJson(result.getResponse().getContentAsString(), UserValidationRepresentation.class);
        assertThat(returnedErrors.getErrors().get(0), is(returnedError));
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnBadRequestIfIdIsAlreadySetOnPost() throws Exception {
        final User userToPersist = validMinimumUser.toBuilder().id(validUserId).build();

        mvc.perform(post(URL_USER)
                .contentType(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(userToPersist)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnBadRequestIfInvalidMailOnPost() throws Exception {
        final User userToPersist = validMinimumUser.toBuilder().login(validLogin.toBuilder().mail("max.mustermann@web.de").build()).build();
        String errorMsg = "only mails by otto allowed";
        String errorCause = "buasiness";
        UserValidationEntryRepresentation returnedError = UserValidationEntryRepresentation.builder().attribute(errorCause).errorMessage(errorMsg).build();
        when(userService.create(userToPersist)).thenThrow(new InvalidUserException(userToPersist, errorCause, errorMsg));

        final MvcResult result = mvc.perform(post(URL_USER)
                .contentType(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(userToPersist)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userService, times(1)).create(userToPersist);
        UserValidationRepresentation returnedErrors = GSON.fromJson(result.getResponse().getContentAsString(), UserValidationRepresentation.class);
        assertThat(returnedErrors.getErrors().get(0), is(returnedError));
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldDeleteUserOnDelete() throws Exception {
        mvc.perform(delete(URL_USER + "/" + validUserId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(validUserId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnNotFoundIfUserIdNotFoundOnDelete() throws Exception {
        doThrow(new NotFoundException("id not found")).when(userService).delete(validUserId);

        mvc.perform(delete(URL_USER + "/" + validUserId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).delete(validUserId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @UseDataProvider("invalidNewUserProvider")
    public void shouldReturnBadRequestForInvalidNewUserOnPost(User invalidUser, UserValidationRepresentation errors) throws Exception {
        final MvcResult result = mvc.perform(post(URL_USER)
                .contentType(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(invalidUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        UserValidationRepresentation returnedErrors = GSON.fromJson(result.getResponse().getContentAsString(), UserValidationRepresentation.class);
        assertThat(returnedErrors.getErrors().stream().filter(error -> !errors.getErrors().contains(error)).collect(toList()).size(), is(0));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @UseDataProvider("invalidExistingUserProvider")
    public void shouldReturnBadRequestForInvalidExistingUserOnPost(User invalidUser, UserValidationRepresentation errors) throws Exception {
        final MvcResult result = mvc.perform(put(URL_USER + "/" + validUserId)
                .contentType(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(invalidUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        UserValidationRepresentation returnedErrors = GSON.fromJson(result.getResponse().getContentAsString(), UserValidationRepresentation.class);
        assertThat(returnedErrors.getErrors().stream().filter(error -> !errors.getErrors().contains(error)).collect(toList()).size(), is(0));
        verifyNoMoreInteractions(userService);
    }

    private ExceptionHandlerExceptionResolver createExceptionResolver() {
        ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver() {
            protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
                Method method = new ExceptionHandlerMethodResolver(ControllerValidationHandler.class).resolveMethod(exception);
                return new ServletInvocableHandlerMethod(new ControllerValidationHandler(messageSource), method);
            }
        };
        exceptionResolver.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        exceptionResolver.afterPropertiesSet();
        return exceptionResolver;
    }
}