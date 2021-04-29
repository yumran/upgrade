package com.yscz.upgrade.utils;

import com.yscz.upgrade.bean.XmlFileAttributeBean;
import com.yscz.upgrade.bean.XmlFolderAttributeBean;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class XMLParserImpl implements XMLParser {

    private static Document document = null;

    public static XMLParserImpl getInstance(String XMLFilePath) {
        return new XMLParserImpl(XMLFilePath);
    }

    private XMLParserImpl(String XMLFilePath) {
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(XMLFilePath);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readXMLGetVersionReturnString() {
        return readXMLAnyNodeReturnString("info/version");
    }

    public String readXMLGetVersionTimeReturnString() {
        return readXMLAnyNodeReturnString("info/dataTime");
    }

    @Override
    public String readXMLAnyNodeReturnString(String nodepath) {
        Element element = (Element) readXMLAnyNodeReturnNode(nodepath);
        return element.getText();
    }

    @Override
    public Node readXMLAnyNodeReturnNode(String nodepath) {
        List<Node> list = readXMLAnyNodeReturnNodeList(nodepath);
        return list.size() == 0 ? null : list.get(0);
    }

    @Override
    public List<Node> readXMLAnyNodeReturnNodeList(String nodepath) {
        return document.selectNodes(nodepath);
    }


    /**
     * 获取文件夹对象
     * @param nodepath
     * @return
     */
    public List<XmlFolderAttributeBean> getXmlFolderAttributeBeanList(String nodepath) {
        List<XmlFolderAttributeBean> list = new ArrayList<>();
        try {
            List<Node> nodeList = readXMLAnyNodeReturnNodeList(nodepath);
            for(Node node : nodeList) {
                Element element = (Element) node;

                XmlFolderAttributeBean bean = new XmlFolderAttributeBean();
                bean.setFolderName(element.elementText("folderName"));
                bean.setDestDir(element.elementText("destDir"));

                list.add(bean);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * 获取文件对象
     * @param nodepath
     * @return
     */
    public List<XmlFileAttributeBean> getXmlFileAttributeBeanList(String nodepath) {
        List<XmlFileAttributeBean> list = new ArrayList<>();
        try {
            List<Node> nodeList = readXMLAnyNodeReturnNodeList(nodepath);
            for (Node node : nodeList) {
                Element element = (Element) node;

                XmlFileAttributeBean bean = new XmlFileAttributeBean();
                bean.setFileName(element.elementText("fileName"));
                bean.setArchive(Boolean.parseBoolean(element.elementText("archive")));
                bean.setArchiveType(element.elementText("archiveType"));
                bean.setImage(Boolean.parseBoolean(element.elementText("image")));
                bean.setDestDir(element.elementText("destDir"));

                list.add(bean);
            }
            if(list.size() > 0) {
                // 镜像文件先处理
                list = list.stream().sorted(Comparator.comparing(XmlFileAttributeBean::isImage)).collect(Collectors.toList());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
