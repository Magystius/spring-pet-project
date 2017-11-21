package de.otto.prototype.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
				.antMatchers("/").permitAll()
				.antMatchers("/user/**", "/group/**", "/resetpassword").hasAnyRole("ADMIN", "USER")
                .antMatchers("/internal/**").hasAnyRole("ADMIN", "MONITORING")
				.and()
				.httpBasic()
				.realmName("user")
				.and()
				.csrf().disable()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	@Autowired
	public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
		auth
				.inMemoryAuthentication()
				.passwordEncoder(new BCryptPasswordEncoder())
				.withUser("admin")
				.password("$2a$10$jheFSBOu4hQpsv5NhJttFO0hCAnFbo5tqcPZKI4UCKI3Y5B0jvahC")
				.roles("ADMIN")
				.and()
				.withUser("user")
				.password("$2a$10$ZaTZZGBGsCuUGn6OrzjQROc/huDCA38V7vh3Mx692SZVM7QZt2Q9C")
				.roles("USER")
				.and()
				.withUser("monitoring")
				.password("$2a$10$/TJ4M2AdIeX010E9yobdt.oKE9rNkSpR9esCubhsX8qOxb96lij02")
				.roles("MONITORING");
	}
}
