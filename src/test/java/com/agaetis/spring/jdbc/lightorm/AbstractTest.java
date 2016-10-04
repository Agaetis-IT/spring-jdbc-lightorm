package com.agaetis.spring.jdbc.lightorm;

import javax.sql.DataSource;

import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.agaetis.spring.jdbc.lightorm.Scan;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public abstract class AbstractTest {

	@Configuration
	@ComponentScan(basePackageClasses = Scan.class)
	static class Config {

		@Bean
		public static PropertySourcesPlaceholderConfigurer properties() throws Exception {
			final PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
			pspc.setLocation(new ClassPathResource("/test.properties"));
			return pspc;
		}

		@Bean
		public DataSource dataSource() {
			return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).addScripts("classpath:schema.sql", "classpath:test-data.sql").build();
		}

		@Bean
		public JdbcTemplate jdbcTamplate() {
			return new JdbcTemplate(dataSource());
		}
	}

}
