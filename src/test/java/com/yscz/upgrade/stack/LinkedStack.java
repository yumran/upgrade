package com.yscz.upgrade.stack;

import java.io.Serializable;

public class LinkedStack<T> implements Stack<T>, Serializable {
    private static final long serialVersionUID = -8032551749144420280L;

    private Node<T> top;

    private int size;

    public LinkedStack() {
        this.top = new Node<T>();
    }

    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return top == null || top.data == null;
    }

    @Override
    public void push(T data) {
        if(data == null) {
            throw new StackException("data can`t be null !!");
        }
        if(this.top == null) {
            this.top = new Node<>(data);
        }else if (this.top.data == null) {
            this.top.data = data;
        }else {
            Node<T> p = new Node<>(data, this.top);
            top = p;
        }
        size++;
    }

    @Override
    public T peek() {
        if(isEmpty()) {
            throw new EmptyStackException("stack empty");
        }
        return top.data;
    }

    @Override
    public T pop() {
        if(isEmpty()) {
            throw new EmptyStackException("stack empty");
        }
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }


    public static void main(String[] args) {
        LinkedStack<String> stringLinkedStack = new LinkedStack<>();
        stringLinkedStack.push("A");
        stringLinkedStack.push("B");
        stringLinkedStack.push("C");
        int length = stringLinkedStack.size();
        for (int i = 0; i < length; i++) {
            System.out.println("s1.pop->" + stringLinkedStack.pop());
        }
    }

}
