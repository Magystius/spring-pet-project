package de.otto.prototype.metrics;

import de.otto.prototype.model.Group;
import de.otto.prototype.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.lang.Math.toIntExact;
import static java.util.stream.Collectors.toList;

@Component
public class GroupInfoContributor implements InfoContributor {

    @Autowired
    private GroupService groupService;

    @Override
    public void contribute(Info.Builder builder) {
        final List<Group> userList = groupService.findAll().collect(toList());
        builder.withDetail("group",
                Map.of("total", userList.size(),
                        "vip", toIntExact(userList.stream().filter(Group::isVip).count())));
    }
}
