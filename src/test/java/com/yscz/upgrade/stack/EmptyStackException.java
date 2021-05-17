package com.yscz.upgrade.stack;

import java.io.Serializable;

public class EmptyStackException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = -2280752441291694339L;

    public EmptyStackException() {
        super();
    }

    public EmptyStackException(String msg) {
        super(msg);
    }

}
