package com.db.migration.core.exception;

/**
 * Marks an internal error (runtime exception) that prevents this software from further processing. Should
 * only be thrown in "impossible" cases where the software suspects a bug in itself.
 */
public class UnexpectedFlywayException
        extends RuntimeException {
    /**
     * Constructs a new {@link UnexpectedFlywayException} from a
     * {@link Throwable} event.
     * @param cause The {@link Throwable} event that should never have happened
     */
    public UnexpectedFlywayException(String msg, Throwable err) {
        super(msg, err);
    }
}
