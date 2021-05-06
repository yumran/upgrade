package com.yscz.upgrade.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellFileTaskUtils {

    private static Logger logger = LoggerFactory.getLogger(ShellFileTaskUtils.class);

    public ShellFileTaskQueue shellFileTaskQueue;

    protected static ShellFileTaskUtils shellFileTaskUtils = new ShellFileTaskUtils();

    public static ShellFileTaskUtils getInstance() {
        return shellFileTaskUtils;
    }

    public ShellFileTaskQueue getShellFileTaskQueue() {
        return shellFileTaskQueue;
    }

    public boolean init() {
        ShellFileTaskQueue taskQueue = new ShellFileTaskQueue();
        taskQueue.start(3);
        return true;
    }
}
