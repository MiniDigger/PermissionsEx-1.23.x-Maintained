package ru.tehkode.permissions.exceptions;

/**
 * This exception is thrown when a permissions backend has issues loading
 */
public class PermissionBackendException extends Exception {
    private static final long serialVersionUID = 1L;

    public PermissionBackendException() {
    }

    public PermissionBackendException(String message) {
        super(message);
    }

    public PermissionBackendException(String message, Throwable cause) {
        super(message, cause);
    }

    public PermissionBackendException(Throwable cause) {
        super(cause);
    }
}
