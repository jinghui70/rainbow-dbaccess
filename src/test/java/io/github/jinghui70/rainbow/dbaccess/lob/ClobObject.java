package io.github.jinghui70.rainbow.dbaccess.lob;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.LobType;
import io.github.jinghui70.rainbow.dbaccess.object.SimpleObject;

public class ClobObject {
    @Id
    private int id;

    @Column(lobType = LobType.CLOB)
    private String lobString;

    @Column(lobType = LobType.CLOB)
    private SimpleObject lobObject;

    @Column(lobType = LobType.CLOB)
    private SimpleObject[] lobArray;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLobString() {
        return lobString;
    }

    public void setLobString(String lobString) {
        this.lobString = lobString;
    }

    public SimpleObject getLobObject() {
        return lobObject;
    }

    public void setLobObject(SimpleObject lobObject) {
        this.lobObject = lobObject;
    }

    public SimpleObject[] getLobArray() {
        return lobArray;
    }

    public void setLobArray(SimpleObject[] lobArray) {
        this.lobArray = lobArray;
    }
}

