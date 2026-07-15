package org.dromara.milvus.plus.core.conditions;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * 复杂 and/or/not 条件拼接回归测试（纯本地，不依赖 Milvus 服务）。
 */
public class ConditionBuilderLogicTest {

    /** 暴露 buildFilters 的测试用 builder */
    static class TestWrapper extends ConditionBuilder<Object> {
        public TestWrapper eq(String f, Object v) {
            super.eq(f, v);
            return this;
        }

        public TestWrapper ne(String f, Object v) {
            super.ne(f, v);
            return this;
        }

        public TestWrapper gt(String f, Object v) {
            super.gt(f, v);
            return this;
        }

        public TestWrapper like(String f, String v) {
            super.like(f, v);
            return this;
        }

        public TestWrapper likeRight(String f, String v) {
            super.likeRight(f, v);
            return this;
        }

        public TestWrapper between(String f, Object s, Object e) {
            super.between(f, s, e);
            return this;
        }

        public TestWrapper in(String f, java.util.List<?> values) {
            super.in(f, values);
            return this;
        }

        public TestWrapper and(TestWrapper other) {
            super.and(other);
            return this;
        }

        public TestWrapper or(TestWrapper other) {
            super.or(other);
            return this;
        }

        public TestWrapper not() {
            super.not();
            return this;
        }

        public TestWrapper not(TestWrapper other) {
            super.not(other);
            return this;
        }

        public TestWrapper textMatch(String f, String v) {
            super.textMatch(f, v);
            return this;
        }

        public String expr() {
            return buildFilters();
        }
    }

    @Test
    public void plainAndChain() {
        String expr = new TestWrapper().eq("status", 1).eq("type", "A").expr();
        Assert.assertEquals("status == 1 && type == 'A'", expr);
    }

    @Test
    public void orNestedGroup_internalAnd() {
        // (status == 1) || (type == 'A' && level == 2)
        TestWrapper nested = new TestWrapper().eq("type", "A").eq("level", 2);
        String expr = new TestWrapper().eq("status", 1).or(nested).expr();
        Assert.assertEquals("(status == 1) || (type == 'A' && level == 2)", expr);
    }

    @Test
    public void andNestedWithInnerOr() {
        // status == 1 && ((type == 'A') || (type == 'B'))
        TestWrapper orGroup = new TestWrapper().eq("type", "A").or(new TestWrapper().eq("type", "B"));
        String expr = new TestWrapper().eq("status", 1).and(orGroup).expr();
        Assert.assertEquals("(status == 1) && ((type == 'A') || (type == 'B'))", expr);
    }

    @Test
    public void complex_A_and_B_or_C_and_D() {
        // (a == 1 && b == 2) || (c == 3 && d == 4)
        TestWrapper left = new TestWrapper().eq("a", 1).eq("b", 2);
        TestWrapper right = new TestWrapper().eq("c", 3).eq("d", 4);
        String expr = left.or(right).expr();
        Assert.assertEquals("(a == 1 && b == 2) || (c == 3 && d == 4)", expr);
    }

    @Test
    public void notCurrentGroup() {
        String expr = new TestWrapper().eq("status", 1).eq("type", "A").not().expr();
        Assert.assertEquals("not (status == 1 && type == 'A')", expr);
    }

    @Test
    public void notNested() {
        TestWrapper nested = new TestWrapper().eq("deleted", true);
        String expr = new TestWrapper().eq("status", 1).not(nested).expr();
        Assert.assertEquals("status == 1 && not (deleted == true)", expr);
    }

    @Test
    public void buildFiltersIdempotent() {
        TestWrapper w = new TestWrapper().eq("status", 1).textMatch("content", "hello");
        String e1 = w.expr();
        String e2 = w.expr();
        Assert.assertEquals(e1, e2);
        Assert.assertEquals("status == 1 && TEXT_MATCH(content, 'hello')", e1);
    }

    @Test
    public void likeRightFixed() {
        String expr = new TestWrapper().likeRight("name", "zhang").expr();
        Assert.assertEquals("name like '%zhang'", expr);
    }

    @Test
    public void stringEscape() {
        String expr = new TestWrapper().eq("name", "O'Brien").expr();
        Assert.assertEquals("name == 'O\\'Brien'", expr);
    }

    @Test
    public void nullValue() {
        String expr = new TestWrapper().eq("name", null).expr();
        Assert.assertEquals("name == null", expr);
    }

    @Test
    public void betweenHasParen() {
        String expr = new TestWrapper().between("age", 18, 30).eq("status", 1).expr();
        Assert.assertEquals("(age >= 18 && age <= 30) && status == 1", expr);
    }

    @Test
    public void inList() {
        String expr = new TestWrapper().in("id", Arrays.asList(1, 2, 3)).expr();
        Assert.assertEquals("id in [1, 2, 3]", expr);
    }

    @Test
    public void deepNesting() {
        // status==1 && ( (type=='A' && level>1) || (type=='B') )
        TestWrapper branchA = new TestWrapper().eq("type", "A").gt("level", 1);
        TestWrapper branchB = new TestWrapper().eq("type", "B");
        TestWrapper orGroup = branchA.or(branchB);
        String expr = new TestWrapper().eq("status", 1).and(orGroup).expr();
        Assert.assertEquals("(status == 1) && ((type == 'A' && level > 1) || (type == 'B'))", expr);
    }

    @Test
    public void orThenAnd_preservesPrecedence() {
        // 链式 .or(...).eq(...) 应得到 ((a==1)||(b==2)) && c==3，而不是 a||(b&&c)
        TestWrapper w = new TestWrapper().eq("a", 1).or(new TestWrapper().eq("b", 2)).eq("c", 3);
        String expr = w.expr();
        Assert.assertEquals("((a == 1) || (b == 2)) && c == 3", expr);
    }

    @Test
    public void preferred_orThenAnd_withExplicitGroup() {
        // 推荐：((a==1) || (b==2)) && c==3
        TestWrapper orGroup = new TestWrapper().eq("a", 1).or(new TestWrapper().eq("b", 2));
        String expr = orGroup.and(new TestWrapper().eq("c", 3)).expr();
        Assert.assertEquals("((a == 1) || (b == 2)) && (c == 3)", expr);
    }
}
