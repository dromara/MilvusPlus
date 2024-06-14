package org.dromara.milvus.demo.model;

import lombok.Data;

import java.util.List;

@Data
public class Person {
    private String name;
    private Integer age;
    private List<String> images;
}
