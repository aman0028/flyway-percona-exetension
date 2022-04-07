package com.db.migration.core.exception;

public class NonZeroExitCodeException
        extends RuntimeException {
    private final int exitCode;

    public NonZeroExitCodeException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
