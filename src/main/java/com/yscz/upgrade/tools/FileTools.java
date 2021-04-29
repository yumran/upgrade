package com.yscz.upgrade.tools;

import com.yscz.upgrade.bean.RespBean;
import com.yscz.upgrade.bean.XmlFileAttributeBean;
import com.yscz.upgrade.bean.XmlFolderAttributeBean;
import com.yscz.upgrade.utils.XMLParserImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                    String zipEntryName = zipEntry.getName().replaceAll("/", "\\\\");
                    logger.info("FileTools unZipFile zipEntryName：" + zipEntryName);
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = zipFile.getInputStream(zipEntry);
                        String outPath = unZipFolder.getAbsolutePath() + zipEntryName;
                        logger.info("FileTools unZipFile outPath：" + outPath);
                        //判断路径是否存在，不存在则创建文件路径
                        File file = new File(outPath.substring(0, outPath.lastIndexOf("\\")));
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
            if(file.isDirectory()) {
                File[] files = file.listFiles();
                if(files != null) {
                    //循环子文件夹重复调用delete方法
                    for (File f : files) {
                        clearFolder(f);
                    }
                }
            }
            if(!file.delete()) {
                respBean = RespBean.error("delete file failed, file name: " + file.getName());
                return false;
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
        List<XmlFileAttributeBean> xmlFileAttributeBeanList = instance.getXmlFileAttributeBeanList("info/files/file");

        try {
            for (XmlFileAttributeBean bean : xmlFileAttributeBeanList) {
                // 文件拷贝到指定路径
                File srcFile = new File(upgradePath.substring(0, upgradePath.lastIndexOf(".")) + "/" + bean.getFileName());
                if(srcFile.exists()) {
                    // 镜像文件 或 不是压缩包
                    if(bean.isImage() || !bean.isArchive()) {
                        try {
                            FileUtils.copyFileToDirectory(srcFile, new File(bean.getDestDir()));
                        }catch (Exception e) {
                            logger.error("FileTools dealXmlFileAttributeBean copyFileToDirectory error, srcFile:" + bean.getFileName());
                            respBean = RespBean.error("FileTools dealXmlFileAttributeBean copyFileToDirectory error, e:" + e.getMessage()).setObj(bean);
                            return false;
                        }
                    }else {
                        // 软件包 压缩文件 需要解压到指定位置
                        String command = "";
                        switch (bean.getArchiveType()) {
                            case "rar":
                                command = "unrar x " + srcFile.getAbsolutePath() + " " + bean.getDestDir(); break;
                            case "zip":
                                command = "unzip -d " + bean.getDestDir() + " " + srcFile.getAbsolutePath(); break;
                            default:
                                break;
                        }

                        if(!StringUtils.isEmpty(command)) {
                            if(!ShellCommandTools.runShellCommand(command)){
                                logger.error("FileTools dealXmlFileAttributeBean runShellCommand error !!");
                                respBean = RespBean.error("FileTools dealXmlFileAttributeBean runShellCommand error !!");
                                return false;
                            }
                        }else {
                            logger.error("FileTools dealXmlFileAttributeBean command is null, may be exist unknown archive type !!");
                            respBean = RespBean.error("FileTools dealXmlFileAttributeBean command is null, may be exist unknown archive type !!");
                            return false;
                        }
                    }
                }

                // 目标文件夹绝对路径
                saveDestDir(bean.getDestDir());
            }
        }catch (Exception e) {
            respBean = RespBean.error("FileTools dealXmlFileAttributeBean command is null, may be exist unknown archive type !!");
            return false;
        }
        return true;
    }


    /**
     * 处理升级包中的 folder
     * @param instance
     * @return
     */
    public static boolean dealXmlFolderAttributeBean(XMLParserImpl instance) {
        List<XmlFolderAttributeBean> xmlFolderAttributeBeanList = instance.getXmlFolderAttributeBeanList("info/folders/folder");
        try {
            for (XmlFolderAttributeBean bean : xmlFolderAttributeBeanList) {
                // 拷贝文件夹到指定位置
                try {
                    FileUtils.copyDirectory(new File(bean.getFolderName()), new File(bean.getDestDir()));
                }catch (Exception e) {
                    logger.error("FileTools dealXmlFolderAttributeBean copyDirectory error, e:" + e.getMessage() + ", " + bean.toString());
                    respBean = RespBean.error("FileTools dealXmlFolderAttributeBean copyDirectory error, e:" + e.getMessage()).setObj(bean);
                    return false;
                }
                // 目标文件夹绝对路径
                saveDestDir(bean.getDestDir());
            }
        }catch (Exception e) {
            respBean = RespBean.error("FileTools dealXmlFolderAttributeBean error, e:" + e.getMessage()).setObj(xmlFolderAttributeBeanList);
            return false;
        }
        return true;
    }

    private static void saveDestDir(String destDir) {
        if (!StringUtils.isEmpty(destDir)) {
            destDirList.add(destDir);
        }
    }
}