package de.otto.prototype.integration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application.test.properties")
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

    HttpHeaders prepareAuthAndMediaTypeAndIfMatchHeaders(String accept, String contentType, String etag) {
        return new HttpHeaders() {{
            String auth = "admin" + ":" + "admin";
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

    HttpHeaders prepareAuthAndMediaTypeHeaders(String accept, String contentType) {
        return new HttpHeaders() {{
            String auth = "admin" + ":" + "admin";
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

    ResponseEntity<String> performGetRequest(final String url) {
        return template.exchange(base.toString() + url,
                GET,
                new HttpEntity<>(prepareAuthAndMediaTypeHeaders(APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE)),
                String.class);
    }

    ResponseEntity<String> performPutRequest(final String url, final Object body, final String eTag) {
        return template.exchange(base.toString() + url,
                PUT,
                new HttpEntity<>(body, prepareAuthAndMediaTypeAndIfMatchHeaders(APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, eTag)),
                String.class);
    }
}
