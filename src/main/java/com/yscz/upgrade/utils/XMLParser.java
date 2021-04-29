package com.yscz.upgrade.utils;

import org.dom4j.Document;
import org.dom4j.Node;

import java.util.List;

public interface XMLParser {

    String readXMLAnyNodeReturnString(String nodepath);

    Node readXMLAnyNodeReturnNode(String nodepath);

    List<Node> readXMLAnyNodeReturnNodeList(String nodepath);
}
