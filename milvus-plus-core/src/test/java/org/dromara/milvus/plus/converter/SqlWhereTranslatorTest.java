package org.dromara.milvus.plus.converter;

import org.dromara.milvus.plus.exception.MilvusPlusException;
import org.junit.Assert;
import org.junit.Test;

public class SqlWhereTranslatorTest {

    @Test
    public void basicAndOrEquals() {
        String expr = SqlWhereTranslator.toMilvusExpr("status = 1 AND type = 'A' OR level > 2");
        Assert.assertEquals("status == 1 && type == 'A' || level > 2", expr);
    }

    @Test
    public void leadingWhere() {
        String expr = SqlWhereTranslator.toMilvusExpr("WHERE name LIKE '%zhang%'");
        Assert.assertEquals("name like '%zhang%'", expr);
    }

    @Test
    public void inList() {
        String expr = SqlWhereTranslator.toMilvusExpr("type IN ('A', 'B')");
        Assert.assertEquals("type in ['A', 'B']", expr);
    }

    @Test
    public void isNull() {
        String expr = SqlWhereTranslator.toMilvusExpr("deleted IS NULL AND name IS NOT NULL");
        Assert.assertEquals("deleted == null && name != null", expr);
    }

    @Test
    public void notEquals() {
        String expr = SqlWhereTranslator.toMilvusExpr("status <> 0 AND status != 2");
        Assert.assertEquals("status != 0 && status != 2", expr);
    }

    @Test
    public void stringWithKeywordInside() {
        String expr = SqlWhereTranslator.toMilvusExpr("name = 'AND OR LIKE'");
        Assert.assertEquals("name == 'AND OR LIKE'", expr);
    }

    @Test(expected = MilvusPlusException.class)
    public void rejectSelect() {
        SqlWhereTranslator.toMilvusExpr("id IN (SELECT id FROM t)");
    }

    @Test(expected = MilvusPlusException.class)
    public void rejectOrderBy() {
        SqlWhereTranslator.toMilvusExpr("status = 1 ORDER BY id");
    }
}
