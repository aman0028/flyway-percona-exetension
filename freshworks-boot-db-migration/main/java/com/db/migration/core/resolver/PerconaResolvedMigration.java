package com.db.migration.core.resolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.util.BomFilter;
import org.springframework.core.io.Resource;

public class PerconaResolvedMigration
        implements ResolvedMigration {
    private final Resource resource;
    private final MigrationVersion version;
    private final String description;
    private final String script;
    private final MigrationType migrationType;
    private final MigrationExecutor executor;

    public PerconaResolvedMigration(Resource resource,
                                    MigrationVersion version,
                                    String description,
                                    String script,
                                    MigrationType migrationType,
                                    MigrationExecutor executor) {
        this.resource = resource;
        this.version = version;
        this.description = description;
        this.script = script;
        this.migrationType = migrationType;
        this.executor = executor;
    }

    @Override
    public MigrationVersion getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public Integer getChecksum() {
        final CRC32 crc32 = new CRC32();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream()),
                4096)) {
            String line = bufferedReader.readLine();

            if (line != null) {
                line = BomFilter.FilterBomFromString(line);
                do {
                    crc32.update(line.getBytes(StandardCharsets.UTF_8));
                } while ((line = bufferedReader.readLine()) != null);
            }
        } catch (IOException e) {
            throw new FlywayException(
                    "Unable to calculate checksum of " + resource.getFilename() + "\r\n" + e.getMessage(), e);
        }

        return (int) crc32.getValue();
    }

    @Override
    public MigrationType getType() {
        return migrationType;
    }

    @Override
    public String getPhysicalLocation() {
        return null;
    }

    @Override
    public MigrationExecutor getExecutor() {
        return executor;
    }

    @Override
    public boolean checksumMatches(Integer checksum) {
        return true;
    }

    @Override
    public boolean checksumMatchesWithoutBeingIdentical(Integer checksum) {
        return false;
    }
}
