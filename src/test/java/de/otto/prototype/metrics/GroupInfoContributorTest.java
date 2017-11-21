package de.otto.prototype.metrics;

import de.otto.prototype.model.Group;
import de.otto.prototype.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.actuate.info.Info;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

class GroupInfoContributorTest {

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupInfoContributor testee;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    @DisplayName("should return infos from the groups stream")
    void shouldReturnAnInfoBuilderWithGroupInfos() {
        given(groupService.findAll()).willReturn(Stream.of(
                Group.builder().build(),
                Group.builder().build(),
                Group.builder().vip(true).build()));

        final Info.Builder builder = new Info.Builder();
        testee.contribute(builder);
        final Info info = builder.build();

        final Map<String, Integer> expectedGroupInfo = Map.of(
                "total", 3,
                "vip", 1);

        assertThat(info.get("group", Map.class), is(expectedGroupInfo));
    }

    @Test
    @DisplayName("should return no infos from a empty groups stream")
    void shouldReturnAnInfoBuilderWithOutGroupInfos() {
        given(groupService.findAll()).willReturn(Stream.of());

        final Info.Builder builder = new Info.Builder();
        testee.contribute(builder);
        final Info info = builder.build();

        final Map<String, Integer> expectedGroupInfo = Map.of(
                "total", 0,
                "vip", 0);

        assertThat(info.get("group", Map.class), is(expectedGroupInfo));
    }
}