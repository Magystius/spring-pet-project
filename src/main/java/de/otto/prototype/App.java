package de.otto.prototype;

import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import static java.util.Arrays.sort;
import static java.util.Arrays.stream;

@SpringBootApplication
public class App {

	private static final Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
    public CommandLineRunner init(ApplicationContext ctx, UserRepository userRepository) {
        return args -> {

			log.info("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			sort(beanNames);
			stream(beanNames).forEach(log::info);


			log.info("-------------------------------");
			log.info("Save some users");
			userRepository.save(User.builder().lastName("Mustermann").firstName("Max").age(30).mail("max.mustermann@otto.de").password("somePassword").build());
			userRepository.save(User.builder().lastName("Musterfrau").firstName("Sabine").age(30).mail("max.mustermann@otto.de").password("somePassword").build());
			userRepository.save(User.builder().lastName("Lavendel").firstName("Lara").age(30).mail("max.mustermann@otto.de").password("somePassword").build());
			log.info("successfully saved some users");

			log.info("Users saved:");
			for (User user : userRepository.findAll()) {
				log.info(user.toString());
			}
			log.info("");
		};
	}

}