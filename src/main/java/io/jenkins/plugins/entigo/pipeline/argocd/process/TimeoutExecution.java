package io.jenkins.plugins.entigo.pipeline.argocd.process;

import hudson.AbortException;
import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.util.ListenerUtil;
import jenkins.util.Timer;

import java.util.concurrent.*;

/**
 * Author: Märt Erlenheim
 * Date: 2020-10-29
 */
public class TimeoutExecution<T> {

    private final Process<T> process;
    private final TaskListener listener;
    private final long end;

    public TimeoutExecution(TaskListener listener, Process<T> process, long timeout) {
        this.process = process;
        this.listener = listener;
        end = System.currentTimeMillis() + (timeout * 1000);
    }

    public ProcessResult<T> run() throws TimeoutException {
        long delay = end - System.currentTimeMillis();
        if (delay > 0) {
            try {
                return Timer.get().submit(this::process).get(delay, TimeUnit.MILLISECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return ProcessResult.failure(new AbortException("Process thread was interrupted"));
            } catch (ExecutionException exception) {
                return ProcessResult.failure(exception);
            } catch (TimeoutException exception) {
                ListenerUtil.error(listener, "Process timed out, stopping the process");
                stop();
                throw exception;
            }
        } else {
            stop();
            return ProcessResult.failure(new AbortException("Timeout expired during a break"));
        }
    }

    public ProcessResult<T> process() {
        try {
            return process.start();
        } catch (Exception exception) {
            return ProcessResult.failure(exception);
        }
    }

    public synchronized void stop() {
        process.stop();
    }
}