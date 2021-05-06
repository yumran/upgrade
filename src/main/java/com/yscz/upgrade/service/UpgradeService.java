package com.yscz.upgrade.service;

import com.yscz.upgrade.bean.RespBean;
import com.yscz.upgrade.config.ViewConfig;
import com.yscz.upgrade.tools.BatFileTools;
import com.yscz.upgrade.tools.FileTools;
import com.yscz.upgrade.tools.ShellCommandTools;
import com.yscz.upgrade.utils.XMLParserImpl;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;

@Service
public class UpgradeService {

    private static Logger logger = LoggerFactory.getLogger(UpgradeService.class);

    /**
     * Windows 环境下升级软件包
     * @param upgradeBasicPath    升级包基础路径
     * @param upgradePath         升级包完整路径
     * @param localXmlPath
     * @param upgradeXmlPath
     * @return
     */
    public RespBean upgradeAplication_win(String upgradeBasicPath, String upgradePath, String localXmlPath, String upgradeXmlPath) {
        // 更新前的检验
        if (!upgradeCommon(upgradeBasicPath, upgradePath, localXmlPath, upgradeXmlPath)) return FileTools.respBean;
        // 执行卸载脚本
        BatFileTools.getInstance().execBatFile(upgradeXmlPath.substring(0, upgradeXmlPath.lastIndexOf("\\") + 1) + ViewConfig.uninstallFileName);    //执行升级包中的脚本文件
        // 第一版； 拷贝文件夹
        try {
            File srcDir = new File(upgradePath.substring(0, upgradePath.lastIndexOf(".zip")));
            // File destDir = new File(ViewConfig.programPath.substring(0, ViewConfig.programPath.lastIndexOf("\\")));
            File destDir = new File(localXmlPath.substring(0, localXmlPath.lastIndexOf("\\")));
            FileUtils.copyDirectory(srcDir, destDir, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return !pathname.getName().endsWith(".bat");
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
        BatFileTools.getInstance().execBatFile(upgradeXmlPath.substring(0, upgradeXmlPath.lastIndexOf("\\") + 1) + ViewConfig.installFileName);

        // 将upgradeXml文件写入localXml
        FileTools.readAndWriteFile(upgradeXmlPath, localXmlPath, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                logger.info("UpgradeService FileTools readAndWriteFile newValue:" + evt.getNewValue());
            }
        });
        return RespBean.ok();
    }


    /**
     * Linux 环境下升级软件包
     * @param upgradePath
     * @param localXmlPath
     * @param upgradeXmlPath
     * @return
     */
    public RespBean upgradeAplication_linux(String upgradeBasicPath, String upgradePath, String localXmlPath, String upgradeXmlPath) {
        // 更新前的检验
        if (!upgradeCommon(upgradeBasicPath, upgradePath, localXmlPath, upgradeXmlPath)) return FileTools.respBean;

        // xmlparser 对象
        XMLParserImpl instance = XMLParserImpl.getInstance(upgradeXmlPath);

        // 获取更新包 XML 对象 folders 下 的 folder
        if(!FileTools.dealXmlFolderAttributeBean(instance, upgradePath)) return FileTools.respBean;

        // 获取 更新包 XML 对象 files
        if (!FileTools.dealXmlFileAttributeBean(instance, upgradePath)) return FileTools.respBean;

        // 执行脚本文件
        if(!ShellCommandTools.runShellFiles(FileTools.destDirList)) return ShellCommandTools.respBean;

        // 重启 docker
        // String command = "systemctl restart docker";
        // if(!ShellCommandTools.runShellCommand(command)) return ShellCommandTools.respBean;

        // 将upgradeXml文件写入localXml
        FileTools.readAndWriteFile(upgradeXmlPath, localXmlPath, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                logger.info("UpgradeService FileTools readAndWriteFile newValue:" + evt.getNewValue());
            }
        });
        return RespBean.ok();
    }


    /**
     * window 和 Linux 版更新前的检验
     * @param upgradeBasicPath
     * @param upgradePath
     * @param localXmlPath
     * @param upgradeXmlPath
     * @return
     */
    private boolean upgradeCommon(String upgradeBasicPath, String upgradePath, String localXmlPath, String upgradeXmlPath) {
        //判断升级包是否存在
        if(!FileTools.checkFileExist(upgradePath) || !FileTools.checkFileExist(localXmlPath)) {
            return false;
        }

        // 清空解压目标文件夹
        File destDir = new File(upgradePath.substring(0, upgradePath.lastIndexOf(".zip")));
        if(!destDir.exists() || !destDir.isDirectory()) {
            destDir.mkdirs();
            logger.info("mkdir1 file :" + destDir.getAbsolutePath());
        }else if(!FileTools.clearFolder(destDir)) {
            return false;
        }


        // 升级包存在 -》 升级包解压
        boolean unZipFileFlag = FileTools.unZipFile(upgradePath, destDir, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                logger.info("UpgradeService upgradeAplication unZipFile newValue:" + evt.getNewValue());
            }
        });
        if(!unZipFileFlag) {
            return false;
        }

        // -》 检查本地版本 -》 升级包版本
        return FileTools.ckeckIsNeedUpgrade(localXmlPath, upgradeXmlPath);
    }
}
