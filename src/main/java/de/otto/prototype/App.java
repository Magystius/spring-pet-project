package de.otto.prototype;

import de.otto.prototype.model.Login;
import de.otto.prototype.model.User;
import de.otto.prototype.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static java.util.Arrays.sort;
import static java.util.Arrays.stream;

@SpringBootApplication
public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(final String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CommandLineRunner init(final UserRepository userRepository) {
        return args -> {
            log.info("-------------------------------");
            log.info("Save some users");
            Login login = Login.builder().mail("max.mustermann@otto.de").password("somePassword").build();
            userRepository.save(User.builder().lastName("AWS").firstName("AWS1").age(30).login(login.toBuilder().build()).build());
            userRepository.save(User.builder().lastName("AWS2").firstName("AWS4").age(30).login(login.toBuilder().build()).build());
            userRepository.save(User.builder().lastName("Lavendel").firstName("Lara").age(30).login(login.toBuilder().build()).build());
            log.info("successfully saved some users");

            log.info("Users saved:");
            for (User user : userRepository.findAll()) {
                log.info(user.toString());
            }
            log.info("");
        };
    }

    @Bean
    public CommandLineRunner checkApp(final ApplicationContext ctx) {
        return args -> {
            log.info("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            sort(beanNames);
            stream(beanNames).forEach(log::info);

            String encoded = new BCryptPasswordEncoder().encode("admin");
            log.info(encoded);
            log.info(Boolean.toString(new BCryptPasswordEncoder().matches("admin", encoded)));
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