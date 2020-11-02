package com.dcampus.common.util;

/**
 * Title: 统一响应结构
 * { "result": { "success": true, "message": "ok" }, "data": ... }
 */
public class Response {
    private static final String OK = "操作成功";
    private static final String ERROR = "操作失败";
    private Meta result;     // 结果数据
    private Object data;   // 响应内容

    public Response success() {
        this.result = new Meta("true", OK);
        return this;
    }

    public Response success(Object data) {
        this.result = new Meta("true", OK);
        this.data = data;
        return this;
    }

    public Response failure() {
        this.result = new Meta("false", ERROR);
        return this;
    }

    public Response failure(String message) {
        this.result = new Meta("false", message);
        return this;
    }

    public Response failure(String message, String code) {
        this.result = new Meta("false", message, code);
        return this;
    }

    public Meta getResult() {
        return result;
    }

    public Object getData() {
        return data;
    }

    /**
     * Title: 请求元数据
     *
     * @author mlpan
     */
    static public class Meta {
        private String success;
        private String message;
        private String code;

        public Meta(String success) {
            this.success = success;
        }

        public Meta(String success, String message) {
            this.success = success;
            this.message = message;
        }

        public Meta(String success, String message, String code) {
            this.success = success;
            this.message = message;
            this.code = code;
        }

        public String isSuccess() {
            return success;
        }

        public String getSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getCode() {
            return code;
        }
    }
}