package com.github.kirviq.dostuff;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class CustomWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

	private static final SimpleGrantedAuthority USER = new SimpleGrantedAuthority("USER");
	private Map<String, String> credentials;

	@SneakyThrows
	@Value("${auth.credentials}")
	public void setCredentials(String json) {
		this.credentials = new ObjectMapper().readValue(json, new TypeReference<Map<String, String>>() {});
		credentials.entrySet().forEach(entry -> entry.setValue("{noop}" + entry.getValue()));
	}
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				String password = credentials.get(username);
				if (password == null) {
					return null;
				}
				return new User(username, password, Collections.singletonList(USER));
			}
		});
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.anyRequest().authenticated()
				.and()
				.httpBasic().realmName("Do Stuff");
	}
}
