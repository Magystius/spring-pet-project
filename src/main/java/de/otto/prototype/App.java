package de.otto.prototype;

import de.otto.prototype.model.Group;
import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.GroupRepository;
import de.otto.prototype.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@SpringBootApplication
public class App {

	private static final Logger log = LoggerFactory.getLogger(App.class);

	public static void main(final String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	public CommandLineRunner init(final UserRepository userRepository, final GroupRepository groupRepository) {
		return args -> {
			log.info("-------------------------------");
			log.info("Save some users");
			Login login = Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
			final User persistedUser1 = userRepository.save(User.builder().lastName("AWS").firstName("AWS1").age(30).login(login.toBuilder().build()).build());
			final User persistedUser2 = userRepository.save(User.builder().lastName("AWS2").firstName("AWS4").age(30).login(login.toBuilder().build()).build());
			userRepository.save(User.builder().lastName("Lavendel").firstName("Lara").age(30).login(login.toBuilder().build()).build());
			log.info("successfully saved some users");
			log.info("save a group");
			groupRepository.save(Group.builder().name("someDemoGroup").userId(persistedUser1.getId()).userId(persistedUser2.getId()).build());

			log.info("Users saved:");
			for (User user : userRepository.findAll()) {
				log.info(user.toString());
			}
			log.info("Group saved:");
			userRepository.findAll().forEach(group -> log.info(group.toString()));

			log.info("");
		};
	}

	@Bean(name = "messageSource")
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageBundle = new ReloadableResourceBundleMessageSource();
		messageBundle.setBasename("classpath:messages/messages");
		messageBundle.setDefaultEncoding("UTF-8");
		return messageBundle;
	}

}