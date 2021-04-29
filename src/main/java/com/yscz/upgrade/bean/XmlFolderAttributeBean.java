package com.yscz.upgrade.bean;

public class XmlFolderAttributeBean {

    private String folderName;          //文件夹名

    private String destDir;            // 目标文件夹   -- 绝对路径

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    @Override
    public String toString() {
        return "XmlFolderAttributeBean{" +
                "folderName='" + folderName + '\'' +
                ", destDir='" + destDir + '\'' +
                '}';
    }
}
