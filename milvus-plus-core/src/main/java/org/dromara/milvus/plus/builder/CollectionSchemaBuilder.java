package org.dromara.milvus.plus.builder;

import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
/**
 * @author xgc
 **/
public class CollectionSchemaBuilder {

    private final String collectionName;
    private final MilvusClientV2 wrapper;
    private final CreateCollectionReq.CollectionSchema  schema;
    private ConsistencyLevel consistencyLevel=ConsistencyLevel.BOUNDED;
    private Boolean enableDynamicField=false;

    public CollectionSchemaBuilder(Boolean enableDynamicField,String collectionName, MilvusClientV2 wrapper) {
        this.collectionName = collectionName;
        this.wrapper = wrapper;
        this.schema = wrapper.createSchema();
        this.enableDynamicField=enableDynamicField;
    }
    public CollectionSchemaBuilder(String collectionName, MilvusClientV2 wrapper) {
        this.collectionName = collectionName;
        this.wrapper = wrapper;
        this.schema = wrapper.createSchema();
    }

    public CollectionSchemaBuilder addField(AddFieldReq field) {
        schema.addField(field);
        return this;
    }
    public CollectionSchemaBuilder addField(AddFieldReq ... fields) {
        for (AddFieldReq field : fields) {
            schema.addField(field);
        }
        return this;
    }
    public void addConsistencyLevel(ConsistencyLevel level){
        this.consistencyLevel=level;
    }

    public CreateCollectionReq.FieldSchema getField(String fileName){
        return schema.getField(fileName);
    }
    public void createSchema() throws MilvusException {
        CreateCollectionReq req=CreateCollectionReq.builder().
                collectionName(this.collectionName).
                collectionSchema(this.schema).
                consistencyLevel(this.consistencyLevel).
                enableDynamicField(this.enableDynamicField)
                .build();
        wrapper.createCollection(req);
    }
    public void createIndex(java.util.List<IndexParam> indexParams) throws MilvusException {
        CreateIndexReq req = CreateIndexReq.builder()
                .collectionName(collectionName)
                .indexParams(indexParams)
                .build();
        wrapper.createIndex(req);
    }
}