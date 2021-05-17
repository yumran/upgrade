package com.yscz.upgrade;

import com.yscz.upgrade.tools.FileTools;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
class UpgradeApplicationTests {


    private int num;

    private List<String> stringList = new ArrayList<>();

    @Test
    void contextLoads() {
        File file = new File("F:\\EDA\\upgradePKG");
        boolean b = FileTools.clearFolder(file);
        System.out.println("clearFolder result :" + b);
    }


    @Test
    void test() {

        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> map1 = new HashMap<>();
        map1.put("oneFrameRisk", 10);
        list.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("oneFrameRisk", 20);
        list.add(map2);

        int oneFrameRisk = (int) Math.round(list.stream().mapToInt(item -> Integer.parseInt(item.get("oneFrameRisk").toString())).average().orElse(0D));
        System.out.println(oneFrameRisk);


        num = 2;
        int one = 3;
        if(one > this.num) {
            this.num = one;
        }
        System.out.println(num);


        List<String> list1 = new ArrayList<>();
        list1.add("11");
        list1.add("22");
        doDeal(list1);
        System.out.println(stringList);
        list1.clear();

        System.out.println(stringList);

    }

    private void doDeal(List<String> list1) {
        System.out.println("doDeal:" + stringList);
        stringList = list1;
        System.out.println("doDeal:" + stringList);

    }

    @Test
    public void doTest11() {

        boolean connecttable = isConnecttable("192.168.16.129", 1111);
        System.out.println(connecttable);
    }

    public boolean isConnecttable(String host, int port) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port));
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally{
            try {
                socket.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }


}
