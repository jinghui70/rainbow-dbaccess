package io.github.jinghui70.rainbow.dbaccess.cnd;

import cn.hutool.core.lang.Assert;
import io.github.jinghui70.rainbow.dbaccess.NamedSql;
import io.github.jinghui70.rainbow.dbaccess.Sql;
import io.github.jinghui70.rainbow.utils.StringBuilderX;

import java.util.List;

public class CndPlus extends Cnd {

    private List<CndPlus> children;

    public List<CndPlus> getChildren() {
        return children;
    }

    public void setChildren(List<CndPlus> children) {
        this.children = children;
    }

    private String tag() {
        switch (field) {
            case AND:
            case OR:
                Assert.notEmpty(children, "{} CndPlus must have children", field);
                return field;
            default:
                return null;
        }
    }
    
    public void toSql(Sql sql) {
        String tag = tag();
        if (tag == null) super.toSql(sql);
        sql.append("(");
        for (CndPlus child : children) {
            child.toSql(sql);
            sql.appendTemp(tag);
        }
        sql.clearTemp().append(")");
    }

    @Override
    public void toNamedSql(NamedSql sql) {
        String tag = tag();
        if (tag == null) super.toNamedSql(sql);
        sql.append("(");
        for (CndPlus child : children) {
            child.toNamedSql(sql);
            sql.appendTemp(tag);
        }
        sql.clearTemp().append(")");
    }

    @Override
    public String toString() {
        String tag = tag();
        if (tag == null) super.toString();
        return new StringBuilderX("(").join(children, tag).append(")").toString();
    }
}
