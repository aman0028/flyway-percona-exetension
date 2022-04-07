package com.db.migration.core.resolver;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.db.migration.core.exception.UnexpectedFlywayException;
import com.db.migration.core.executor.PerconaMigrationExecutor;
import com.db.migration.entity.MigrationDto;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class PerconaMigrationResolver
        implements MigrationResolver {
    private static final String JSON_EXTENSION_REGEX = "/**/*.json";
    private static final String FILENAME_REGEX = "([vV][0-9]{14}__\\w+\\.json)";
    private static final String DOT = ".";
    private static final String UNDERSCORE = "_";
    private final Pattern pattern = Pattern.compile(FILENAME_REGEX);

    @Value("${percona.toolkit.path}")
    private String perconaToolkitPath;

    @Value("${database.host}")
    private String databaseHost;

    @Value("${database.name}")
    private String databaseName;

    /**
     * This method fetch all json migrations from a given migration path and construct migration file wise Resolvers.
     * Expected migration file should match loose pattern i.e. {FILENAME_REGEX}.
     * It is failsafe and if migration file not matches, it won't entertain subsequent files.
     * Format: V{yyyyMMddHHmmss}__{file_description}.json
     * e.g. V20191011133630__add_c1_Column_table.json
     *
     * @param context
     * @return
     */
    @Override
    public Collection<ResolvedMigration> resolveMigrations(Context context) {
        Set<ResolvedMigration> resolvedMigrations = new HashSet<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for (Resource resource : getPerconaScripts(context.getConfiguration().getLocations()[0])) {
            try {
                String fileName = resource.getFilename();

                if (!StringUtils.hasText(fileName) || !isFileNamePatternMatched(fileName)) {
                    log.error("File {} naming convention NOT MATCHED. Required matching pattern "
                            + "V{yyyyMMddHHmmss}__{file_description}.json", fileName);
                    throw new IllegalArgumentException(
                            "File Pattern mismatched for " + fileName + ". Migration Aborted.");
                }
                log.info("Json Migration: {}", fileName);

                MigrationDto migration = objectMapper.readValue(resource.getFile(), MigrationDto.class);
                String tableName = migration.getTableName();
                String alterStatement = migration.getAlterStatement();
                Optional<String[]> perconaOptions = Optional.ofNullable(migration.getPerconaOptions());
                PerconaMigrationExecutor perconaMigrationExecutor =
                        new PerconaMigrationExecutor(databaseName, tableName, alterStatement, perconaOptions,
                                perconaToolkitPath, databaseHost);

                val version =
                        MigrationVersion.fromVersion(fileName.substring(1, fileName.indexOf(UNDERSCORE)));
                String description =
                        fileName.substring(fileName.indexOf(UNDERSCORE) + 2, fileName.indexOf(DOT));
                PerconaResolvedMigration perconaResolvedMigration =
                        new PerconaResolvedMigration(resource, version, description,
                                fileName, MigrationType.CUSTOM, perconaMigrationExecutor);

                resolvedMigrations.add(perconaResolvedMigration);
            } catch (IOException ex) {
                throw new UnexpectedFlywayException("Resource failed to read file", ex);
            }
        }
        return resolvedMigrations;
    }

    private boolean isFileNamePatternMatched(String fileName) {
        Matcher matcher = pattern.matcher(fileName);
        return matcher.matches();
    }

    public Resource[] getPerconaScripts(Location location) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        try {
            return resolver.getResources(location + JSON_EXTENSION_REGEX);
        } catch (IOException ex) {
            log.error("Couldn't list percona migration JSON files");
            throw new UnexpectedFlywayException("Invalid Migration Path", ex);
        }
    }
}
