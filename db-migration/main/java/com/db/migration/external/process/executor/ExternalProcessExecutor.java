package com.db.migration.external.process.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import com.db.migration.core.exception.NonZeroExitCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalProcessExecutor {
    private static final Logger log = LoggerFactory.getLogger(ExternalProcessExecutor.class);

    /**
     * This method implements a "stream gobbler" class that consumes process output from the process's output stream
     * (error stream merged with output stream) and it waits for the external process to complete using
     * Process.waitFor(), then print the process return code
     * Params:
     * consumer - a consumer to perform action as per the given request
     * command – a string array containing the program and its arguments
     */
    public void executeCommand(Consumer<String> consumer, String... command)
                throws InterruptedException, IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = null;
        try {
            process = processBuilder.start();
            CustomStreamGobbler csg = new CustomStreamGobbler(process.getInputStream(), consumer);
            csg.start();
            int exitCode = process.waitFor();
            log.info("Command:{}, Exit code:{}", command[0], exitCode);
            if (exitCode != 0) {
                throw new NonZeroExitCodeException("Process exit with " + exitCode, exitCode);
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    static class CustomStreamGobbler
            extends Thread {
        private final InputStream inputStream;
        private final Consumer<String> consumer;

        public CustomStreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }
}
