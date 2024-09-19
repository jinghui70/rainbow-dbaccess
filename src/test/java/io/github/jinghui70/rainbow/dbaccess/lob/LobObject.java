package io.github.jinghui70.rainbow.dbaccess.lob;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.LobType;
import io.github.jinghui70.rainbow.dbaccess.object.SimpleObject;

public class LobObject {
    @Id
    private int id;

    @Column(lobType = LobType.CLOB)
    private String content;

    @Column(lobType = LobType.BLOB)
    private String binString;

    @Column(lobType = LobType.BLOB)
    private byte[] binByteArray;

    @Column(lobType = LobType.BLOB)
    private SimpleObject binObject;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBinString() {
        return binString;
    }

    public void setBinString(String binString) {
        this.binString = binString;
    }

    public byte[] getBinByteArray() {
        return binByteArray;
    }

    public void setBinByteArray(byte[] binByteArray) {
        this.binByteArray = binByteArray;
    }

    public SimpleObject getBinObject() {
        return binObject;
    }

    public void setBinObject(SimpleObject binObject) {
        this.binObject = binObject;
    }
}

