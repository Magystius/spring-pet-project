package de.otto.prototype.controller;

import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.otto.prototype.controller.handlers.ControllerValidationHandler;
import de.otto.prototype.controller.representation.UserValidationEntryRepresentation;
import de.otto.prototype.controller.representation.UserValidationRepresentation;
import de.otto.prototype.exceptions.ConcurrentModificationException;
import de.otto.prototype.exceptions.InvalidUserException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.hash.Hashing.sha256;
import static de.otto.prototype.controller.UserController.URL_USER;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

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

    @Mock
    private UserService userService;

    private static Stream<Arguments> invalidNewUserProvider() {
        return Stream.of(
                Arguments.of(validMinimumUser.toBuilder().id(validUserId).build(), buildUVRep(of(buildUVERep("error.id.new")))),
                Arguments.of(validMinimumUser.toBuilder().firstName("a").build(), buildUVRep(of(buildUVERep("error.name.range")))),
                Arguments.of(validMinimumUser.toBuilder().firstName("").build(), buildUVRep(of(buildUVERep("error.name.empty"), buildUVERep("error.name.range")))),
                Arguments.of(validMinimumUser.toBuilder().secondName("a").build(), buildUVRep(of(buildUVERep("error.name.range")))),
                Arguments.of(validMinimumUser.toBuilder().lastName("a").build(), buildUVRep(of(buildUVERep("error.name.range")))),
                Arguments.of(validMinimumUser.toBuilder().lastName("").build(), buildUVRep(of(buildUVERep("error.name.empty"), buildUVERep("error.name.range")))),
                Arguments.of(validMinimumUser.toBuilder().age(15).build(), buildUVRep(of(buildUVERep("error.age.young")))),
                Arguments.of(validMinimumUser.toBuilder().age(200).build(), buildUVRep(of(buildUVERep("error.age.old")))),
                Arguments.of(validMinimumUser.toBuilder().login(validLogin.toBuilder().mail("keineMail").build()).build(), buildUVRep(of(buildUVERep("error.mail.invalid")))),
                Arguments.of(validMinimumUser.toBuilder().login(validLogin.toBuilder().password("").build()).build(), buildUVRep(of(buildUVERep("error.password.empty"), buildUVERep("error.password")))),
                Arguments.of(validMinimumUser.toBuilder().bio("<script>alert(\"malicious code\")</script>").build(), buildUVRep(of(buildUVERep("error.bio.invalid")))));
    }

    private static Stream<Arguments> invalidExistingUserProvider() {
        return Stream.of(
                Arguments.of(validMinimumUserWithId.toBuilder().id(null).build(), buildUVRep(of((buildUVERep("error.id.existing"))))),
                Arguments.of(validMinimumUserWithId.toBuilder().firstName("a").build(), buildUVRep(of(buildUVERep("error.name.range")))),
                Arguments.of(validMinimumUserWithId.toBuilder().firstName("").build(), buildUVRep(of(buildUVERep("error.name.range"), buildUVERep("error.name.empty")))),
                Arguments.of(validMinimumUserWithId.toBuilder().secondName("a").build(), buildUVRep(of(buildUVERep("error.name.range")))),
                Arguments.of(validMinimumUserWithId.toBuilder().lastName("a").build(), buildUVRep(of(buildUVERep("error.name.range")))),
                Arguments.of(validMinimumUserWithId.toBuilder().lastName("").build(), buildUVRep(of(buildUVERep("error.name.range"), buildUVERep("error.name.empty")))),
                Arguments.of(validMinimumUserWithId.toBuilder().age(15).build(), buildUVRep(of(buildUVERep("error.age.young")))),
                Arguments.of(validMinimumUserWithId.toBuilder().age(200).build(), buildUVRep(of(buildUVERep("error.age.old")))),
                Arguments.of(validMinimumUserWithId.toBuilder().login(validLoginWithId.toBuilder().mail("keineMail").build()).build(), buildUVRep(of(buildUVERep("error.mail.invalid")))),
                Arguments.of(validMinimumUserWithId.toBuilder().login(validLoginWithId.toBuilder().password("").build()).build(), buildUVRep(of(buildUVERep("error.password.empty"), buildUVERep("error.password")))),
                Arguments.of(validMinimumUserWithId.toBuilder().bio("<script>alert(\"malicious code\")</script>").build(), buildUVRep(of(buildUVERep("error.bio.invalid")))));
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

    @BeforeEach
    void init() {
        initMessageSource();
        initMocks(this);
        mvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setHandlerExceptionResolvers(createExceptionResolver())
                .build();
    }

    //TODO: can these two be nested?
    @ParameterizedTest
    @MethodSource("invalidNewUserProvider")
    @DisplayName("should return a bad request response for invalid new user")
    void shouldReturnBadRequestForInvalidNewUserOnPost(User invalidUser, UserValidationRepresentation errors) throws Exception {
        final MvcResult result = mvc.perform(post(URL_USER)
                .contentType(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(invalidUser)))
                .andExpect(status().isBadRequest())
                .andReturn();

        UserValidationRepresentation returnedErrors = GSON.fromJson(result.getResponse().getContentAsString(), UserValidationRepresentation.class);
        assertThat(returnedErrors.getErrors().stream().filter(error -> !errors.getErrors().contains(error)).collect(toList()).size(), is(0));
        then(userService).shouldHaveZeroInteractions();
    }

    @ParameterizedTest
    @MethodSource("invalidExistingUserProvider")
    @DisplayName("should return a bad request response for invalid existing user")
    void shouldReturnBadRequestForInvalidExistingUserOnPut(User invalidUser, UserValidationRepresentation errors) throws Exception {
        final MvcResult result = mvc.perform(put(URL_USER + "/" + validUserId)
                .contentType(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(invalidUser)))
                .andExpect(status().isBadRequest())
                .andReturn();

        UserValidationRepresentation returnedErrors = GSON.fromJson(result.getResponse().getContentAsString(), UserValidationRepresentation.class);
        assertThat(returnedErrors.getErrors().stream().filter(error -> !errors.getErrors().contains(error)).collect(toList()).size(), is(0));
        then(userService).shouldHaveZeroInteractions();
    }

    private void assertUserRepresentation(String responseBody, User expectedUser) {
        DocumentContext parsedResponse = JsonPath.parse(responseBody);
        assertAll("user representation",
                () -> assertThat(parsedResponse.read("$.content.id"), is(expectedUser.getId())),
                () -> assertThat(parsedResponse.read("$.content.firstName"), is(expectedUser.getFirstName())),
                () -> assertThat(parsedResponse.read("$.content.secondName"), is(expectedUser.getSecondName())),
                () -> assertThat(parsedResponse.read("$.content.lastName"), is(expectedUser.getLastName())),
                () -> assertThat(parsedResponse.read("$.content.age"), is(expectedUser.getAge())),
                () -> assertThat(parsedResponse.read("$.content.vip"), is(expectedUser.isVip())),
                () -> assertThat(parsedResponse.read("$.content.login.mail"), is(expectedUser.getLogin().getMail())),
                () -> assertThat(parsedResponse.read("$.content.login.password"), is(expectedUser.getLogin().getPassword())),
                () -> assertThat(parsedResponse.read("$.content.bio"), is(expectedUser.getBio())),
                () -> assertThat(parsedResponse.read("$.links[0].href"), containsString("/user/" + expectedUser.getId())));
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

    @Nested
    @DisplayName("when try to retrieve all users")
    class getAllUsers {
        @Test
        @DisplayName("should return a no content response if no users found")
        void shouldReturnNotContentNoUsersOnGetAll() throws Exception {
            given(userService.findAll()).willReturn(Stream.of());

            mvc.perform(get(URL_USER).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should return the list of all users")
        void shouldReturnListOfUsersAndETagHeaderOnGetAll() throws Exception {
            final Supplier<Stream<User>> sup = () -> Stream.of(validMinimumUserWithId);
            given(userService.findAll()).willReturn(sup.get());

            final String combinedETags = sup.get().map(User::getETag).reduce("", (eTag1, eTag2) -> eTag1 + "," + eTag2);
            final HashCode hashCode = sha256().newHasher().putString(combinedETags, UTF_8).hash();

            MvcResult result = mvc.perform(get(URL_USER)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            assertUserListRepresentation(hashCode, result);
        }

        @Test
        @DisplayName("should return the list of all users and their etag if etag differs")
        void shouldReturnListOfUsersAndETagHeaderIfDifferentEtagOnGetAll() throws Exception {
            final Supplier<Stream<User>> sup = () -> Stream.of(validMinimumUserWithId);
            given(userService.findAll()).willReturn(sup.get());

            final String combinedETags = sup.get().map(User::getETag).reduce("", (eTag1, eTag2) -> eTag1 + "," + eTag2);
            final HashCode hashCode = sha256().newHasher().putString(combinedETags, UTF_8).hash();

            MvcResult result = mvc.perform(get(URL_USER)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(IF_NONE_MATCH, "someDifferentETag"))
                    .andExpect(status().isOk())
                    .andReturn();

            assertUserListRepresentation(hashCode, result);
        }

        @Test
        @DisplayName("should return a not modified response if etags are equal")
        void shouldReturnNoUserListIfETagMatchesOnGetAll() throws Exception {
            final Supplier<Stream<User>> sup = () -> Stream.of(validMinimumUserWithId);
            given(userService.findAll()).willReturn(sup.get());

            final String combinedETags = sup.get().map(User::getETag).reduce("", (eTag1, eTag2) -> eTag1 + "," + eTag2);
            final String eTag = sha256().newHasher().putString(combinedETags, UTF_8).hash().toString();

            mvc.perform(get(URL_USER)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(IF_NONE_MATCH, eTag))
                    .andExpect(status().isNotModified())
                    .andExpect(header().string(ETAG, eTag))
                    .andExpect(content().string(""));
        }

        private void assertUserListRepresentation(HashCode hashCode, MvcResult result) throws UnsupportedEncodingException {
            DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
            assertAll("user list representation",
                    () -> assertThat(parsedResponse.read("$.content"), is(notNullValue())),
                    () -> assertThat(parsedResponse.read("$.content[0].lastName"), is("Mustermann")),
                    () -> assertThat(parsedResponse.read("$.links[0].href"), containsString("/user")),
                    () -> assertThat(parsedResponse.read("$.links[1].href"), containsString("/user/someUserId")),
                    () -> assertThat(parsedResponse.read("$.total"), is(1)));

            final String eTagHeader = result.getResponse().getHeader(ETAG);
            assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(hashCode.toString()));
        }
    }

    @Nested
    @DisplayName("when a user is retrieved via a given id")
    class getOne {
        @Test
        @DisplayName("should return a user with all possible rel-links")
        void shouldReturnAllLinksIfGetUserFromMiddlePosition() throws Exception {
            given(userService.findOne(validUserId)).willReturn(Optional.of(validMinimumUserWithId));
            given(userService.findAll()).willReturn(Stream.of(validMinimumUserWithId.toBuilder().id("first").build(),
                    validMinimumUserWithId.toBuilder().id("someUserId").build(),
                    validMinimumUserWithId.toBuilder().id("last").build()));

            MvcResult result = mvc.perform(get(URL_USER + "/" + validUserId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
            assertAll("user rel-links",
                    () -> assertThat(parsedResponse.read("$.links[0].href"), containsString("/user/someUserId")),
                    () -> assertThat(parsedResponse.read("$.links[1].href"), containsString("/user/first")),
                    () -> assertThat(parsedResponse.read("$.links[2].href"), containsString("/user/first")),
                    () -> assertThat(parsedResponse.read("$.links[3].href"), containsString("/user/last")));
        }

        @Test
        @DisplayName("should return a user with only self and start rel-links")
        void shouldReturnSelfAndStartIfOnlyOne() throws Exception {
            given(userService.findOne(validUserId)).willReturn(Optional.of(validMinimumUserWithId));
            given(userService.findAll()).willReturn(Stream.of(validMinimumUserWithId.toBuilder().id("someUserId").build()));

            MvcResult result = mvc.perform(get(URL_USER + "/" + validUserId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
            assertAll("user rel-links",
                    () -> assertThat(parsedResponse.read("$.links[0].href"), containsString("/user/someUserId")),
                    () -> assertThat(parsedResponse.read("$.links[1].href"), containsString("/user/someUserId")));
        }

        @Test
        @DisplayName("should return a user with prev and start rel-links")
        void shouldReturnPrevIfLastUser() throws Exception {
            given(userService.findOne(validUserId)).willReturn(Optional.of(validMinimumUserWithId));
            given(userService.findAll()).willReturn(Stream.of(validMinimumUserWithId.toBuilder().id("first").build(),
                    validMinimumUserWithId.toBuilder().id("someUserId").build()));

            MvcResult result = mvc.perform(get(URL_USER + "/" + validUserId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
            assertAll("user rel-links",
                    () -> assertThat(parsedResponse.read("$.links[0].href"), containsString("/user/someUserId")),
                    () -> assertThat(parsedResponse.read("$.links[1].href"), containsString("/user/first")),
                    () -> assertThat(parsedResponse.read("$.links[2].href"), containsString("/user/first")));
        }

        @Test
        @DisplayName("should return a user with next rel-link")
        void shouldReturnNextIfFirstUser() throws Exception {
            given(userService.findOne(validUserId)).willReturn(Optional.of(validMinimumUserWithId));
            given(userService.findAll()).willReturn(Stream.of(validMinimumUserWithId.toBuilder().id("someUserId").build(),
                    validMinimumUserWithId.toBuilder().id("last").build()));

            MvcResult result = mvc.perform(get(URL_USER + "/" + validUserId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
            assertAll("user rel-links",
                    () -> assertThat(parsedResponse.read("$.links[0].href"), containsString("/user/someUserId")),
                    () -> assertThat(parsedResponse.read("$.links[1].href"), containsString("/user/someUserId")),
                    () -> assertThat(parsedResponse.read("$.links[2].href"), containsString("/user/last")));
        }

        @Test
        @DisplayName("should return a user if eTag is different")
        void shouldReturnAUserAndETagHeaderIfDifferentETagGetOne() throws Exception {
            given(userService.findOne(validUserId)).willReturn(Optional.of(validMinimumUserWithId));
            final User user = validMinimumUserWithId.toBuilder().id("someUserId").build();
            given(userService.findAll()).willReturn(Stream.of(user));

            MvcResult result = mvc.perform(get(URL_USER + "/" + validUserId)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(IF_NONE_MATCH, "someDifferentETag"))
                    .andExpect(status().isOk())
                    .andReturn();

            assertUserRepresentation(result.getResponse().getContentAsString(), validMinimumUserWithId);
            final String eTagHeader = result.getResponse().getHeader(ETAG);
            assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(user.getETag()));
        }

        @Test
        @DisplayName("should return a not modified response if eTag is equal")
        void shouldReturnNoUserIfETagMatches() throws Exception {
            given(userService.findOne(validUserId)).willReturn(Optional.of(validMinimumUserWithId));
            final User user = validMinimumUserWithId.toBuilder().id("someUserId").build();
            given(userService.findAll()).willReturn(Stream.of(user));

            mvc.perform(get(URL_USER + "/" + validUserId)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(IF_NONE_MATCH, user.getETag()))
                    .andExpect(status().isNotModified())
                    .andExpect(header().string(ETAG, user.getETag()))
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should return a not found response if given id is unknown")
        void shouldReturn404IfUserNotFoundOnGetOne() throws Exception {
            given(userService.findOne(validUserId)).willReturn(Optional.empty());

            mvc.perform(get(URL_USER + "/" + validUserId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("when a user for a given id is about to be updated")
    class updateUser {

        @Test
        @DisplayName("should update a user and return it with new etag")
        void shouldUpdateUserAndReturnHimAndHisETagOnPut() throws Exception {
            final User updatedUser = validMinimumUserWithId.toBuilder().build();
            given(userService.update(updatedUser, null)).willReturn(updatedUser);
            given(userService.findAll()).willReturn(Stream.of(updatedUser));

            MvcResult result = mvc.perform(put(URL_USER + "/" + validUserId)
                    .contentType(APPLICATION_JSON_VALUE)
                    .accept(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(updatedUser)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("eTag", is(updatedUser.getETag())))
                    .andReturn();

            assertUserRepresentation(result.getResponse().getContentAsString(), updatedUser);
        }

        @Test
        @DisplayName("should update a user if the given eTag matches and return it and a new etag")
        void shouldUpdateUserWithETagHeaderAndReturnHimAndHisETagOnPut() throws Exception {
            final String eTag = validMinimumUserWithId.getETag();
            given(userService.update(validMinimumUserWithId, eTag)).willReturn(validMinimumUserWithId);
            given(userService.findAll()).willReturn(Stream.of(validMinimumUserWithId));

            MvcResult result = mvc.perform(put(URL_USER + "/" + validUserId)
                    .contentType(APPLICATION_JSON_VALUE)
                    .accept(APPLICATION_JSON_VALUE)
                    .header(IF_MATCH, eTag)
                    .content(GSON.toJson(validMinimumUserWithId)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("eTag", is(eTag)))
                    .andReturn();

            assertUserRepresentation(result.getResponse().getContentAsString(), validMinimumUserWithId);
        }

        @Test
        @DisplayName("should return a precondition failed response if given eTag isn´t equal")
        void shouldReturnPreconditionFailedIfETagsArentEqual() throws Exception {
            willThrow(new ConcurrentModificationException("")).given(userService).update(validMinimumUserWithId, "differentEtag");

            mvc.perform(put(URL_USER + "/" + validUserId)
                    .contentType(APPLICATION_JSON_VALUE)
                    .accept(APPLICATION_JSON_VALUE)
                    .header(IF_MATCH, "differentEtag")
                    .content(GSON.toJson(validMinimumUserWithId)))
                    .andExpect(status().isPreconditionFailed())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should return a not found response if ids in url and body are different")
        void shouldReturnNotFoundIfIDsDifferOnPut() throws Exception {
            final User updatedUser = validMinimumUserWithId.toBuilder().build();

            Long differentId = 9999L;
            mvc.perform(put(URL_USER + "/" + differentId)
                    .contentType(APPLICATION_JSON_VALUE)
                    .accept(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(updatedUser)))
                    .andExpect(status().isNotFound());

            then(userService).shouldHaveZeroInteractions();
        }

        @Test
        @DisplayName("should return a not found response if given id is unknown")
        void shouldReturnNotFoundIfIdNotFoundOnPut() throws Exception {
            final User updatedUser = validMinimumUserWithId.toBuilder().build();
            willThrow(new NotFoundException("id not found")).given(userService).update(updatedUser, null);

            mvc.perform(put(URL_USER + "/" + validUserId)
                    .contentType(APPLICATION_JSON_VALUE)
                    .accept(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(updatedUser)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return a bad request response if business validation fails")
        void shouldReturnBadRequestIfBusinessValidationFailsOnPut() throws Exception {
            final User userToPersist = validMinimumUserWithId.toBuilder().login(validLoginWithId.toBuilder().mail("max.mustermann@web.de").build()).build();
            String errorMsg = "only mails by otto allowed";
            String errorCause = "buasiness";
            UserValidationEntryRepresentation returnedError = UserValidationEntryRepresentation.builder().attribute(errorCause).errorMessage(errorMsg).build();
            willThrow(new InvalidUserException(userToPersist, errorCause, errorMsg)).given(userService).update(userToPersist, null);

            final MvcResult result = mvc.perform(put(URL_USER + "/" + validUserId)
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(userToPersist)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            UserValidationRepresentation returnedErrors = GSON.fromJson(result.getResponse().getContentAsString(), UserValidationRepresentation.class);
            assertThat(returnedErrors.getErrors().get(0), is(returnedError));
        }
    }

    @Nested
    @DisplayName("when a new user to create is given")
    class createUser {
        @Test
        @DisplayName("should create the user and return it, a location and eTag header")
        void shouldCreateUserAndReturnItsLocationAndETagOnPost() throws Exception {
            final User userToPersist = validMinimumUser;
            final User persistedUser = userToPersist.toBuilder().id(validUserId).build();
            given(userService.create(userToPersist)).willReturn(persistedUser);
            given(userService.findAll()).willReturn(Stream.of(persistedUser));

            MvcResult result = mvc.perform(post(URL_USER)
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(userToPersist)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("location", containsString(URL_USER + "/" + validUserId)))
                    .andExpect(header().string("eTag", is(persistedUser.getETag())))
                    .andReturn();

            assertUserRepresentation(result.getResponse().getContentAsString(), persistedUser);
        }

        @Test
        @DisplayName("should return a bad request response, if id is already set")
        void shouldReturnBadRequestIfIdIsAlreadySetOnPost() throws Exception {
            final User userToPersist = validMinimumUser.toBuilder().id(validUserId).build();

            mvc.perform(post(URL_USER)
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(userToPersist)))
                    .andExpect(status().isBadRequest());

            then(userService).shouldHaveZeroInteractions();
        }

        @Test
        @DisplayName("should return bad request if business validation fails")
        void shouldReturnBadRequestIfBusinessValidationFailsOnPost() throws Exception {
            final User userToPersist = validMinimumUser.toBuilder().login(validLogin.toBuilder().mail("max.mustermann@web.de").build()).build();
            String errorMsg = "only mails by otto allowed";
            String errorCause = "business";
            UserValidationEntryRepresentation returnedError = UserValidationEntryRepresentation.builder().attribute(errorCause).errorMessage(errorMsg).build();
            willThrow(new InvalidUserException(userToPersist, errorCause, errorMsg)).given(userService).create(userToPersist);

            final MvcResult result = mvc.perform(post(URL_USER)
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(userToPersist)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            UserValidationRepresentation returnedErrors = GSON.fromJson(result.getResponse().getContentAsString(), UserValidationRepresentation.class);
            assertThat(returnedErrors.getErrors().get(0), is(returnedError));
        }
    }

    @Nested
    @DisplayName("when a user id is given to delete a user")
    class deleteUser {
        @Test
        @DisplayName("should delete the user")
        void shouldDeleteUserOnDelete() throws Exception {
            mvc.perform(delete(URL_USER + "/" + validUserId))
                    .andExpect(status().isNoContent());

            then(userService).should(times(1)).delete(validUserId);
        }

        @Test
        @DisplayName("should return a not found exception if id is unknown")
        void shouldReturnNotFoundIfUserIdNotFoundOnDelete() throws Exception {
            willThrow(new NotFoundException("id not found")).given(userService).delete(validUserId);

            mvc.perform(delete(URL_USER + "/" + validUserId))
                    .andExpect(status().isNotFound());
        }
    }
}