package io.jenkins.plugins.entigo.pipeline.argocd.process;

import hudson.AbortException;
import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.rest.ResponseException;
import io.jenkins.plugins.entigo.pipeline.rest.RetryableException;
import io.jenkins.plugins.entigo.pipeline.util.ListenerUtil;

/**
 * Author: Märt Erlenheim
 * Date: 2020-12-01
 */
public abstract class RequestProcess<T> implements Process<T> {

    protected static final int INITIAL_RETRY_DELAY = 1;
    private static final int MAX_RETRY_DELAY = 30;

    private final TaskListener listener;
    private boolean running = true;
    private long retryDelay = INITIAL_RETRY_DELAY;

    protected RequestProcess(TaskListener listener) {
        this.listener = listener;
    }

    protected boolean isRunning() {
        return running;
    }

    protected void resetRetryDelay() {
        this.retryDelay = INITIAL_RETRY_DELAY;
    }

    protected long getRetryDelay() {
        return retryDelay;
    }

    protected abstract ProcessResult<T> run() throws AbortException;

    @Override
    public ProcessResult<T> start() throws AbortException {
        while (this.running) {
            checkInterruptions();
            ProcessResult<T> result = getResult();
            if (result.hasFinished()) {
                return result;
            }
            sleep(retryDelay * 1000L);
            if (retryDelay < MAX_RETRY_DELAY) {
                retryDelay = Math.min(MAX_RETRY_DELAY, retryDelay * 2);
            }
        }
        // This should only happen when process is stopped by timeout or thread interruption and this value won't be used
        return ProcessResult.unfinished();
    }

    private ProcessResult<T> getResult() throws AbortException {
        try {
            return run();
        } catch (RetryableException exception) {
            ListenerUtil.println(listener, String.format("Request failed, retrying in %d seconds," +
                    " exception message: %s", getRetryDelay(), exception.getMessage()));
            return ProcessResult.unfinished();
        } catch (ResponseException exception) {
            throw new AbortException("Request failed, process stopped, exception: " + exception.getMessage());

        }
    }

    @Override
    public void stop() {
        if (this.running) {
            this.running = false;
            ListenerUtil.println(listener, "Closing the connection, please wait a minute");
            close();
        }
    }

    protected synchronized void close() {
    }

    protected void checkInterruptions() throws AbortException {
        if (Thread.currentThread().isInterrupted()) {
            stop();
            throw new AbortException("Process was interrupted");
        }
    }

    private void sleep(Long delay) {
        try {
            // TODO Should we use a jenkins Timer or a Task Scheduler for waiting?
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            if (running) {
                stop();
            }
            Thread.currentThread().interrupt();
        }
    }
}
