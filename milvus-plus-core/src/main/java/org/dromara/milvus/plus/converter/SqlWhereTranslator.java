package org.dromara.milvus.plus.converter;

import org.dromara.milvus.plus.exception.MilvusPlusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将「类 SQL WHERE 子集」翻译为 Milvus boolean expression。
 * <p>
 * 这不是完整 MySQL 方言，只覆盖日常过滤最常用的子集，方便 SQL 习惯用户上手。
 * <pre>
 *   status = 1 AND name LIKE '%张%' OR type IN ('A','B')
 *   =&gt; status == 1 && name like '%张%' || type in ['A','B']
 * </pre>
 *
 * <b>支持：</b> {@code = != <> > >= < <= AND OR NOT LIKE IN IS NULL IS NOT NULL ()}<br>
 * <b>不支持：</b> JOIN / 子查询 / 聚合 / 函数 / SELECT 列表 / ORDER BY / BETWEEN 等
 */
public final class SqlWhereTranslator {

    private static final Pattern LEADING_WHERE = Pattern.compile("^\\s*where\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern IN_LIST = Pattern.compile("(?i)\\bin\\s*\\(([^()]*)\\)");

    private SqlWhereTranslator() {
    }

    /**
     * @param sqlWhere 允许带或不带 WHERE 前缀的条件串
     * @return Milvus filter expression
     */
    public static String toMilvusExpr(String sqlWhere) {
        if (sqlWhere == null || sqlWhere.trim().isEmpty()) {
            return "";
        }
        String s = sqlWhere.trim();
        Matcher whereMatcher = LEADING_WHERE.matcher(s);
        if (whereMatcher.find()) {
            s = s.substring(whereMatcher.end());
        }

        LiteralMask mask = LiteralMask.mask(s);
        String body = mask.masked;

        body = replaceWord(body, "and", "&&");
        body = replaceWord(body, "or", "||");
        body = replaceWord(body, "not", "not");
        body = replaceWord(body, "like", "like");
        body = replaceWord(body, "in", "in");
        body = replaceWord(body, "is\\s+not\\s+null", "!= null");
        body = replaceWord(body, "is\\s+null", "== null");

        // 比较符：先处理多字符
        body = body.replace("<>", "!=");
        body = body.replace(">=", "\u0001GE\u0001");
        body = body.replace("<=", "\u0001LE\u0001");
        body = body.replace("!=", "\u0001NE\u0001");
        body = body.replace("==", "\u0001EQ\u0001");
        body = body.replace("=", "==");
        body = body.replace("\u0001GE\u0001", ">=");
        body = body.replace("\u0001LE\u0001", "<=");
        body = body.replace("\u0001NE\u0001", "!=");
        body = body.replace("\u0001EQ\u0001", "==");

        body = convertInLists(body);

        String restored = mask.restore(body);
        validateRough(restored);
        return collapseSpaces(restored).trim();
    }

    private static String replaceWord(String input, String wordPattern, String replacement) {
        return input.replaceAll("(?i)(?<![A-Za-z0-9_])" + wordPattern + "(?![A-Za-z0-9_])", replacement);
    }

    private static String convertInLists(String body) {
        Matcher m = IN_LIST.matcher(body);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "in [" + Matcher.quoteReplacement(m.group(1).trim()) + "]");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static void validateRough(String expr) {
        String lower = expr.toLowerCase(Locale.ROOT);
        // 用词边界粗检不支持的 SQL 结构
        if (containsSqlKeyword(lower, "select")
                || containsSqlKeyword(lower, "join")
                || lower.contains("group by")
                || lower.contains("order by")
                || containsSqlKeyword(lower, "union")
                || containsSqlKeyword(lower, "exists")
                || containsSqlKeyword(lower, "between")
                || containsSqlKeyword(lower, "from")) {
            throw MilvusPlusException.of("SQL_WHERE_UNSUPPORTED",
                    "sqlWhere only supports a filter subset (AND/OR/NOT/LIKE/IN/=/comparisons). " +
                            "Full SQL (SELECT/JOIN/ORDER BY/BETWEEN/...) is not supported: " + expr);
        }
    }

    private static boolean containsSqlKeyword(String lowerExpr, String keyword) {
        return Pattern.compile("(?i)(?<![A-Za-z0-9_])" + Pattern.quote(keyword) + "(?![A-Za-z0-9_])")
                .matcher(lowerExpr)
                .find();
    }

    private static String collapseSpaces(String s) {
        return s.replaceAll("[ \\t\\x0B\\f\\r]+", " ");
    }

    /**
     * 保护引号字符串，避免关键字替换破坏字面量。
     */
    private static final class LiteralMask {
        private final String masked;
        private final List<String> literals;

        private LiteralMask(String masked, List<String> literals) {
            this.masked = masked;
            this.literals = literals;
        }

        static LiteralMask mask(String s) {
            List<String> literals = new ArrayList<>();
            StringBuilder out = new StringBuilder(s.length());
            int i = 0;
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == '\'' || c == '"') {
                    char quote = c;
                    int j = i + 1;
                    StringBuilder lit = new StringBuilder();
                    while (j < s.length()) {
                        char d = s.charAt(j);
                        if (d == quote) {
                            // SQL escape ''
                            if (quote == '\'' && j + 1 < s.length() && s.charAt(j + 1) == '\'') {
                                lit.append('\'');
                                j += 2;
                                continue;
                            }
                            j++;
                            break;
                        }
                        lit.append(d);
                        j++;
                    }
                    String inner = lit.toString().replace("\\", "\\\\").replace("'", "\\'");
                    String milvusLiteral = "'" + inner + "'";
                    String token = "__MP_LIT_" + literals.size() + "__";
                    literals.add(milvusLiteral);
                    out.append(token);
                    i = j;
                } else {
                    out.append(c);
                    i++;
                }
            }
            return new LiteralMask(out.toString(), literals);
        }

        String restore(String maskedBody) {
            String r = maskedBody;
            for (int i = 0; i < literals.size(); i++) {
                r = r.replace("__MP_LIT_" + i + "__", literals.get(i));
            }
            return r;
        }
    }
}
