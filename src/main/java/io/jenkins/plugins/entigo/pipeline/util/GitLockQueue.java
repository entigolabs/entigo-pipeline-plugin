package io.jenkins.plugins.entigo.pipeline.util;

import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.model.GitBranch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Using a lock instead of a synchronized block to try to preserve execution order with lock fairness
 * Author: Märt Erlenheim
 * Date: 2020-09-14
 */
public class GitLockQueue {

    private static Map<GitBranch, Lock> itemQueue = Collections.synchronizedMap(new HashMap<>());

    public static void executeInQueue(GitBranch key, TaskListener taskListener, Callable<Void> callable)
            throws Exception {
        Lock lock = itemQueue.computeIfAbsent(key, l -> new ReentrantLock(true));
        taskListener.getLogger().println("Getting lock for repo: " + key);
        lock.lock();
        try {
            taskListener.getLogger().println("Got the repo lock, executing given process");
            callable.call();
        } finally {
            lock.unlock();
            taskListener.getLogger().println("Released the lock for repo: " + key);
        }
    }
}
