package com.yscz.upgrade;

public class A {
    private static int numA1;
    private int numA2;

    static {
        System.out.println("A的静态字段：" + numA1);;
    }

    {
        System.out.println("A的成员变量：" + numA2);
    }

    public A() {
        System.out.println("A的构造器");
    }

    public A(int n) {
        System.out.println("A的有参构造");
        this.numA2 = n;
    }
}


class B extends A {
    private static int numB1;
    private int numB2;

    static {
        System.out.println("B的静态字段：" + numB1);;
    }

    {
        System.out.println("B的成员变量：" + numB2);
    }

    public B() {
        System.out.println("B的构造器");
    }

    public B(int n) {
        System.out.println("B的有参构造");
        this.numB2 = n;
    }
}

class Box {
    public static void main(String[] args) {
        B b = new B(1);

//        A ab = new B();
//        System.out.println("----------");
//        ab = new B();
    }
}