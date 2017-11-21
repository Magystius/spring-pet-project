package de.otto.prototype.metrics;

import de.otto.prototype.model.User;
import de.otto.prototype.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.actuate.info.Info;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

class UserInfoContributorTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserInfoContributor testee;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    @DisplayName("should return infos from the users stream")
    void shouldReturnAnInfoBuilderWithUserInfos() {
        given(userService.findAll()).willReturn(Stream.of(
                User.builder().build(),
                User.builder().build(),
                User.builder().vip(true).build()));

        final Info.Builder builder = new Info.Builder();
        testee.contribute(builder);
        final Info info = builder.build();

        final Map<String, Integer> expectedUserInfo = Map.of(
                "total", 3,
                "vip", 1);

        assertThat(info.get("user", Map.class), is(expectedUserInfo));
    }

    @Test
    @DisplayName("should return no infos from a empty users stream")
    void shouldReturnAnInfoBuilderWithOutUserInfos() {
        given(userService.findAll()).willReturn(Stream.of());

        final Info.Builder builder = new Info.Builder();
        testee.contribute(builder);
        final Info info = builder.build();

        final Map<String, Integer> expectedUserInfo = Map.of(
                "total", 0,
                "vip", 0);

        assertThat(info.get("user", Map.class), is(expectedUserInfo));
    }
}