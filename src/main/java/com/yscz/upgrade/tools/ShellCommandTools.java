package com.yscz.upgrade.tools;

import com.yscz.upgrade.bean.RespBean;
import com.yscz.upgrade.config.ViewConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Set;

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
        try {
            for (String createFilePath : set) {
                File file = new File(createFilePath + "/create.sh");
                if(file.exists()) {
                    runShellCommand("cd " + createFilePath + " && ./create.sh");
                }
            }
        }catch (Exception e) {
            logger.error("ShellCommandTools runShellFiles error, e:" + e.getMessage());
            respBean = RespBean.error("ShellCommandTools runShellFiles error, e" + e.getMessage());
            return false;
        }
        return true;
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
