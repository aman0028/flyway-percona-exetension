package com.db.migration.extension;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.configuration.Configuration;

/**
 * This class is responsible for constructing Percona terminal command per json migration file for execution.
 */
@Slf4j
public class PerconaCommandBuilder {
    public static final String COMMAND = "pt-online-schema-change";

    private String perconaToolkitPath;
    private String databaseName;
    private String tableName;
    private String alterStatement;
    private Optional<String[]> perconaOptions;
    private String dbHost;

    public PerconaCommandBuilder(String databaseName, String tableName, String alterStatement,
                                 Optional<String[]> perconaOptions, String perconaToolkitPath, String dbHost) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.alterStatement = alterStatement;
        this.perconaOptions = perconaOptions;
        this.perconaToolkitPath = perconaToolkitPath;
        this.dbHost = dbHost;
    }

    public List<String> buildCommand(Configuration configuration) {
        List<String> commands = new ArrayList<>();
        commands.add(getFullToolkitPath());

        //Note:  '--config' must be the first on the command line, otherwise can't be used
        perconaOptions.ifPresent(option -> Collections.addAll(commands, option));
        commands.add("--alter=" + alterStatement);
        commands.add("--execute");

        StringBuilder dsn = new StringBuilder(200);
        dsn.append("p=").append(configuration.getPassword());
        dsn.append(",u=").append(configuration.getUser());
        dsn.append(",h=").append(dbHost);
        dsn.append(",D=").append(databaseName);
        dsn.append(",t=").append(tableName);

        commands.add(dsn.toString());
        log.info("ProcessBuilder Command :: {}", filterCommands(commands));
        return commands;
    }

    private String filterCommands(List<String> command) {
        StringBuilder sb = new StringBuilder();
        command.forEach(c -> sb.append(" ").append(c.replaceFirst("p=.*,u=", "p=*****,u=")));
        return sb.substring(1);
    }

    private String getFullToolkitPath() {
        log.info("PerconaToolkit Installation Path:: {}", perconaToolkitPath);
        if (perconaToolkitPath == null || perconaToolkitPath.isEmpty()) {
            return COMMAND;
        }
        if (perconaToolkitPath.endsWith(File.separator)) {
            return perconaToolkitPath + COMMAND;
        } else {
            return perconaToolkitPath + File.separator + COMMAND;
        }
    }
}
