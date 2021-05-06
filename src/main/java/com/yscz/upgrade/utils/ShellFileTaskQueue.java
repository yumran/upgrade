package com.yscz.upgrade.utils;

import com.yscz.upgrade.tools.ShellCommandTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellFileTaskQueue extends TaskQueue {

    private static Logger logger = LoggerFactory.getLogger(ShellFileTaskQueue.class);

    @Override
    public boolean svc(Object task) {
        String commandTask = (String) task;
        logger.error("ShellFileTaskQueue svc commandTask:" + commandTask);
        return ShellCommandTools.runShellCommand(commandTask);
    }

    @Override
    public boolean unExecute(Object task) {
        return true;
    }

    @Override
    public void threadUninit() {

    }
}
