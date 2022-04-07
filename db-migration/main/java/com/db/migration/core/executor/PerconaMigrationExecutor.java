package com.db.migration.core.executor;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.db.migration.core.exception.UnexpectedFlywayException;
import com.db.migration.extension.PerconaCommandBuilder;
import com.db.migration.external.process.executor.ExternalProcessExecutor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;

@Slf4j
public class PerconaMigrationExecutor
        implements MigrationExecutor {
    private final PerconaCommandBuilder perconaCommandBuilder;
    private final ExternalProcessExecutor externalProcessExecutor;

    public PerconaMigrationExecutor(String databaseName, String tableName, String alterStatement,
                                    Optional<String[]> perconaOptions, String perconaToolkitPath, String dbHost) {
        this.perconaCommandBuilder = new PerconaCommandBuilder(databaseName, tableName, alterStatement,
                perconaOptions, perconaToolkitPath, dbHost);
        this.externalProcessExecutor = new ExternalProcessExecutor();
    }

    /**
     * This method is used to trigger generated Percona MySql command on terminal via ProcessBuilder
     *
     * @param context Flyway Context
     */
    @Override
    public void execute(Context context) {
        List<String> commands = getPerconaCommandBuilder().buildCommand(context.getConfiguration());
        try {
            getExternalProcessExecutor().executeCommand(log::info, commands.toArray(String[]::new));
        } catch (InterruptedException e) {
            log.error("Java thread exception occurred", e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("An error has occurred: ", e);
            throw new UnexpectedFlywayException("ProcessBuilder failed to start", e);
        }
    }

    @Override
    public boolean canExecuteInTransaction() {
        return true;
    }

    public PerconaCommandBuilder getPerconaCommandBuilder() {
        return this.perconaCommandBuilder;
    }

    public ExternalProcessExecutor getExternalProcessExecutor() {
        return this.externalProcessExecutor;
    }
}
