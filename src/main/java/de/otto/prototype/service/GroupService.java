package de.otto.prototype.service;

import de.otto.prototype.exceptions.ConcurrentModificationException;
import de.otto.prototype.exceptions.InvalidGroupException;
import de.otto.prototype.exceptions.NotFoundException;
import de.otto.prototype.model.Group;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;

@Service
public class GroupService {

	private final GroupRepository groupRepository;

	private final UserService userService;

	@Autowired
	public GroupService(final GroupRepository groupRepository, UserService userService) {
		this.groupRepository = groupRepository;
		this.userService = userService;
	}

	public Stream<Group> findAll() {
		return groupRepository.findAll().toStream();
	}

	public Optional<Group> findOne(final String groupId) {
		return Optional.ofNullable(groupRepository.findById(Mono.just(groupId)).block());
	}

	public Group create(final Group groupToCreate) {
		validateGroup(groupToCreate, true);
		return Mono.just(groupToCreate).flatMap(groupRepository::save).block();
	}

	public Group update(final Group groupToUpdate, final String eTag) {
		final Group foundGroup =
				groupRepository.findById(Mono.just(groupToUpdate.getId())).switchIfEmpty(Mono.error(new NotFoundException("group not found"))).block();
		if (!isNullOrEmpty(eTag) && !foundGroup.getETag().equals(eTag))
			throw new ConcurrentModificationException("etags aren´t equal");
		validateGroup(groupToUpdate, false);
		return Mono.just(groupToUpdate).flatMap(groupRepository::save).block();
	}

	public void delete(final String groupId) {
		groupRepository.findById(Mono.just(groupId))
				.switchIfEmpty(Mono.error(new NotFoundException("group not found")))
				.flatMap(groupRepository::delete)
				.block();
	}

	private void validateGroup(final Group groupToValidate, final Boolean newGroup) {
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
