package de.otto.prototype.integration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

	static final Gson GSON = new GsonBuilder().serializeNulls().create();

	static final Locale LOCALE = LocaleContextHolder.getLocale();

	@Value("${local.server.port}")
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
}
