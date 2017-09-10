package de.otto.prototype.controller;

import de.otto.prototype.controller.representation.group.GroupListEntryRepresentation;
import de.otto.prototype.controller.representation.group.GroupListRepresentation;
import de.otto.prototype.controller.representation.group.GroupRepresentation;
import de.otto.prototype.model.Group;
import de.otto.prototype.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static de.otto.prototype.controller.GroupController.URL_GROUP;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(URL_GROUP)
@Validated
public class GroupController extends BaseController {

	public static final String URL_GROUP = "/group";

	private GroupService groupService;

	@Autowired
	public GroupController(final GroupService groupService) {
		this.groupService = groupService;
	}

	@RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<GroupListRepresentation> getAll(final @RequestHeader(value = IF_NONE_MATCH, required = false) String ETagHeader) {
		final List<Group> allGroups = groupService.findAll().collect(toList());

		if (allGroups.isEmpty())
			return noContent().build();

		final MultiValueMap<String, String> header = getETagHeader(allGroups);
		final String groupListETag = header.getFirst(ETAG);
		if (!isNullOrEmpty(ETagHeader) && groupListETag.equals(ETagHeader))
			return ResponseEntity.status(NOT_MODIFIED).header(ETAG, groupListETag).build();

		final GroupListRepresentation listOfGroups = GroupListRepresentation.builder()
				.groups(allGroups.stream().map(group -> GroupListEntryRepresentation.builder()
						.link(linkTo(GroupController.class).slash(group).withSelfRel())
						.group(group)
						.build()).collect(toList()))
				.link(linkTo(GroupController.class).withSelfRel())
				.link(linkTo(GroupController.class).slash(allGroups.get(0)).withRel("start"))
				.total(allGroups.size())
				.build();

		return new ResponseEntity<>(listOfGroups, header, OK);
	}

	@RequestMapping(value = "/{groupId}", method = GET, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<GroupRepresentation> getOne(final @Pattern(regexp = "^\\w{24}$", message = "error.id.invalid")
													  @PathVariable("groupId") String groupId,
													  final @RequestHeader(value = IF_NONE_MATCH, required = false) String ETagHeader) {
		final Optional<Group> foundGroup = groupService.findOne(groupId);

		if (!foundGroup.isPresent())
			return notFound().build();

		final Group group = foundGroup.get();
		final String groupETag = group.getETag();
		if (!isNullOrEmpty(ETagHeader) && groupETag.equals(ETagHeader))
			return ResponseEntity.status(NOT_MODIFIED).header(ETAG, groupETag).build();

		return new ResponseEntity<>(GroupRepresentation.builder()
				.group(group)
				.links(determineLinks(group, groupService.findAll().collect(toList()), GroupController.class))
				.build(), getETagHeader(group), OK);
	}

	@RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<GroupRepresentation> create(final @Validated(Group.New.class) @RequestBody Group group) {
		final Group persistedGroup = groupService.create(group);
		return created(linkTo(GroupController.class).slash(persistedGroup).toUri())
				.header(ETAG, persistedGroup.getETag())
				.body(GroupRepresentation.builder()
						.group(persistedGroup)
						.links(determineLinks(persistedGroup, groupService.findAll().collect(toList()), GroupController.class))
						.build());
	}

	@RequestMapping(value = "/{groupId}", method = PUT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<GroupRepresentation> update(final @Pattern(regexp = "^\\w{24}$", message = "error.id.invalid") @PathVariable("groupId") String groupId,
													  final @Validated(Group.Existing.class) @RequestBody Group group,
													  final @RequestHeader(value = IF_MATCH, required = false) String ETagHeader) {
		if (!groupId.equals(group.getId()))
			return notFound().build();
		final Group updatedGroup = groupService.update(group, ETagHeader);
		return new ResponseEntity<>(GroupRepresentation.builder()
				.group(updatedGroup)
				.links(determineLinks(updatedGroup, groupService.findAll().collect(toList()), GroupController.class))
				.build(), getETagHeader(updatedGroup), OK);
	}

	@RequestMapping(value = "/{groupId}", method = DELETE)
	public ResponseEntity delete(final @Pattern(regexp = "^\\w{24}$", message = "error.id.invalid") @PathVariable("groupId") String groupId) {
		groupService.delete(groupId);
		return noContent().build();
	}
}
