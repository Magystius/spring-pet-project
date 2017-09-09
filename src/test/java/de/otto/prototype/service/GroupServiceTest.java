package de.otto.prototype.service;

import com.google.common.collect.ImmutableList;
import de.otto.prototype.exceptions.ConcurrentModificationException;
import de.otto.prototype.exceptions.InvalidGroupException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Group;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.GroupRepository;
import org.hibernate.validator.HibernateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.initMocks;

class GroupServiceTest {

	private static final String VALID_GROUP_ID = "someGroupId";
	private static final String VALID_USER_ID_NON_VIP = "someNonVipUserId";
	private static final String VALID_USER_ID_VIP = "someVipUserId";
	private static final Login VALID_LOGIN =
			Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
	private static final User VALID_MINIMUM_USER_VIP =
			User.builder().id(VALID_USER_ID_VIP).lastName("Mustermann").firstName("Max").age(30).vip(true).login(VALID_LOGIN).build();
	private static final User VALID_MINIMUM_USER_NON_VIP =
			User.builder().id(VALID_USER_ID_NON_VIP).lastName("Mustermann").firstName("Max").age(30).login(VALID_LOGIN).build();
	private static final Group VALID_MINIMUM_GROUP =
			Group.builder().name("someGroupName").userIds(ImmutableList.of(VALID_USER_ID_NON_VIP)).build();
	private static final Group VALID_MINIMUM_VIP_GROUP =
			Group.builder().name("someGroupName").vip(true).userIds(ImmutableList.of(VALID_USER_ID_VIP)).build();
	private static final Group VALID_MINIMUM_GROUP_WITH_ID =
			VALID_MINIMUM_GROUP.toBuilder().id(VALID_GROUP_ID).build();
	private static final Group VALID_MINIMUM_VIP_GROUP_WITH_ID =
			VALID_MINIMUM_VIP_GROUP.toBuilder().id(VALID_GROUP_ID).build();

	@Mock
	private GroupRepository groupRepository;

	@Mock
	private UserService userService;

	private GroupService testee;

	@BeforeEach
	void setUp() throws Exception {
		initMocks(this);

		LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
		validatorFactory.setProviderClass(HibernateValidator.class);
		validatorFactory.afterPropertiesSet();

		testee = new GroupService(groupRepository, userService, validatorFactory);

		given(userService.findAll()).willReturn(Stream.of(VALID_MINIMUM_USER_NON_VIP, VALID_MINIMUM_USER_VIP));
	}

	@Nested
	@DisplayName("when one or all groups are requested it")
	class getGroups {
		@Test
		@DisplayName("should return an empty list if no groups are found")
		void shouldReturnEmptyListIfNoGroupIsFound() throws Exception {
			given(groupRepository.streamAll()).willReturn(Stream.of());

			final Stream<Group> returnedList = testee.findAll();

			assertThat(returnedList.collect(toList()).size(), is(0));
		}

		@Test
		@DisplayName("should return a stream of all groups found")
		void shouldReturnListOfGroupsFound() throws Exception {
			final Group groupToReturn = Group.builder().name("someGroup").build();
			given(groupRepository.streamAll()).willReturn(Stream.of(groupToReturn));

			final List<Group> listOfReturnedGroups = testee.findAll().collect(toList());

			final Supplier<Stream<Group>> sup = listOfReturnedGroups::stream;
			assertAll("stream of groups",
					() -> assertThat(sup.get().collect(toList()).size(), is(1)),
					() -> assertThat(sup.get().collect(toList()).get(0), is(groupToReturn)));
		}

		@Test
		@DisplayName("should return an optional of found group for an id")
		void shouldReturnAGroupIfFound() throws Exception {
			String groupId = "someId";
			String groupName = "someName";
			final Group groupToReturn = Group.builder().id(groupId).name(groupName).build();
			given(groupRepository.findOne(groupId)).willReturn(groupToReturn);

			final Group foundGroup = testee.findOne(groupId).orElse(null);

			assert foundGroup != null;
			assertAll("group",
					() -> assertThat(foundGroup.getId(), is(groupId)),
					() -> assertThat(foundGroup.getName(), is(groupName)));
		}

		@Test
		@DisplayName("should an empty optional if no group found for id")
		void shouldReturnNoGroupIfNotFound() throws Exception {
			String groupId = "someId";
			given(groupRepository.findOne(groupId)).willReturn(null);

			final Optional<Group> foundGroup = testee.findOne(groupId);

			assertThat(foundGroup.isPresent(), is(false));
		}
	}

	@Nested
	@DisplayName("when a new group is given to be persisted")
	class createGroup {
		@Test
		@DisplayName("should persist and return the new standard group")
		void shouldReturnCreatedStandardGroup() throws Exception {
			given(groupRepository.save(VALID_MINIMUM_GROUP)).willReturn(VALID_MINIMUM_GROUP_WITH_ID);

			final Group returnedGroup = testee.create(VALID_MINIMUM_GROUP);

			assertThat(returnedGroup, is(VALID_MINIMUM_GROUP_WITH_ID));
		}

		@Test
		@DisplayName("should persist and return the new standard group with vip user")
		void shouldReturnCreatedStandardGroupWithVipUser() throws Exception {
			final Group standardGroupToCreateWithVipUser = VALID_MINIMUM_GROUP.toBuilder().userId(VALID_USER_ID_VIP).build();
			final Group expectedGroup = standardGroupToCreateWithVipUser.toBuilder().id(VALID_GROUP_ID).build();
			given(groupRepository.save(standardGroupToCreateWithVipUser)).willReturn(expectedGroup);

			final Group returnedGroup = testee.create(standardGroupToCreateWithVipUser);

			assertThat(returnedGroup, is(expectedGroup));
		}

		@Test
		@DisplayName("should persist and return the new vip group")
		void shouldReturnCreatedVipGroup() throws Exception {
			given(groupRepository.save(VALID_MINIMUM_VIP_GROUP)).willReturn(VALID_MINIMUM_VIP_GROUP_WITH_ID);

			final Group returnedGroup = testee.create(VALID_MINIMUM_VIP_GROUP);

			assertThat(returnedGroup, is(VALID_MINIMUM_VIP_GROUP_WITH_ID));
		}

		@Test
		@DisplayName("should throw a constraint violation if an invalid group is about to persisted")
		void shouldThrowConstraintViolationExceptionIfInvalidNewGroup() {
			final ConstraintViolationException exception =
					assertThrows(ConstraintViolationException.class, () -> testee.create(VALID_MINIMUM_GROUP.toBuilder().name("a").build()));
			final String msgCode = exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).findFirst().orElse("");
			assertThat(msgCode, is("error.name.range"));
			then(groupRepository).should(never()).save(any(Group.class));
		}

		@Test
		@DisplayName("should throw an invalid group exception if the group name is already taken")
		void shouldThrowInvalidGroupExceptionOnNewGroupIfNameIsAlreadyTaken() {
			final Group invalidGroupToCreate = Group.builder().name(VALID_MINIMUM_GROUP_WITH_ID.getName()).userIds(ImmutableList.of(VALID_USER_ID_NON_VIP)).build();
			given(groupRepository.streamAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID));
			final InvalidGroupException exception = assertThrows(InvalidGroupException.class, () -> testee.create(invalidGroupToCreate));
			assertAll("exception content",
					() -> assertThat(exception.getGroup(), is(invalidGroupToCreate)),
					() -> assertThat(exception.getErrorMsg(), is("the group name is already taken")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			then(groupRepository).should(never()).save(any(Group.class));
		}

		@Test
		@DisplayName("should throw an invalid group exception if the group to be persisted contains unknown userIds")
		void shouldThrowInvalidGroupExceptionOnNewGroupIfContainsUnknownUsers() {
			given(groupRepository.streamAll()).willReturn(Stream.empty());
			final Group invalidGroupToCreate = VALID_MINIMUM_GROUP.toBuilder().userId("unknownUserId").build();
			final InvalidGroupException exception = assertThrows(InvalidGroupException.class, () -> testee.create(invalidGroupToCreate));
			assertAll("exception content",
					() -> assertThat(exception.getGroup(), is(invalidGroupToCreate)),
					() -> assertThat(exception.getErrorMsg(), is("the group contains unknown users")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			then(groupRepository).should(never()).save(any(Group.class));
		}

		@Test
		@DisplayName("should throw an invalid group exception if the vip group to be persisted contains non-vip users")
		void shouldThrowInvalidGroupExceptionOnNewVipGroupIfContainsNonVipUsers() {
			given(groupRepository.streamAll()).willReturn(Stream.empty());
			final Group invalidGroupToCreate = VALID_MINIMUM_VIP_GROUP.toBuilder().userId(VALID_USER_ID_NON_VIP).build();
			final InvalidGroupException exception = assertThrows(InvalidGroupException.class, () -> testee.create(invalidGroupToCreate));
			assertAll("exception content",
					() -> assertThat(exception.getGroup(), is(invalidGroupToCreate)),
					() -> assertThat(exception.getErrorMsg(), is("vip groups must only contains vip users")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			then(groupRepository).should(never()).save(any(Group.class));
		}
	}

	@Nested
	@DisplayName("when a group is about be to updated")
	class updateGroup {
		@Test
		@DisplayName("should update the standard group and return it")
		void shouldReturnUpdatedStandardGroup() throws Exception {
			final Group updatedGroup = VALID_MINIMUM_GROUP_WITH_ID.toBuilder().name("newName").build();
			given(groupRepository.findOne(VALID_GROUP_ID)).willReturn(VALID_MINIMUM_GROUP_WITH_ID);
			given(groupRepository.save(updatedGroup)).willReturn(updatedGroup);
			given(groupRepository.streamAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID));

			final Group persistedGroup = testee.update(updatedGroup, null);

			assertThat(persistedGroup, is(updatedGroup));
		}

		@Test
		@DisplayName("should update the standard group with an vip user and return it")
		void shouldReturnUpdatedStandardGroupWithVipUser() throws Exception {
			final Group updatedGroup = VALID_MINIMUM_GROUP_WITH_ID.toBuilder().userId(VALID_USER_ID_VIP).build();
			given(groupRepository.findOne(VALID_GROUP_ID)).willReturn(VALID_MINIMUM_GROUP_WITH_ID);
			given(groupRepository.save(updatedGroup)).willReturn(updatedGroup);
			given(groupRepository.streamAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID));

			final Group persistedGroup = testee.update(updatedGroup, null);

			assertThat(persistedGroup, is(updatedGroup));
		}

		@Test
		@DisplayName("should update the vip group and return it")
		void shouldReturnUpdatedVipGroup() throws Exception {
			final Group updatedGroup = VALID_MINIMUM_VIP_GROUP_WITH_ID.toBuilder().name("newName").build();
			given(groupRepository.findOne(VALID_GROUP_ID)).willReturn(VALID_MINIMUM_VIP_GROUP_WITH_ID);
			given(groupRepository.save(updatedGroup)).willReturn(updatedGroup);
			given(groupRepository.streamAll()).willReturn(Stream.of(VALID_MINIMUM_VIP_GROUP_WITH_ID));

			final Group persistedGroup = testee.update(updatedGroup, null);

			assertThat(persistedGroup, is(updatedGroup));
		}

		@Test
		@DisplayName("should return a not found exception if no group for given id is found")
		void shouldReturnNotFoundExceptionIfIdUnknown() throws Exception {
			given(groupRepository.findOne(VALID_GROUP_ID)).willReturn(null);
			NotFoundException exception =
					assertThrows(NotFoundException.class, () -> testee.update(VALID_MINIMUM_GROUP_WITH_ID, null));
			assertThat(exception.getMessage(), is("group not found"));
			then(groupRepository).should(never()).save(any(Group.class));
		}

		@Test
		@DisplayName("should update group and return it, if the given etag and the group one are equal")
		void shouldReturnUpdatedUserIfETagsAreEqual() throws Exception {
			final Group updatedGroup = VALID_MINIMUM_GROUP_WITH_ID.toBuilder().name("newName").build();
			given(groupRepository.findOne(VALID_GROUP_ID)).willReturn(VALID_MINIMUM_GROUP_WITH_ID);
			given(groupRepository.streamAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID));
			given(groupRepository.save(updatedGroup)).willReturn(updatedGroup);

			final Group persistedGroup = testee.update(updatedGroup, VALID_MINIMUM_GROUP_WITH_ID.getETag());

			assertThat(persistedGroup, is(updatedGroup));
		}

		@Test
		@DisplayName("should throw an concurrent modification exception if etags aren´t equal")
		void shouldThrowConcurrentModificationExceptionIfETagsUnequal() {
			given(groupRepository.findOne(VALID_GROUP_ID)).willReturn(VALID_MINIMUM_GROUP_WITH_ID);
			ConcurrentModificationException exception =
					assertThrows(ConcurrentModificationException.class, () -> testee.update(VALID_MINIMUM_GROUP_WITH_ID, "someDifferentEtag"));
			assertThat(exception.getMessage(), is("etags aren´t equal"));
			then(groupRepository).should(never()).save(any(Group.class));
		}

		@Test
		@DisplayName("should throw a constraint violation if an invalid group update is given")
		void shouldThrowConstraintViolationExceptionIfInvalidExistingGroup() {
			given(groupRepository.findOne(VALID_GROUP_ID)).willReturn(VALID_MINIMUM_GROUP_WITH_ID);
			final ConstraintViolationException exception =
					assertThrows(ConstraintViolationException.class, () -> testee.update(VALID_MINIMUM_GROUP_WITH_ID.toBuilder().name("a").build(), null));
			final String msgCode = exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).findFirst().orElse("");
			assertThat(msgCode, is("error.name.range"));
			then(groupRepository).should(never()).save(any(Group.class));
		}

		@Test
		@DisplayName("should throw an invalid group exception if the group name is already taken")
		void shouldThrowInvalidGroupExceptionOnExistingGroupIfNameIsAlreadyTaken() {
			given(groupRepository.findOne(VALID_GROUP_ID)).willReturn(VALID_MINIMUM_GROUP_WITH_ID);
			given(groupRepository.streamAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID,
					VALID_MINIMUM_GROUP_WITH_ID.toBuilder().id("someOtherId").name("alreadyTakenName").build()));
			final Group invalidGroupToUpdate = VALID_MINIMUM_GROUP_WITH_ID.toBuilder().name("alreadyTakenName").build();
			final InvalidGroupException exception = assertThrows(InvalidGroupException.class, () -> testee.update(invalidGroupToUpdate, null));
			assertAll("exception content",
					() -> assertThat(exception.getGroup(), is(invalidGroupToUpdate)),
					() -> assertThat(exception.getErrorMsg(), is("the group name is already taken")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			then(groupRepository).should(never()).save(any(Group.class));
		}

		@Test
		@DisplayName("should throw an invalid group exception if the group update to be persisted contains unknown userIds")
		void shouldThrowInvalidGroupExceptionOnExistingGroupIfContainsUnknownUsers() {
			given(groupRepository.findOne(VALID_GROUP_ID)).willReturn(VALID_MINIMUM_GROUP_WITH_ID);
			given(groupRepository.streamAll()).willReturn(Stream.of(VALID_MINIMUM_GROUP_WITH_ID));
			final Group invalidGroupToUpdate = VALID_MINIMUM_GROUP_WITH_ID.toBuilder().userId("unknownUserId").build();
			final InvalidGroupException exception = assertThrows(InvalidGroupException.class, () -> testee.update(invalidGroupToUpdate, null));
			assertAll("exception content",
					() -> assertThat(exception.getGroup(), is(invalidGroupToUpdate)),
					() -> assertThat(exception.getErrorMsg(), is("the group contains unknown users")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			then(groupRepository).should(never()).save(any(Group.class));
		}

		@Test
		@DisplayName("should throw an invalid group exception if the vip group update to be persisted contains non-vip users")
		void shouldThrowInvalidGroupExceptionOnExistingVipGroupIfContainsNonVipUsers() {
			given(groupRepository.findOne(VALID_GROUP_ID)).willReturn(VALID_MINIMUM_VIP_GROUP_WITH_ID);
			given(groupRepository.streamAll()).willReturn(Stream.of(VALID_MINIMUM_VIP_GROUP_WITH_ID));
			final Group invalidGroupToUpdate = VALID_MINIMUM_VIP_GROUP_WITH_ID.toBuilder().userId(VALID_USER_ID_NON_VIP).build();
			final InvalidGroupException exception = assertThrows(InvalidGroupException.class, () -> testee.update(invalidGroupToUpdate, null));
			assertAll("exception content",
					() -> assertThat(exception.getGroup(), is(invalidGroupToUpdate)),
					() -> assertThat(exception.getErrorMsg(), is("vip groups must only contains vip users")),
					() -> assertThat(exception.getErrorCause(), is("business")));
			then(groupRepository).should(never()).save(any(Group.class));
		}
	}

	@Nested
	@DisplayName("when a group id is given to delete a group")
	class deleteGroup {
		@Test
		@DisplayName("should delete the group")
		void shouldDeleteGroup() throws Exception {
			final String groupId = "someId";
			given(groupRepository.findOne(groupId)).willReturn(Group.builder().build());

			testee.delete(groupId);
			then(groupRepository).should(inOrder(groupRepository)).findOne(groupId);
			then(groupRepository).should(inOrder(groupRepository)).delete(groupId);
		}

		@Test
		@DisplayName("should throw a not found exception if no group for given is found")
		void shouldThrowNotFoundExceptionForUnkownGroupId() throws Exception {
			final String groupId = "someId";
			given(groupRepository.findOne(groupId)).willReturn(null);
			NotFoundException exception = assertThrows(NotFoundException.class, () -> testee.delete(groupId));
			assertThat(exception.getMessage(), is("group id not found"));
			then(groupRepository).should(never()).delete(groupId);
		}
	}
}
