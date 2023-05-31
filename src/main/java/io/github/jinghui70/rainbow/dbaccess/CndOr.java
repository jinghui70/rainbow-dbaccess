package io.github.jinghui70.rainbow.dbaccess;

import java.util.List;

public class CndOr implements ICnd {

    private List<ICnd> children;

    @Override
    public void toSql(Sql sql) {
        sql.append("(");
        for (ICnd child : children) {
            child.toSql(sql);
            sql.appendTemp(Cnd.OR);
        }
        sql.clearTemp().append(")");
    }

    @Override
    public void toNamedSql(NamedSql sql) {
        sql.append("(");
        for (ICnd child : children) {
            child.toNamedSql(sql);
            sql.appendTemp(Cnd.OR);
        }
        sql.clearTemp().append(")");
    }
}
