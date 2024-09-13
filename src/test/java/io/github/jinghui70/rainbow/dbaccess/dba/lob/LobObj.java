package io.github.jinghui70.rainbow.dbaccess.dba.lob;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.LobType;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;

@Table(name = "X")
public class LobObj {
    private int id;

    @Column(lobType = LobType.BLOB)
    private byte[] content;

    @Column(lobType = LobType.CLOB)
    private String info;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}

