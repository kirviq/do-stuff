package com.github.kirviq.dostuff;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class CustomWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

	private static final SimpleGrantedAuthority USER = new SimpleGrantedAuthority("USER");
	private Map<String, String> credentials;

	@Value("${credentials}")
	public void setCredentials(String[] namePasswordPairs) {
		this.credentials = Arrays.stream(namePasswordPairs)
				.map(pair -> pair.split(":", 2))
				.collect(ImmutableMap.toImmutableMap(pair -> pair[0], pair -> pair[1]));
	}
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(new AuthenticationProvider() {
			@Override
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				if (!authentication.getCredentials().equals(credentials.get(authentication.getName().toLowerCase()))) {
					throw new BadCredentialsException("username and password don't match");
				}
				return new UsernamePasswordAuthenticationToken(authentication.getPrincipal().toString().toLowerCase(), "-", Collections.singletonList(USER));
			}

			@Override
			public boolean supports(Class<?> aClass) {
				return true;
			}
		});
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
//				.antMatchers("/securityNone").permitAll()
				.anyRequest().authenticated()
				.and()
				.httpBasic();

//		http.addFilterAfter(new CustomFilter(),
//				BasicAuthenticationFilter.class);
	}

//	@Bean
//	public PasswordEncoder passwordEncoder() {
//		return new BCryptPasswordEncoder();
//	}
}
