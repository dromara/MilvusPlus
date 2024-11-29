package org.dromara.milvus.plus.model;

/**
 * 枚举表示内置的过滤器类型。
 */
public enum BuiltInFilterType {
    lowercase, asciifolding, alphanumonly, cnalphanumonly, cncharonly, stop, length, stemmer
}