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
public class TimeoutExecution {

    private final AbstractProcess process;
    private final TaskListener listener;
    private final long end;
    private boolean waitFailure = true;
    private transient ScheduledFuture<?> timeoutTask = null;
    private transient Future<?> processTask = null;

    public TimeoutExecution(AbstractProcess process, long timeout, TaskListener listener) {
        this.process = process;
        this.listener = listener;
        end = System.currentTimeMillis() + (timeout * 1000);
    }

    public void setWaitFailure(boolean waitFailure) {
        this.waitFailure = waitFailure;
    }

    public void start() {
        long delay = end - System.currentTimeMillis();
        if (delay > 0) {
            timeoutTask = Timer.get().schedule(() -> {
                ListenerUtil.error(listener, "Process timed out, stopping the process");
                stop();
                if (waitFailure) {
                    process.failure(new AbortException("Process timed out"));
                } else {
                    ListenerUtil.error(listener, "waitFailure was False, continuing build");
                    process.success(null);
                }
            }, delay, TimeUnit.MILLISECONDS);
            processTask = Timer.get().submit(() -> {
                try {
                    process.start();
                } catch (Exception exception) {
                    process.failure(exception);
                }
                timeoutTask.cancel(true); // Need to cancel or it causes an already delivered failure
            });
        } else {
            process.failure(new AbortException("Process timed out during a break"));
        }
    }

    public void stop() {
        if (timeoutTask != null) {
            timeoutTask.cancel(true);
        }
        if (processTask != null) {
            processTask.cancel(true);
        }
        process.stop();
    }

}