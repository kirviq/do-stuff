package com.github.kirviq.dostuff;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BasicAuthInterceptor implements HandlerInterceptor {
	private Map<String, String> logins;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String header = request.getHeader("Authorization");
		if (header == null) {
			return deny(response, "credentials missing");
		}
		String[] auth = new String(Base64.getDecoder().decode(header.substring("Basic ".length())), StandardCharsets.ISO_8859_1).split(":", 2);
		if (!Objects.equals(logins.get(auth[0]), auth[1])) {
			return deny(response, "wrong credentials");
		}
		return true;
	}
	
	private boolean deny(HttpServletResponse response, String s) throws IOException {
		response.setHeader("WWW-Authenticate", "Basic realm=\"Do Stuff\"");
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, s);
		return false;
	}
	
	@SneakyThrows
	@Value("${auth.credentials}")
	public void setLogins(String json) {
		//noinspection unchecked
		this.logins = (Map<String, String>) new ObjectMapper().readValue(json, Map.class);
	}
}
