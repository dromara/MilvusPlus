package org.dromara.milvus.plus.exception;

/**
 * MilvusPlus 统一运行时异常，便于业务侧识别与处理。
 */
public class MilvusPlusException extends RuntimeException {

    private final String code;

    public MilvusPlusException(String message) {
        this("MILVUS_PLUS_ERROR", message);
    }

    public MilvusPlusException(String code, String message) {
        super(message);
        this.code = code;
    }

    public MilvusPlusException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public MilvusPlusException(String message, Throwable cause) {
        this("MILVUS_PLUS_ERROR", message, cause);
    }

    public String getCode() {
        return code;
    }

    public static MilvusPlusException of(String code, String message) {
        return new MilvusPlusException(code, message);
    }

    public static MilvusPlusException wrap(Throwable cause) {
        if (cause instanceof MilvusPlusException) {
            return (MilvusPlusException) cause;
        }
        String msg = cause == null ? "unknown error" : cause.getMessage();
        return new MilvusPlusException("MILVUS_SDK_ERROR", msg == null ? cause.toString() : msg, cause);
    }
}
