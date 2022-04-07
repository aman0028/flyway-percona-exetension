package com.db.migration.config;

import com.db.migration.core.resolver.PerconaMigrationResolver;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

/**
 * BootFlywayConfiguration is used to append Spring backed flyway to include Custom Resolvers
 */
@Slf4j
@Configuration
public class BootFlywayConfiguration {
    private final PerconaMigrationResolver perconaMigrationResolver;

    private final DataSourceProperties properties;

    private final ResourceLoader resourceLoader;

    @Autowired
    public BootFlywayConfiguration(ResourceLoader resourceLoader,
                                   PerconaMigrationResolver perconaMigrationResolver,
                                   DataSourceProperties properties) {
        this.resourceLoader = resourceLoader;
        this.perconaMigrationResolver = perconaMigrationResolver;
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public FlywayMigrationInitializer flywayInitializer(Flyway flyway,
                                                        ObjectProvider<FlywayMigrationStrategy> migrationStrategy) {
        ClassicConfiguration configuration = (ClassicConfiguration) flyway.getConfiguration();
        configuration.setResolvers(perconaMigrationResolver);
        flyway = new FluentConfiguration(resourceLoader.getClassLoader())
                .configuration(configuration)
                .resolvers(perconaMigrationResolver)
                .dataSource(properties.getUrl(), properties.getUsername(), properties.getPassword())
                .load();
        return new FlywayMigrationInitializer(flyway, migrationStrategy.getIfAvailable());
    }
}
