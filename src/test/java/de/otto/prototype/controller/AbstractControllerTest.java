package de.otto.prototype.controller;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.otto.prototype.model.User;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

class AbstractControllerTest {

    //TODO: hey, heres something the same??
    protected void assertUserRepresentation(String responseBody, User expectedUser) {
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
}
