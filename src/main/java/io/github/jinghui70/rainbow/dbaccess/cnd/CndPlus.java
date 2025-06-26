package io.github.jinghui70.rainbow.dbaccess.cnd;

import cn.hutool.core.collection.CollStreamUtil;
import io.github.jinghui70.rainbow.dbaccess.Sql;
import io.github.jinghui70.rainbow.utils.StringBuilderX;

import java.util.List;

public class CndPlus extends Cnd {

    private String relation;

    private List<CndPlus> children;

    public List<CndPlus> getChildren() {
        return children;
    }

    public void toSql(Sql sql) {
        if (relation == null)
            super.toSql(sql);
        else {
            sql.append("(");
            for (CndPlus child : children) {
                child.toSql(sql);
                sql.appendTemp(relation);
            }
            sql.clearTemp().append(")");
        }
    }

    @Override
    public String toString() {
        if (relation == null) return super.toString();
        return new StringBuilderX("(").join(children, relation).append(")").toString();
    }

    public static CndPlus shrink(CndPlus cnd) {
        if (cnd.relation == null) return cnd;
        if (cnd.children.size() == 1) return shrink(cnd.children.get(0));
        cnd.children = CollStreamUtil.toList(cnd.children, CndPlus::shrink);
        return cnd;
    }
}