package de.otto.prototype.integration;


import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.otto.prototype.model.User;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;
    @Autowired
    protected TestRestTemplate template;
    URL base;

    HttpHeaders prepareCompleteHeaders(String user, String password, String accept, String contentType) {
        return new HttpHeaders() {{
            String auth = user + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
            if (accept != null && !accept.isEmpty())
                set("Accept", accept);
            if (contentType != null && !contentType.isEmpty())
                set("Content-Type", contentType);
        }};
    }

    HttpHeaders prepareSimpleHeaders(String accept, String contentType) {
        return new HttpHeaders() {{
            if (accept != null && !accept.isEmpty())
                set("Accept", accept);
            if (contentType != null && !contentType.isEmpty())
                set("Content-Type", contentType);
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
