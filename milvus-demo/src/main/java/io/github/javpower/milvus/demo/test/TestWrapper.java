//package io.github.javpower.milvus.demo.test;
//
//import com.google.common.collect.Lists;
//import io.github.javpower.milvus.plus.core.conditions.MilvusWrapper;
//import io.github.javpower.milvus.plus.model.MilvusResp;
//
//import java.util.List;
//
//public class TestWrapper {
//    public static void main(String[] args) {
//        MilvusWrapper<Face> wrapper=new MilvusWrapper();
//        List<Float> vector = Lists.newArrayList(0.1f,0.2f,0.3f);
//        MilvusResp<Face> resp = wrapper.lambda()
//                .eq(Face::getPersonId,1l)
//                .addVector(vector)
//                .query();
//    }
//
//
//
//}
