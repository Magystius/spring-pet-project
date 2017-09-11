package de.otto.prototype.service;

import de.otto.prototype.exceptions.ConcurrentModificationException;
import de.otto.prototype.exceptions.InvalidGroupException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Group;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;

@Service
public class GroupService {

	private final GroupRepository groupRepository;

	private final UserService userService;

	private final Validator validator;

	@Autowired
	public GroupService(final GroupRepository groupRepository, UserService userService, final Validator validator) {
		this.groupRepository = groupRepository;
		this.userService = userService;
		this.validator = validator;
	}

	public Stream<Group> findAll() {
		return groupRepository.streamAll();
	}

	public Optional<Group> findOne(final String groupId) {
		return groupRepository.findById(groupId);
	}

	public Group create(final Group group) {
		validateGroup(group, true);
		return groupRepository.save(group);
	}

	public Group update(final Group group, final String eTag) {
		final Group foundGroup = groupRepository.findById(group.getId())
				.orElseThrow(() -> new NotFoundException("group not found"));
		if (!isNullOrEmpty(eTag) && !foundGroup.getETag().equals(eTag))
			throw new ConcurrentModificationException("etags aren´t equal");
		validateGroup(group, false);
		return groupRepository.save(group);
	}

	public void delete(final String groupId) {
		if (!groupRepository.findById(groupId).isPresent())
			throw new NotFoundException("group not found");
		groupRepository.deleteById(groupId);
	}

	private void validateGroup(final Group groupToValidate, final Boolean newGroup) {
		Class groupType = newGroup ? Group.New.class : Group.Existing.class;
		Set<ConstraintViolation<Group>> errors = validator.validate(groupToValidate, groupType);
		if (!errors.isEmpty())
			throw new ConstraintViolationException(errors);

		//TODO: optimize doubled line?
		if (newGroup && findAll().anyMatch(group -> group.getName().equals(groupToValidate.getName())))
			throw new InvalidGroupException(groupToValidate, "business", "the group name is already taken");
		if (!newGroup && findAll().filter(group -> !group.getId().equals(groupToValidate.getId())).anyMatch(group -> group.getName().equals(groupToValidate.getName())))
			throw new InvalidGroupException(groupToValidate, "business", "the group name is already taken");

		final List<User> fetchedUsers = userService.findAll().collect(toList());
		final List<String> fetchedUserIds = fetchedUsers.stream().map(User::getId).collect(toList());
		if (!fetchedUserIds.containsAll(groupToValidate.getUserIds())) {
			throw new InvalidGroupException(groupToValidate, "business", "the group contains unknown users");
		}
		if (groupToValidate.isVip() && groupToValidate.getUserIds().stream().anyMatch(userIdToValidate -> !fetchedUsers.get(fetchedUserIds.indexOf(userIdToValidate)).isVip())) {
			throw new InvalidGroupException(groupToValidate, "business", "vip groups must only contains vip users");
		}
	}
}
