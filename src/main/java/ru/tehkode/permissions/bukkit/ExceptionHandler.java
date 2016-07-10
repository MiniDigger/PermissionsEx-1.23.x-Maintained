package ru.tehkode.permissions.bukkit;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final ErrorReport outer;

    public ExceptionHandler(final ErrorReport outer) {
        this.outer = outer;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        ErrorReport.handleError("Unknown error in thread " + t.getName() + "-" + t.getId(), e);
    }
}

