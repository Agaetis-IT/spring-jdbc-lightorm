package com.agaetis.spring.jdbc.lightorm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.agaetis.spring.jdbc.lightorm.sql.BasicSqlGenerator;
import com.agaetis.spring.jdbc.lightorm.sql.DBType;
import com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator;
import com.agaetis.spring.jdbc.lightorm.sql.SqlServer2012Generator;

@Configuration
public class SqlGeneratorConfig {

	private static final String LIGHTORM_DB_TYPE = "lightorm.db.type";

	@Autowired
	private Environment env;

	@Bean
	public SqlGenerator sqlGenerator() {
		DBType type = env.getProperty(LIGHTORM_DB_TYPE, DBType.class, DBType.GENERIC);
		switch (type) {
		case SQLSERVER_2012:
			return new SqlServer2012Generator();
		default:
			return new BasicSqlGenerator();
		}
	}
}
