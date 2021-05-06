package com.yscz.upgrade.tools;

import com.yscz.upgrade.bean.RespBean;
import com.yscz.upgrade.config.ViewConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ShellCommandTools {

    private static Logger logger = LoggerFactory.getLogger(ShellCommandTools.class);

    public static RespBean respBean = null;

    /**
     * 执行shell命令
     * @param command
     */
    public static boolean runShellCommand(String command) {
        logger.info("ShellCommandTools runShell command:" + command);
        try {
            String[] args;
            String os = ViewConfig.OSName;
            if(os.toLowerCase().startsWith("win")){
                args = new String[]{"/bin/sh", "-c", command};
            }else {
                args = new String[]{"sh", "-c", command};
            }

            Process process = Runtime.getRuntime().exec(args);
            InputStream errorStream = process.getErrorStream();
            InputStream inputStream = process.getInputStream();
            readStreamInfo(errorStream, inputStream);
            int exitValue = process.waitFor();
            process.destroy();
            return 0 == exitValue;
        } catch (Exception e) {
            logger.error("ShellCommandTools runShell error, e:" + e.getMessage());
            respBean = RespBean.error("ShellCommandTools runShellFiles error, e" + e.getMessage());
            return false;
        }
    }

    /**
     * 执行脚本文件
     * @param set
     * @return
     */
    public static boolean runShellFiles(Set<String> set) {
        logger.info("ShellCommandTools runShell Files :" + set);
//        new Thread(() -> {
//            try {
//                for (String createFilePath : set) {
//                    File file = new File(createFilePath + "/create.sh");
//                    if(file.exists()) {
//                        runShellCommand("cd " + createFilePath + " && ./create.sh");
//                        // ShellFileTaskQueue taskQueue = new ShellFileTaskQueue();
//                        // taskQueue.AddTask("cd " + createFilePath + " && ./create.sh");
//                    }
//                }
//            }catch (Exception e) {
//                logger.error("ShellCommandTools runShellFiles error, e:" + e.getMessage());
//                respBean = RespBean.error("ShellCommandTools runShellFiles error, e" + e.getMessage());
//                // return false;
//            }
//        }).start();

        try {
            for (String createFilePath : set) {
                callable_runShellFile(createFilePath);
            }
        }catch (Exception e) {
            logger.error("ShellCommandTools runShellFiles error, e:" + e.getMessage());
            respBean = RespBean.error("ShellCommandTools runShellFiles error, e" + e.getMessage());
            // return false;
        }
        return true;
    }


    /**
     * 执行脚本文件
     * @param createFilePath
     */
    private static void callable_runShellFile(String createFilePath) {
        try {
            Callable<Boolean> callable = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    File file = new File(createFilePath + "/create.sh");
                    if(file.exists()) {
                        runShellCommand("cd " + createFilePath + " && ./create.sh");
                        // ShellFileTaskQueue taskQueue = new ShellFileTaskQueue();
                        // taskQueue.AddTask("cd " + createFilePath + " && ./create.sh");
                    }
                    return true;
                }
            };
            //使用FutureTask来包装Callable对象
            FutureTask<Boolean> task = new FutureTask<Boolean>(callable);
            new Thread(task).start();
            task.get();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 使用Runtime.exec运行子进程的输入流
     * @param inputStreams
     */
    private static void readStreamInfo(InputStream ... inputStreams) {
        for (InputStream in : inputStreams){
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line = null;
                while((line = br.readLine())!=null){
                    logger.info("ShellCommandTools runShell readStreamInfo inputStream:" + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    in.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

}
