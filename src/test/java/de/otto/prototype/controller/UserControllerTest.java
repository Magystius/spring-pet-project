package de.otto.prototype.controller;

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
import de.otto.prototype.model.UserList;
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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(DataProviderRunner.class)
public class UserControllerTest extends AbstractControllerTest {

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
				.andExpect(status().isOk())
				.andExpect(content().string(is(GSON.toJson(UserList.builder().build()))));

		verify(userService, times(1)).findAll();
		verifyNoMoreInteractions(userService);
	}

	@Test
	public void shouldReturnListOfUsersOnGetAll() throws Exception {
		final Supplier<Stream<User>> sup = () -> Stream.of(User.builder().lastName("Mustermann").build());
		final UserList listOfUsers = UserList.builder().users(sup.get().collect(toList())).build();
		when(userService.findAll()).thenReturn(sup.get());

		mvc.perform(get(URL_USER)
				.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string(is(GSON.toJson(listOfUsers))));

		verify(userService, times(1)).findAll();
		verifyNoMoreInteractions(userService);
	}

	@Test
	public void shouldReturnAUserIfFoundOnGetOne() throws Exception {
		when(userService.findOne(validUserId)).thenReturn(Optional.of(validMinimumUserWithId));

		MvcResult result = mvc.perform(get(URL_USER + "/" + validUserId)
				.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();

		assertUserRepresentation(result.getResponse().getContentAsString(), validMinimumUserWithId);

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
		when(userService.create(userToPersist)).thenReturn(userToPersist.toBuilder().id(validUserId).build());

		mvc.perform(post(URL_USER)
				.contentType(APPLICATION_JSON_VALUE)
				.content(GSON.toJson(userToPersist)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(header().string("location", containsString(URL_USER + "/" + validUserId)));

		verify(userService, times(1)).create(userToPersist);
		verifyNoMoreInteractions(userService);
	}

	@Test
	public void shouldUpdateUserOnPut() throws Exception {
		final User updatedUser = validMinimumUserWithId.toBuilder().build();
		when(userService.update(updatedUser)).thenReturn(updatedUser);

		mvc.perform(put(URL_USER + "/" + validUserId)
				.contentType(APPLICATION_JSON_VALUE)
				.accept(APPLICATION_JSON_VALUE)
				.content(GSON.toJson(updatedUser)))
				.andDo(print())
				.andExpect(status().isOk());

		verify(userService, times(1)).update(updatedUser);
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