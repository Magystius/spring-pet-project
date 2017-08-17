package de.otto.prototype.integration;


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

}
