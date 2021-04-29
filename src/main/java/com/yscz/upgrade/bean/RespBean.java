package com.yscz.upgrade.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RespBean implements Serializable {

    private static final long serialVersionUID = -7678857179058650385L;

    private boolean success = false;
    private int result ;
    private Integer status;
    private String message;
    private Object obj;

    public static RespBean build() {
        return new RespBean();
    }

    public static RespBean ok(String msg, Object obj) {
        return new RespBean(true, 0,200, msg, obj);
    }

    public static RespBean ok(String msg) {
        return new RespBean(true,0,200, msg, null);
    }

    public static RespBean ok() {
        return new RespBean(true,0,200, "ok", null);
    }

    public static RespBean error(String msg, Object obj) {
        return new RespBean(false,1,500, msg, obj);
    }

    public static RespBean error(String msg) {
        return new RespBean(false,1, 500, msg, null);
    }

}
