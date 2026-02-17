package com.bhd.documentgateway.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Component
public class R2dbcInitializer {

    private final ConnectionFactory connectionFactory;

    public R2dbcInitializer(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @PostConstruct
    public void init() throws Exception {
        String schema = StreamUtils.copyToString(
                new ClassPathResource("schema.sql").getInputStream(),
                StandardCharsets.UTF_8);
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        for (String statement : schema.split(";")) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                client.sql(trimmed).fetch().rowsUpdated().then().block();
            }
        }
    }
}
