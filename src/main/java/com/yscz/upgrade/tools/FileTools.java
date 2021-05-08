package com.yscz.upgrade.tools;

import com.yscz.upgrade.bean.RespBean;
import com.yscz.upgrade.bean.XmlFileAttributeBean;
import com.yscz.upgrade.bean.XmlFolderAttributeBean;
import com.yscz.upgrade.config.ViewConfig;
import com.yscz.upgrade.utils.XMLParserImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileTools {

    private static Logger logger = LoggerFactory.getLogger(FileTools.class);

    public static RespBean respBean = null;

    public static Set<String> destDirList = new HashSet<>();  // 保存目标文件夹目录(绝对路径) 即 create.sh的路径


    /**
     * 检查文件是否存在
     * @param filePath
     * @return
     */
    public static boolean checkFileExist(String filePath) {
        logger.info("FileTools checkFileExist filePath:" + filePath);
        if(StringUtils.isEmpty(filePath)) {
            respBean = RespBean.error("FileTools checkFileExist filePath is empty !!");
            return false;
        }
        // 检查文件 /文件夹是否存在
        File file = new File(filePath);
        if(!file.exists()) {
            respBean = RespBean.error("FileTools checkFileExist filePath is not exist !!");
            return false;
        }
        // 文件存在
        respBean =  RespBean.ok("FileTools checkFileExist filePath is exist");
        return true;
    }


    /**
     * 根据XML文件中记录的版本号 / 时间; 判断是否需要升级
     * @param localXMLPath
     * @param upgradeXMLPath
     * @return
     */
    public static boolean ckeckIsNeedUpgrade(String localXMLPath, String upgradeXMLPath) {
        logger.info("FileTools ckeckIsNeedUpgrade localXMLPath:" + localXMLPath + " , upgradeXMLPath: "  + upgradeXMLPath);
        try {
            // 检查文件是否存在
            File localXMLFile = new File(localXMLPath);
            File upgradeXMLFile = new File(upgradeXMLPath);
            if(!localXMLFile.exists() || !upgradeXMLFile.exists()) {
                respBean = RespBean.error("本地XML文件 或 升级包XML文件不存在！！");
                return false;
            }
            long localVersionTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(XMLParserImpl.getInstance(localXMLPath).readXMLGetVersionTimeReturnString()).getTime();
            long upgradeVersionTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(XMLParserImpl.getInstance(upgradeXMLPath).readXMLGetVersionTimeReturnString()).getTime();
            if(upgradeVersionTime > localVersionTime) {
                respBean = RespBean.ok("需要升级");
                return true;
            }
            respBean = RespBean.error("已是最新版本， 不需要升级 ！！");
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            respBean = RespBean.error("版本比较异常，" + e.getMessage());
            return false;
        }
    }


    /**
     * 解压升级包文件
     * @param upgradeFilePath
     * @param propertyChangeListener
     * @return
     */
    public static boolean unZipFile(String upgradeFilePath, File unZipFolder, PropertyChangeListener propertyChangeListener) {
        // 解压完成的标记
        boolean unZipFileFlag = false;
        // String unZipFolder = upgradeFilePath.substring(0, upgradeFilePath.lastIndexOf(".zip"));
        logger.info("FileTools unZipFile filePath:" + upgradeFilePath + " , unZipFolder: " + unZipFolder.getAbsolutePath());
        try {
            // 解压文件到指定文件夹
            File upgradeFile = new File(upgradeFilePath);
            long totalSize = upgradeFile.length();  //总大小
            long readSize = 0;
            ZipFile zipFile = null;
            // 开始解压
            try {
                //根据ZIP文件创建ZIPFile对象，此类的作用是从ZIP文件读取条目
                zipFile = new ZipFile(upgradeFile, Charset.forName("GBK"));

                for(Enumeration<?> entries = zipFile.entries(); entries.hasMoreElements();) {
                    ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                    String zipEntryName = zipEntry.getName();
                    if(ViewConfig.OSName.contains("win")) {
                        zipEntryName = zipEntry.getName().replaceAll("/", "\\\\");
                    }
                    logger.info("FileTools unZipFile zipEntryName：" + zipEntryName);
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = zipFile.getInputStream(zipEntry);
                        String outPath = unZipFolder.getAbsolutePath().substring(0, unZipFolder.getAbsolutePath().lastIndexOf("/") + 1) + zipEntryName;
                        if(ViewConfig.OSName.contains("win")) {
                            outPath = unZipFolder.getAbsolutePath().substring(0, unZipFolder.getAbsolutePath().lastIndexOf("\\") + 1) + zipEntryName;
                        }
                        logger.info("FileTools unZipFile outPath：" + outPath);
                        //判断路径是否存在，不存在则创建文件路径
                        File file = null;
                        if(ViewConfig.OSName.contains("win")){
                            file = new File(outPath.substring(0, outPath.lastIndexOf("\\")));
                        }else {
                            file = new File(outPath.substring(0, outPath.lastIndexOf("/")));
                        }
                        if(!file.exists()) {
                            file.mkdirs();
                            logger.info("mkdir2 file :" + file.getAbsolutePath());
                        }
                        if(new File(outPath).isDirectory()){
                            continue;
                        }

                        out = new FileOutputStream(outPath);
                        byte[] bytes = new byte[1024];
                        int len;
                        while ((len = in.read(bytes)) > 0) {
                            out.write(bytes, 0, len);
                        }
                        Integer oldValue = (int)((readSize * 1.0 / totalSize) * 100);  //已解压的字节大小占总字节的百分
                        readSize += zipEntry.getCompressedSize();// 累加字节长度
                        Integer newValue = (int) ((readSize * 1.0 / totalSize) * 100);// 已解压的字节大小占总字节的大小的百分比
                        if (propertyChangeListener != null) {// 通知调用者解压进度发生改变
                            propertyChangeListener.propertyChange(new PropertyChangeEvent(upgradeFile, "progress", oldValue, newValue));
                        }
                        unZipFileFlag = true;  //标记解压完成
                    }catch (Exception e) {
                        e.printStackTrace();
                        respBean = RespBean.error("FileTools unZipFile error e1:" + e.getMessage());
                    }finally {
                        if(null != in) {
                            in.close();
                        }
                        if(null != out) {
                            out.close();
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                respBean = RespBean.error("FileTools unZipFile error e2:" + e.getMessage());
            }finally {
                if(zipFile != null) {
                    try {
                        zipFile.close();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            respBean = RespBean.error("FileTools unZipFile error e3:" + e.getMessage());
        }
        return unZipFileFlag;
    }

    /**
     * 清空文件夹
     * @param file
     * @return
     */
    public static boolean clearFolder(File file) {
        logger.info("FileTools clearFolder folder:" + file.getAbsolutePath());
        try {
            if(ViewConfig.OSName.contains("win")) {
                if(file.getAbsolutePath().contains("upgradePKG") && file.isDirectory()) {
                    File[] files = file.listFiles();
                    if(files != null) {
                        //循环子文件夹重复调用delete方法
                        for (File f : files) {
                            clearFolder(f);
                        }
                    }
                }
                if(file.getAbsolutePath().contains("upgradePKG") && !file.delete()) {
                    respBean = RespBean.error("delete file failed, file name: " + file.getName());
                    return false;
                }
            }else {
                if(file.getAbsolutePath().endsWith("upgradePKG") && file.isDirectory()) {
                    String command = "rm -rf " + file.getAbsolutePath();
                    if(!ShellCommandTools.runShellCommand(command)){
                        logger.error("FileTools clearFolder folder error !!");
                        respBean = RespBean.error("FileTools clearFolder folder error !!");
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            respBean = RespBean.error("delete file error, message: " + e.getMessage());
            return false;
        }
        respBean = RespBean.ok("FileTools clearFolder folder success !!");
        return true;
    }


    /**
     * 处理升级包中的 file
     * @param instance
     * @param upgradePath
     * @return
     */
    public static boolean dealXmlFileAttributeBean(XMLParserImpl instance, String upgradePath) {
        logger.info("FileTools dealXmlFileAttributeBean upgradePath :" + upgradePath);

        List<XmlFileAttributeBean> xmlFileAttributeBeanList = instance.getXmlFileAttributeBeanList("info/files/file");
        logger.info("FileTools dealXmlFileAttributeBean xmlFileAttributeBeanList:" + xmlFileAttributeBeanList);

        List<Boolean> collect = xmlFileAttributeBeanList.parallelStream().map(item -> dealXmlFileAttributeBeanByStream(item, upgradePath)).collect(Collectors.toList());
        return !collect.contains(false);


//        try {
//            for (XmlFileAttributeBean bean : xmlFileAttributeBeanList) {
//                logger.info("FileTools dealXmlFileAttributeBean fileName:" + bean.getFileName() + ", destDir:" + bean.getDestDir() + ", archiveType:" + bean.getArchiveType());
//                if(!StringUtils.isEmpty(bean.getFileName()) && !StringUtils.isEmpty(bean.getDestDir()) && (!bean.isArchive() || !StringUtils.isEmpty(bean.getArchiveType()))) {
//                    // 文件拷贝到指定路径
//                    File srcFile = new File(upgradePath.substring(0, upgradePath.lastIndexOf(".")) + "/" + bean.getFileName());
//                    if(srcFile.exists()) {
//                        // 镜像文件 或 不是压缩包
//                        if(bean.isImage() || !bean.isArchive()) {
//                            try {
//                                FileUtils.copyFileToDirectory(srcFile, new File(bean.getDestDir()));
////                                if(bean.isImage()) {
////                                    String command = "cd " + bean.getDestDir() + " && docker load --input " + bean.getFileName();
////                                    ShellCommandTools.runShellCommand(command);
////                                }
//                            }catch (Exception e) {
//                                logger.error("FileTools dealXmlFileAttributeBean copyFileToDirectory error, srcFile:" + bean.getFileName());
//                                respBean = RespBean.error("FileTools dealXmlFileAttributeBean copyFileToDirectory error, e:" + e.getMessage()).setObj(bean);
//                            }
//                        }else {
//                            // 软件包 压缩文件 需要解压到指定位置
//                            String command = "";
//                            switch (bean.getArchiveType()) {
//                                case "rar":
//                                    command = "rar x -o+ " + srcFile.getAbsolutePath() + " " + bean.getDestDir().substring(0, bean.getDestDir().lastIndexOf("/") + 1); break;
//                                case "zip":
//                                    command = "unzip -od " + bean.getDestDir().substring(0, bean.getDestDir().lastIndexOf("/") + 1) + " " + srcFile.getAbsolutePath(); break;
//                                default:
//                                    break;
//                            }
//                            logger.info("FileTools dealXmlFileAttributeBean command :" + command);
//                            if(!StringUtils.isEmpty(command)) {
//                                if(!ShellCommandTools.runShellCommand(command)){
//                                    logger.error("FileTools dealXmlFileAttributeBean runShellCommand error !!");
//                                    respBean = RespBean.error("FileTools dealXmlFileAttributeBean runShellCommand error !!");
//                                }
//                            }else {
//                                logger.error("FileTools dealXmlFileAttributeBean command is null, may be exist unknown archive type !!");
//                                respBean = RespBean.error("FileTools dealXmlFileAttributeBean command is null, may be exist unknown archive type !!");
//                            }
//                        }
//                    }
//
//                    // 目标文件夹绝对路径
//                    saveDestDir(bean.getDestDir());
//                }
//            }
//            dealXmlFileAttributeBeanFlag = true;
//        }catch (Exception e) {
//            respBean = RespBean.error("FileTools dealXmlFileAttributeBean command is null, may be exist unknown archive type !!");
//        }
//        return dealXmlFileAttributeBeanFlag;
    }


    private static boolean dealXmlFileAttributeBeanByStream(XmlFileAttributeBean bean, String upgradePath) {
        boolean dealXmlFileAttributeBeanFlag = false;
        try {
            logger.info("FileTools dealXmlFileAttributeBean fileName:" + bean.getFileName() + ", destDir:" + bean.getDestDir() + ", archiveType:" + bean.getArchiveType());
            if(!StringUtils.isEmpty(bean.getFileName()) && !StringUtils.isEmpty(bean.getDestDir()) && (!bean.isArchive() || !StringUtils.isEmpty(bean.getArchiveType()))) {
                // 文件拷贝到指定路径
                File srcFile = new File(upgradePath.substring(0, upgradePath.lastIndexOf(".")) + "/" + bean.getFileName());
                if(srcFile.exists()) {
                    // 镜像文件 或 不是压缩包
                    if(bean.isImage() || !bean.isArchive()) {
                        try {
                            FileUtils.copyFileToDirectory(srcFile, new File(bean.getDestDir()));
//                                if(bean.isImage()) {
//                                    String command = "docker rm $(docker ps -a |grep 'media'|awk '{print s1}') && docker rmi $(docker images|grep 'media'|awk '{print $3}') && cd " + bean.getDestDir() + " && docker load --input " + bean.getFileName();
//                                    if(bean.getFileName().contains("alg")) {
//                                        command = "docker rm $(docker ps -a |grep 'alg'|awk '{print s1}') && docker rmi $(docker images|grep 'alg'|awk '{print $3}') && cd " + bean.getDestDir() + " && docker load --input " + bean.getFileName();
//                                    }
//                                    ShellCommandTools.runShellCommand(command);
//                                }
                        }catch (Exception e) {
                            logger.error("FileTools dealXmlFileAttributeBean copyFileToDirectory error, srcFile:" + bean.getFileName());
                            respBean = RespBean.error("FileTools dealXmlFileAttributeBean copyFileToDirectory error, e:" + e.getMessage()).setObj(bean);
                        }
                    }else {
                        // 软件包 压缩文件 需要解压到指定位置
                        String command = "";
                        switch (bean.getArchiveType()) {
                            case "rar":
                                command = "rar x -o+ " + srcFile.getAbsolutePath() + " " + bean.getDestDir().substring(0, bean.getDestDir().lastIndexOf("/") + 1); break;
                            case "zip":
                                command = "unzip -od " + bean.getDestDir().substring(0, bean.getDestDir().lastIndexOf("/") + 1) + " " + srcFile.getAbsolutePath(); break;
                            default:
                                break;
                        }
                        logger.info("FileTools dealXmlFileAttributeBean command :" + command);
                        if(!StringUtils.isEmpty(command)) {
                            if(!ShellCommandTools.runShellCommand(command)){
                                logger.error("FileTools dealXmlFileAttributeBean runShellCommand error !! command:" + command);
                                respBean = RespBean.error("FileTools dealXmlFileAttributeBean runShellCommand error !!");
                            }
                        }else {
                            logger.error("FileTools dealXmlFileAttributeBean command is null, may be exist unknown archive type !!");
                            respBean = RespBean.error("FileTools dealXmlFileAttributeBean command is null, may be exist unknown archive type !!");
                        }
                    }
                }
                // 目标文件夹绝对路径
                saveDestDir(bean.getDestDir());
            }
            dealXmlFileAttributeBeanFlag = true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return dealXmlFileAttributeBeanFlag;
    }



    /**
     * 处理升级包中的 folder
     * @param instance
     * @return
     */
    public static boolean dealXmlFolderAttributeBean(XMLParserImpl instance, String upgradePath) {
        boolean dealXmlFolderAttributeBeanFlag = false;
        List<XmlFolderAttributeBean> xmlFolderAttributeBeanList = instance.getXmlFolderAttributeBeanList("info/folders/folder");
        logger.info("FileTools dealXmlFileAttributeBean xmlFolderAttributeBeanList:" + xmlFolderAttributeBeanList);
        try {
            String substring = upgradePath.substring(0, upgradePath.lastIndexOf(".zip"));
            xmlFolderAttributeBeanList.parallelStream().forEach(item -> dealXmlFolderAttributeBeanByStream(item, substring));
//            for (XmlFolderAttributeBean bean : xmlFolderAttributeBeanList) {
//                // 拷贝文件夹到指定位置
//                if(!StringUtils.isEmpty(bean.getFolderName()) && !StringUtils.isEmpty(bean.getDestDir())) {
//                    try {
//                        FileUtils.copyDirectory(new File(bean.getFolderName()), new File(bean.getDestDir()));
//                    }catch (Exception e) {
//                        logger.error("FileTools dealXmlFolderAttributeBean copyDirectory error, e:" + e.getMessage() + ", " + bean.toString());
//                        respBean = RespBean.error("FileTools dealXmlFolderAttributeBean copyDirectory error, e:" + e.getMessage()).setObj(bean);
//                    }
//                    // 目标文件夹绝对路径
//                    saveDestDir(bean.getDestDir());
//                }
//            }
            dealXmlFolderAttributeBeanFlag = true;
        }catch (Exception e) {
            respBean = RespBean.error("FileTools dealXmlFolderAttributeBean error, e:" + e.getMessage()).setObj(xmlFolderAttributeBeanList);
        }
        return dealXmlFolderAttributeBeanFlag;
    }

    private static void dealXmlFolderAttributeBeanByStream(XmlFolderAttributeBean bean, String upgradePath) {
        // 拷贝文件夹到指定位置
        if(!StringUtils.isEmpty(bean.getFolderName()) && !StringUtils.isEmpty(bean.getDestDir())) {
            try {
                FileUtils.copyDirectory(new File(upgradePath + "/" + bean.getFolderName()), new File(bean.getDestDir()));
            }catch (Exception e) {
                logger.error("FileTools dealXmlFolderAttributeBean copyDirectory error, e:" + e.getMessage() + ", " + bean.toString());
                respBean = RespBean.error("FileTools dealXmlFolderAttributeBean copyDirectory error, e:" + e.getMessage()).setObj(bean);
            }
            // 目标文件夹绝对路径
            saveDestDir(bean.getDestDir());
        }
    }

    private static void saveDestDir(String destDir) {
        if (!StringUtils.isEmpty(destDir)) {
            destDirList.add(destDir);
        }
    }


    /**
     * 读写文件操作
     * @param readFilPath
     * @param writeFilePath
     * @param propertyChangeListener
     * @return
     */
    public static boolean readAndWriteFile(String readFilPath, String writeFilePath, PropertyChangeListener propertyChangeListener) {
        boolean reultFlag = false;
        // 文件大小
        long length = 0;
        // 高效字符输入流
        BufferedReader bufferedReader = null;
        // 高效字符输出流
        BufferedWriter bufferedWriter = null;
        try {
            File readFile = new File(readFilPath);
            if(!readFile.exists()) {
                respBean = RespBean.error("读写操作时源文件不存在，请检查！！");
            }

            long totalSize = readFile.length();  //总大小
            long readSize = 0;

            File writeFile = new File(writeFilePath);
            if(!writeFile.exists()) {
                writeFile.mkdirs();
            }

            bufferedReader = new BufferedReader(new FileReader(readFile));
            bufferedWriter = new BufferedWriter(new FileWriter(writeFile));

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line + "\r\n"); //写入文件
                bufferedWriter.flush();
                readSize += line.length();// 累加字符长度
                Integer newValue = (int) ((readSize * 1.0 / totalSize) * 100);// 已解压的字节大小占总字节的大小的百分比
                if (propertyChangeListener != null) {// 通知调用者解压进度发生改变
                    propertyChangeListener.propertyChange(new PropertyChangeEvent(readFile, "progress", readSize, newValue));
                }
            }
            if (propertyChangeListener != null) {// 通知调用者解压进度发生改变
                propertyChangeListener.propertyChange(new PropertyChangeEvent(readFile, "progress", readSize, 100));
            }
            reultFlag = true;
        }catch (Exception e) {
            e.printStackTrace();
            respBean = RespBean.error("读写操作时异常，e:" + e.getMessage());
        }finally{
            try {
                if(bufferedReader != null) {
                    bufferedReader.close();
                }
                if(bufferedWriter != null) {
                    bufferedWriter.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
                respBean = RespBean.error("读写操作时关闭输入输出流异常，e:" + e.getMessage());
            }
        }
        return reultFlag;
    }

}
