package de.otto.prototype.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.common.hash.HashCode;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.otto.prototype.controller.representation.ValidationEntryRepresentation;
import de.otto.prototype.controller.representation.ValidationRepresentation;
import de.otto.prototype.exceptions.ConcurrentModificationException;
import de.otto.prototype.exceptions.InvalidGroupException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Group;
import de.otto.prototype.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.hash.Hashing.sha256;
import static de.otto.prototype.controller.GroupController.URL_GROUP;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GroupControllerTest extends BaseControllerTest {

    private static final Type validationRepresentationType = new TypeToken<ValidationRepresentation<Group>>() {
    }.getType();

    private static final String VALID_GROUP_ID = "someGroupId";
    private static final String VALID_USER_ID = "someNonVipUserId";
    private static final Group VALID_MINIMUM_GROUP =
            Group.builder().name("someGroupName").userIds(ImmutableList.of(VALID_USER_ID)).build();
    private static final Group VALID_MINIMUM_GROUP_WITH_ID =
            VALID_MINIMUM_GROUP.toBuilder().id(VALID_GROUP_ID).build();

    @Mock
    private GroupService groupService;

    private static Stream<Arguments> invalidNewGroupProvider() {
        return Streams.concat(Stream.of(
                Arguments.of(VALID_MINIMUM_GROUP.toBuilder().id(VALID_GROUP_ID).build(), buildUVRep(of(buildUVERep("error.id.new", "group"))))),
                commonInvalidGroupProvider(VALID_MINIMUM_GROUP));
    }

    private static Stream<Arguments> invalidExistingGroupProvider() {
        return Streams.concat(Stream.of(
                Arguments.of(VALID_MINIMUM_GROUP_WITH_ID.toBuilder().id(null).build(), buildUVRep(of(buildUVERep("error.id.existing", "group"))))),
                commonInvalidGroupProvider(VALID_MINIMUM_GROUP_WITH_ID));
    }

    private static Stream<Arguments> commonInvalidGroupProvider(Group group) {
        return Stream.of(
                Arguments.of(group.toBuilder().name("a").build(), buildUVRep(of(buildUVERep("error.name.range", "group")))),
                Arguments.of(group.toBuilder().name("").build(), buildUVRep(of(buildUVERep("error.name.empty", "group"), buildUVERep("error.name.range", "group")))),
                Arguments.of(group.toBuilder().clearUserIds().build(), buildUVRep(of(buildUVERep("error.userlist.empty", "group")))));
    }

    private void assertGroupRepresentation(String responseBody) {
        DocumentContext parsedResponse = JsonPath.parse(responseBody);
        assertAll("group representation",
                () -> assertThat(GSON.fromJson(parsedResponse.read("$.content").toString(), Group.class), is(VALID_MINIMUM_GROUP_WITH_ID)),
                () -> assertThat(parsedResponse.read("$.links[0].href"), containsString("/group/" + VALID_MINIMUM_GROUP_WITH_ID.getId())));
    }

    @BeforeEach
    void setUp() {
        initMessageSource();
        initMocks(this);
        setupDefaultMockMvc(new GroupController(groupService));
    }

    @ParameterizedTest
    @MethodSource("invalidNewGroupProvider")
    @DisplayName("should return a bad request response for invalid new group")
    void shouldReturnBadRequestForInvalidNewGroupOnPost(Group invalidGroup, ValidationRepresentation<Group> errors) throws Exception {
        final MvcResult result = mvc.perform(post(URL_GROUP)
                .contentType(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(invalidGroup)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ValidationRepresentation<Group> returnedErrors = GSON.fromJson(result.getResponse().getContentAsString(), validationRepresentationType);
        assertThat(returnedErrors.getErrors().stream().filter(error -> !errors.getErrors().contains(error)).collect(toList()).size(), is(0));
        then(groupService).shouldHaveZeroInteractions();
    }

    @ParameterizedTest
    @MethodSource("invalidExistingGroupProvider")
    @DisplayName("should return a bad request response for invalid existing group")
    void shouldReturnBadRequestForInvalidExistingGroupOnPut(Group invalidGroup, ValidationRepresentation<Group> errors) throws Exception {
        final MvcResult result = mvc.perform(put(URL_GROUP + "/" + VALID_GROUP_ID)
                .contentType(APPLICATION_JSON_VALUE)
                .content(GSON.toJson(invalidGroup)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ValidationRepresentation<Group> returnedErrors = GSON.fromJson(result.getResponse().getContentAsString(), validationRepresentationType);
        assertThat(returnedErrors.getErrors().stream().filter(error -> !errors.getErrors().contains(error)).collect(toList()).size(), is(0));
        then(groupService).shouldHaveZeroInteractions();
    }

    @Nested
    @DisplayName("when try to retrieve all groups")
    class getAllGroups {

        @Test
        @DisplayName("should return a no content response if no groups found")
        void shouldReturnNotContentNoGroupsOnGetAll() throws Exception {
            given(groupService.findAll()).willReturn(Stream.of());

            mvc.perform(get(URL_GROUP).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should return the list of all groups")
        void shouldReturnListOfGroupsAndETagHeaderOnGetAll() throws Exception {
            final Supplier<Stream<Group>> sup = () -> Stream.of(VALID_MINIMUM_GROUP_WITH_ID);
            given(groupService.findAll()).willReturn(sup.get());

            final String combinedETags = sup.get().map(Group::getETag).reduce("", (eTag1, eTag2) -> eTag1 + "," + eTag2);
            final HashCode hashCode = sha256().newHasher().putString(combinedETags, UTF_8).hash();

            MvcResult result = mvc.perform(get(URL_GROUP)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            assertGroupListRepresentation(hashCode, result);
        }

        @Test
        @DisplayName("should return the list of all groups and their etag if etag differs")
        void shouldReturnListOfGroupsAndETagHeaderIfDifferentEtagOnGetAll() throws Exception {
            final Supplier<Stream<Group>> sup = () -> Stream.of(VALID_MINIMUM_GROUP_WITH_ID);
            given(groupService.findAll()).willReturn(sup.get());

            final String combinedETags = sup.get().map(Group::getETag).reduce("", (eTag1, eTag2) -> eTag1 + "," + eTag2);
            final HashCode hashCode = sha256().newHasher().putString(combinedETags, UTF_8).hash();

            MvcResult result = mvc.perform(get(URL_GROUP)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(IF_NONE_MATCH, "someDifferentETag"))
                    .andExpect(status().isOk())
                    .andReturn();

            assertGroupListRepresentation(hashCode, result);
        }

        @Test
        @DisplayName("should return a not modified response if etags are equal")
        void shouldReturnNoGroupListIfETagMatchesOnGetAll() throws Exception {
            final Supplier<Stream<Group>> sup = () -> Stream.of(VALID_MINIMUM_GROUP_WITH_ID);
            given(groupService.findAll()).willReturn(sup.get());

            final String combinedETags = sup.get().map(Group::getETag).reduce("", (eTag1, eTag2) -> eTag1 + "," + eTag2);
            final String eTag = sha256().newHasher().putString(combinedETags, UTF_8).hash().toString();

            mvc.perform(get(URL_GROUP)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(IF_NONE_MATCH, eTag))
                    .andExpect(status().isNotModified())
                    .andExpect(header().string(ETAG, eTag))
                    .andExpect(content().string(""));
        }

        private void assertGroupListRepresentation(HashCode hashCode, MvcResult result) throws UnsupportedEncodingException {
            DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
            assertAll("group list representation",
                    () -> assertThat(parsedResponse.read("$.content"), is(notNullValue())),
                    () -> assertThat(parsedResponse.read("$.content[0].links[0].href"), containsString(VALID_GROUP_ID)),
                    () -> assertThat(parsedResponse.read("$.content[0].content.id"), is(VALID_GROUP_ID)),
                    () -> assertThat(parsedResponse.read("$.content[0].content.name"), is(VALID_MINIMUM_GROUP_WITH_ID.getName())),
                    () -> assertThat(parsedResponse.read("$.content[0].content.userIds[0]"), is(VALID_USER_ID)),
                    () -> assertThat(parsedResponse.read("$.links[0].href"), containsString("/group")),
                    () -> assertThat(parsedResponse.read("$.links[1].href"), containsString("/group/someGroupId")),
                    () -> assertThat(parsedResponse.read("$.total"), is(1)));

            final String eTagHeader = result.getResponse().getHeader(ETAG);
            assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(hashCode.toString()));
        }
    }

    @Nested
    @DisplayName("when a group is retrieved via a given id")
    class getOne {

        @Test
        @DisplayName("should return a group with all possible rel-links")
        void shouldReturnAllLinksIfGetGroupFromMiddlePosition() throws Exception {
            given(groupService.findOne(VALID_GROUP_ID)).willReturn(Optional.of(VALID_MINIMUM_GROUP_WITH_ID));
            given(groupService.findAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID.toBuilder().id("first").build(),
                    VALID_MINIMUM_GROUP_WITH_ID,
                    VALID_MINIMUM_GROUP_WITH_ID.toBuilder().id("last").build()));

            MvcResult result = mvc.perform(get(URL_GROUP + "/" + VALID_GROUP_ID)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
            assertAll("group rel-links",
                    () -> assertThat(parsedResponse.read("$.links[0].href"), containsString("/group/someGroupId")),
                    () -> assertThat(parsedResponse.read("$.links[1].href"), containsString("/group/first")),
                    () -> assertThat(parsedResponse.read("$.links[2].href"), containsString("/group/first")),
                    () -> assertThat(parsedResponse.read("$.links[3].href"), containsString("/group/last")));
        }

        @Test
        @DisplayName("should return a group with only self and start rel-links")
        void shouldReturnSelfAndStartIfOnlyOne() throws Exception {
            given(groupService.findOne(VALID_GROUP_ID)).willReturn(Optional.of(VALID_MINIMUM_GROUP_WITH_ID));
            given(groupService.findAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID));

            MvcResult result = mvc.perform(get(URL_GROUP + "/" + VALID_GROUP_ID)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
            assertAll("group rel-links",
                    () -> assertThat(parsedResponse.read("$.links[0].href"), containsString("/group/someGroupId")),
                    () -> assertThat(parsedResponse.read("$.links[1].href"), containsString("/group/someGroupId")));
        }

        @Test
        @DisplayName("should return a group with prev and start rel-links")
        void shouldReturnPrevIfLastGroup() throws Exception {
            given(groupService.findOne(VALID_GROUP_ID)).willReturn(Optional.of(VALID_MINIMUM_GROUP_WITH_ID));
            given(groupService.findAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID.toBuilder().id("first").build(),
                    VALID_MINIMUM_GROUP_WITH_ID));

            MvcResult result = mvc.perform(get(URL_GROUP + "/" + VALID_GROUP_ID)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
            assertAll("group rel-links",
                    () -> assertThat(parsedResponse.read("$.links[0].href"), containsString("/group/someGroupId")),
                    () -> assertThat(parsedResponse.read("$.links[1].href"), containsString("/group/first")),
                    () -> assertThat(parsedResponse.read("$.links[2].href"), containsString("/group/first")));
        }

        @Test
        @DisplayName("should return a group with next rel-link")
        void shouldReturnNextIfFirstGroup() throws Exception {
            given(groupService.findOne(VALID_GROUP_ID)).willReturn(Optional.of(VALID_MINIMUM_GROUP_WITH_ID));
            given(groupService.findAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID,
                    VALID_MINIMUM_GROUP_WITH_ID.toBuilder().id("last").build()));

            MvcResult result = mvc.perform(get(URL_GROUP + "/" + VALID_GROUP_ID)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            DocumentContext parsedResponse = JsonPath.parse(result.getResponse().getContentAsString());
            assertAll("group rel-links",
                    () -> assertThat(parsedResponse.read("$.links[0].href"), containsString("/group/someGroupId")),
                    () -> assertThat(parsedResponse.read("$.links[1].href"), containsString("/group/someGroupId")),
                    () -> assertThat(parsedResponse.read("$.links[2].href"), containsString("/group/last")));
        }

        @Test
        @DisplayName("should return a group if eTag is different")
        void shouldReturnAGroupAndETagHeaderIfDifferentETagGetOne() throws Exception {
            given(groupService.findOne(VALID_GROUP_ID)).willReturn(Optional.of(VALID_MINIMUM_GROUP_WITH_ID));
            given(groupService.findAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID));

            MvcResult result = mvc.perform(get(URL_GROUP + "/" + VALID_GROUP_ID)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(IF_NONE_MATCH, "someDifferentETag"))
                    .andExpect(status().isOk())
                    .andReturn();

            assertGroupRepresentation(result.getResponse().getContentAsString());
            final String eTagHeader = result.getResponse().getHeader(ETAG);
            assertThat(eTagHeader.substring(1, eTagHeader.length() - 1), is(VALID_MINIMUM_GROUP_WITH_ID.getETag()));
        }

        @Test
        @DisplayName("should return a not modified response if eTag is equal")
        void shouldReturnNoGroupIfETagMatches() throws Exception {
            given(groupService.findOne(VALID_GROUP_ID)).willReturn(Optional.of(VALID_MINIMUM_GROUP_WITH_ID));

            final String eTag = VALID_MINIMUM_GROUP_WITH_ID.getETag();
            mvc.perform(get(URL_GROUP + "/" + VALID_GROUP_ID)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(IF_NONE_MATCH, eTag))
                    .andExpect(status().isNotModified())
                    .andExpect(header().string(ETAG, eTag))
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should return a not found response if given id is unknown")
        void shouldReturn404IfGroupNotFoundOnGetOne() throws Exception {
            given(groupService.findOne(VALID_GROUP_ID)).willReturn(Optional.empty());

            mvc.perform(get(URL_GROUP + "/" + VALID_GROUP_ID)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("when a new group to create is given")
    class createGroup {
        @Test
        @DisplayName("should create the group and return it, a location and eTag header")
        void shouldCreatGroupAndReturnItsLocationAndETagOnPost() throws Exception {
            given(groupService.create(VALID_MINIMUM_GROUP)).willReturn(VALID_MINIMUM_GROUP_WITH_ID);
            given(groupService.findAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID));

            MvcResult result = mvc.perform(post(URL_GROUP)
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(VALID_MINIMUM_GROUP)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("location", containsString(URL_GROUP + "/" + VALID_GROUP_ID)))
                    .andExpect(header().string("eTag", is(VALID_MINIMUM_GROUP_WITH_ID.getETag())))
                    .andReturn();

            assertGroupRepresentation(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("should return a bad request response, if id is already set")
        void shouldReturnBadRequestIfIdIsAlreadySetOnPost() throws Exception {
            mvc.perform(post(URL_GROUP)
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(VALID_MINIMUM_GROUP_WITH_ID)))
                    .andExpect(status().isBadRequest());

            then(groupService).shouldHaveZeroInteractions();
        }

        @Test
        @DisplayName("should return bad request if business validation fails")
        void shouldReturnBadRequestIfBusinessValidationFailsOnPost() throws Exception {
            String errorMsg = "Die Gruppe muss mind. einen Nutzer enthalten";
            String errorCause = "business";
            ValidationEntryRepresentation returnedError = ValidationEntryRepresentation.builder().attribute(errorCause).errorMessage(errorMsg).build();
            willThrow(new InvalidGroupException(VALID_MINIMUM_GROUP, errorCause, errorMsg)).given(groupService).create(VALID_MINIMUM_GROUP);

            final MvcResult result = mvc.perform(post(URL_GROUP)
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(VALID_MINIMUM_GROUP)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            ValidationRepresentation<Group> returnedErrors = GSON.fromJson(result.getResponse().getContentAsString(), validationRepresentationType);
            assertThat(returnedErrors.getErrors().get(0), is(returnedError));
        }
    }

    @Nested
    @DisplayName("when a group for a given id is about to be updated")
    class updateGroup {

        @Test
        @DisplayName("should update a group and return it with new etag")
        void shouldUpdateGroupAndReturnItAndItsETagOnPut() throws Exception {
            given(groupService.update(VALID_MINIMUM_GROUP_WITH_ID, null)).willReturn(VALID_MINIMUM_GROUP_WITH_ID);
            given(groupService.findAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID));

            MvcResult result = mvc.perform(put(URL_GROUP + "/" + VALID_GROUP_ID)
                    .contentType(APPLICATION_JSON_VALUE)
                    .accept(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(VALID_MINIMUM_GROUP_WITH_ID)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("eTag", is(VALID_MINIMUM_GROUP_WITH_ID.getETag())))
                    .andReturn();

            assertGroupRepresentation(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("should update a group if the given eTag matches and return it and a new etag")
        void shouldUpdateGroupWithETagHeaderAndReturnItAndItsETagOnPut() throws Exception {
            final String eTag = VALID_MINIMUM_GROUP_WITH_ID.getETag();
            given(groupService.update(VALID_MINIMUM_GROUP_WITH_ID, eTag)).willReturn(VALID_MINIMUM_GROUP_WITH_ID);
            given(groupService.findAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID));

            MvcResult result = mvc.perform(put(URL_GROUP + "/" + VALID_GROUP_ID)
                    .contentType(APPLICATION_JSON_VALUE)
                    .accept(APPLICATION_JSON_VALUE)
                    .header(IF_MATCH, eTag)
                    .content(GSON.toJson(VALID_MINIMUM_GROUP_WITH_ID)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("eTag", is(eTag)))
                    .andReturn();

            assertGroupRepresentation(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("should return a precondition failed response if given eTag isn´t equal")
        void shouldReturnPreconditionFailedIfETagsArentEqual() throws Exception {
            willThrow(new ConcurrentModificationException("")).given(groupService).update(VALID_MINIMUM_GROUP_WITH_ID, "differentETag");

            mvc.perform(put(URL_GROUP + "/" + VALID_GROUP_ID)
                    .contentType(APPLICATION_JSON_VALUE)
                    .accept(APPLICATION_JSON_VALUE)
                    .header(IF_MATCH, "differentETag")
                    .content(GSON.toJson(VALID_MINIMUM_GROUP_WITH_ID)))
                    .andExpect(status().isPreconditionFailed())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should return a not found response if ids in url and body are different")
        void shouldReturnNotFoundIfIDsDifferOnPut() throws Exception {
            Long differentId = 9999L;
            mvc.perform(put(URL_GROUP + "/" + differentId)
                    .contentType(APPLICATION_JSON_VALUE)
                    .accept(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(VALID_MINIMUM_GROUP_WITH_ID)))
                    .andExpect(status().isNotFound());

            then(groupService).shouldHaveZeroInteractions();
        }

        @Test
        @DisplayName("should return a not found response if given id is unknown")
        void shouldReturnNotFoundIfIdNotFoundOnPut() throws Exception {
            willThrow(new NotFoundException("id not found")).given(groupService).update(VALID_MINIMUM_GROUP_WITH_ID, null);

            mvc.perform(put(URL_GROUP + "/" + VALID_GROUP_ID)
                    .contentType(APPLICATION_JSON_VALUE)
                    .accept(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(VALID_MINIMUM_GROUP_WITH_ID)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return a bad request response if business validation fails")
        void shouldReturnBadRequestIfBusinessValidationFailsOnPut() throws Exception {
            String errorMsg = "Die Gruppe muss mind. einen Nutzer enthalten";
            String errorCause = "business";
            ValidationEntryRepresentation returnedError = ValidationEntryRepresentation.builder().attribute(errorCause).errorMessage(errorMsg).build();
            willThrow(new InvalidGroupException(VALID_MINIMUM_GROUP_WITH_ID, errorCause, errorMsg)).given(groupService).update(VALID_MINIMUM_GROUP_WITH_ID, null);

            final MvcResult result = mvc.perform(put(URL_GROUP + "/" + VALID_GROUP_ID)
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(GSON.toJson(VALID_MINIMUM_GROUP_WITH_ID)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            ValidationRepresentation<Group> returnedErrors = GSON.fromJson(result.getResponse().getContentAsString(), validationRepresentationType);
            assertThat(returnedErrors.getErrors().get(0), is(returnedError));
        }
    }

    @Nested
    @DisplayName("when a group id is given to delete a group")
    class deleteGroup {
        @Test
        @DisplayName("should delete the group")
        void shouldDeleteGroupOnDelete() throws Exception {
            mvc.perform(delete(URL_GROUP + "/" + VALID_GROUP_ID))
                    .andExpect(status().isNoContent());

            then(groupService).should(times(1)).delete(VALID_GROUP_ID);
        }

        @Test
        @DisplayName("should return a not found exception if id is unknown")
        void shouldReturnNotFoundIfGroupIdNotFoundOnDelete() throws Exception {
            willThrow(new NotFoundException("id not found")).given(groupService).delete(VALID_GROUP_ID);

            mvc.perform(delete(URL_GROUP + "/" + VALID_GROUP_ID))
                    .andExpect(status().isNotFound());
        }
    }
}