package com.yscz.upgrade.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class TaskQueue implements Runnable{

    private static Logger logger = LoggerFactory.getLogger(TaskQueue.class);

    //工作线程
    protected ArrayList<Thread> m_arrWorkThread;
    protected LinkedList<String> m_lstTasks;
    protected Lock m_Lock;
    protected volatile boolean m_bRunnable;

    public TaskQueue() {
        m_arrWorkThread = new ArrayList<Thread>();
        m_lstTasks = new LinkedList<String>();
        m_Lock = new ReentrantLock();
        m_bRunnable = false;
    }

    public void lock() {
        m_Lock.lock();
    }
    public void unlock() {
        m_Lock.unlock();
    }

    /**
     * 启动
     */
    public boolean start(int nThreadNum) {
        return start(nThreadNum, "TaskQueue");
    }

    /**
     * 启动线程
     * @param nThreadNum 线程数
     * @param preName 线程前缀名
     */
    public boolean start(int nThreadNum, String preName) {
        if(nThreadNum < 1)
            nThreadNum = 1;
        m_bRunnable = true;
        for(int i=0; i<nThreadNum; i++) {
            Thread workThread = new Thread(this, preName + i);
            workThread.start();
            m_arrWorkThread.add(workThread);
        }
        return true;
    }

    /**
     * 停止
     */
    public void stop() {
        m_bRunnable = false;
        try {
            int size = m_arrWorkThread.size();
            for(int i=0; i< size; i++) {
                m_arrWorkThread.get(i).join();
            }
            m_arrWorkThread.clear();
            //清空队列
            lock();
            while(!m_lstTasks.isEmpty()) {
                Object task = m_lstTasks.removeFirst();
                unExecute(task);
            }
            unlock();
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 添加任务
     */
    public boolean AddTask(String task) {
        lock();
        logger.error("TaskQueue AddTask task is:" + task);
        boolean ret = m_lstTasks.add(task);
        logger.error("TaskQueue AddTask task size:" + m_lstTasks.size());
        unlock();
        return ret;
    }

    public abstract boolean svc(Object task);
    /**
     * 未执行
     */
    public abstract boolean unExecute(Object task);

    public abstract void threadUninit();
    public boolean IsRun() { return m_bRunnable; }

    /**
     * 获取任务总数
     */
    public int getTaskCount() {
        lock();
        int nTaskCount = m_lstTasks.size();
        unlock();
        return nTaskCount;
    }
    /**
     * 读取任务
     */
    protected Object getTask() {
        Object task = null;
        lock();
        logger.error("TaskQueue getTask task m_lstTasks is:" + m_lstTasks);
        if(!m_lstTasks.isEmpty())
            task = m_lstTasks.removeFirst();
        unlock();
        return task;
    }

    /**
     * 工作线程
     */
    public void run() {
        logger.info("TaskQueue run task m_bRunnable is:" + m_bRunnable);
        while(m_bRunnable) {
            try {
                Object task = getTask();
                logger.info("TaskQueue run task task is:" + task);
                if(task == null) {
                    Thread.sleep(100);
                } else {
                    if(!svc(task)) {
                        break;
                    }
                }
            } catch(Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        threadUninit();
    }
}
