package io.jenkins.plugins.entigo.pipeline.argocd.process;

/**
 * Author: Märt Erlenheim
 * Date: 2020-11-26
 */
public class ProcessResult<T> {

    private final T value;
    private final Exception exception;
    private final boolean finished;

    private ProcessResult(T result, Exception exception, boolean finished) {
        this.value = result;
        this.exception = exception;
        this.finished = finished;
    }

    public static <T> ProcessResult<T> success(T value) {
        return new ProcessResult<>(value, null, true);
    }

    public static <T> ProcessResult<T> failure(Exception exception) {
        return new ProcessResult<>(null, exception, true);
    }

    public static <T> ProcessResult<T> unfinished() {
        return new ProcessResult<>(null, null, false);
    }

    public T get() throws Exception {
        if (finished) {
            if (exception == null) {
                return value;
            } else {
                throw exception;
            }
        } else {
            throw new IllegalStateException("Process didn't finish");
        }
    }

    public Exception getException() {
        return exception;
    }

    public boolean isSuccessful() {
        return exception == null;
    }

    public boolean notSuccessful() {
        return !isSuccessful();
    }

    public boolean hasFinished() {
        return finished;
    }
}
