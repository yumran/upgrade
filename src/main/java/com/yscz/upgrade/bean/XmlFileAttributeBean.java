package com.yscz.upgrade.bean;

public class XmlFileAttributeBean {

    private String fileName;           //文件名

    private boolean archive;           //是否为压缩包

    private String  archiveType;       //压缩类型

    private boolean image;             // 是否是镜像

    private String destDir;            // 目标文件夹   -- 绝对路径


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public String getArchiveType() {
        return archiveType;
    }

    public void setArchiveType(String archiveType) {
        this.archiveType = archiveType;
    }

    public boolean isImage() {
        return image;
    }

    public void setImage(boolean image) {
        this.image = image;
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

}
