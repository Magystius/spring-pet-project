package de.otto.prototype.integration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.otto.prototype.model.User;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

	static final Gson GSON = new GsonBuilder().serializeNulls().create();

	static final Locale LOCALE = LocaleContextHolder.getLocale();

	@LocalServerPort
	protected int port;
	@Autowired
	protected TestRestTemplate template;
	URL base;

	MessageSource messageSource;

	ReloadableResourceBundleMessageSource initMessageSource() {
		ReloadableResourceBundleMessageSource messageBundle = new ReloadableResourceBundleMessageSource();
		messageBundle.setBasename("classpath:messages/messages");
		messageBundle.setDefaultEncoding("UTF-8");
		return messageBundle;
	}

	HttpHeaders prepareAuthAndMediaTypeAndIfMatchHeaders(String user, String password, String accept, String contentType, String etag) {
		return new HttpHeaders() {{
			String auth = user + ":" + password;
			byte[] encodedAuth = Base64.encodeBase64(
					auth.getBytes(Charset.forName("US-ASCII")));
			String authHeader = "Basic " + new String(encodedAuth);
			set(AUTHORIZATION, authHeader);
			if (!isNullOrEmpty(accept))
				set(ACCEPT, accept);
			if (!isNullOrEmpty(contentType))
				set(CONTENT_TYPE, contentType);
			if (!isNullOrEmpty(etag))
				set(IF_MATCH, etag);
		}};
	}

	HttpHeaders prepareAuthAndMediaTypeHeaders(String user, String password, String accept, String contentType) {
		return new HttpHeaders() {{
			String auth = user + ":" + password;
			byte[] encodedAuth = Base64.encodeBase64(
					auth.getBytes(Charset.forName("US-ASCII")));
			String authHeader = "Basic " + new String(encodedAuth);
			set(AUTHORIZATION, authHeader);
			if (!isNullOrEmpty(accept))
				set(ACCEPT, accept);
			if (!isNullOrEmpty(contentType))
				set(CONTENT_TYPE, contentType);
		}};
	}

	HttpHeaders prepareMediaTypeHeaders(String accept, String contentType) {
		return new HttpHeaders() {{
			if (!isNullOrEmpty(accept))
				set(ACCEPT, accept);
			if (!isNullOrEmpty(contentType))
				set(CONTENT_TYPE, contentType);
		}};
	}

	void assertUserRepresentation(String responseBody, User expectedUser, Boolean testLinks) {
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
		assertThat(parsedResponse.read("$._links.self.href"), containsString("/user/" + expectedUser.getId()));
		if (testLinks)
			assertThat(parsedResponse.read("$._links.start.href"), containsString("/user/" + expectedUser.getId()));
	}

}
