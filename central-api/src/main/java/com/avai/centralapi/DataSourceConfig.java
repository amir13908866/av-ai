package com.avai.centralapi;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Bean
    DataSource dataSource(
            @Value("${DATABASE_URL:}") String databaseUrl,
            @Value("${DB_DRIVER:}") String driver,
            @Value("${DB_USER:}") String username,
            @Value("${DB_PASSWORD:}") String password) {

        String url = databaseUrl == null ? "" : databaseUrl.trim();
        if (url.isBlank()) {
            url = "jdbc:h2:mem:avaidb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
        } else if (!url.startsWith("jdbc:")) {
            url = "jdbc:" + url;
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        if (!username.isBlank()) ds.setUsername(username);
        if (!password.isBlank()) ds.setPassword(password);
        if (!driver.isBlank()) ds.setDriverClassName(driver);
        else if (url.startsWith("jdbc:postgresql:")) ds.setDriverClassName("org.postgresql.Driver");
        else ds.setDriverClassName("org.h2.Driver");
        return ds;
    }
}
