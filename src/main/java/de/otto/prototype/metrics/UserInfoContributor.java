package de.otto.prototype.metrics;

import de.otto.prototype.model.User;
import de.otto.prototype.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.lang.Math.toIntExact;
import static java.util.stream.Collectors.toList;

@Component
public class UserInfoContributor implements InfoContributor {

    @Autowired
    private UserService userService;

    @Override
    public void contribute(Info.Builder builder) {
        final List<User> userList = userService.findAll().collect(toList());
        builder.withDetail("user",
                Map.of("total", userList.size(),
                        "vip", toIntExact(userList.stream().filter(User::isVip).count())));
    }
}
