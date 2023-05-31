package io.github.jinghui70.rainbow.dbaccess;

public interface ICnd {

    void toSql(Sql sql);

    void toNamedSql(NamedSql sql);

}
