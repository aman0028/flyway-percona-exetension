package com.db.migration.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DbMigrationApplication {
    public static void main(String[] args) {
        SpringApplication
                .run(DbMigrationApplication.class, args)
                .close();
    }
}
