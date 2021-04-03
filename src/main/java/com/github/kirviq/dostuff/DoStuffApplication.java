package com.github.kirviq.dostuff;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@SpringBootApplication(proxyBeanMethods = false)
public class DoStuffApplication implements WebMvcConfigurer {
	
	private final BasicAuthInterceptor basicAuthInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(basicAuthInterceptor);
	}
	public static void main(String[] args) {
		SpringApplication.run(DoStuffApplication.class, args);
	}

	@Setter(onMethod = @__(@Autowired))
	private DataSource dataSource;
	@PostConstruct
	public void initLiquibase() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
			database.setDatabaseChangeLogTableName("DATABASECHANGELOG");
			database.setDatabaseChangeLogLockTableName("DATABASECHANGELOGLOCK");
			try (Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.xml", new ClassLoaderResourceAccessor(), database)) {
				liquibase.update(new Contexts(), new LabelExpression());
			}
		}
	}
}
