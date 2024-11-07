package io.github.jinghui70.rainbow.dbaccess.lob;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.LobType;
import io.github.jinghui70.rainbow.dbaccess.object.SimpleObject;

import java.util.List;

public class BlobObject {
    @Id
    private int id;

    @Column(lobType = LobType.BLOB)
    private String lobString;

    @Column(lobType = LobType.BLOB)
    private byte[] lobByteArray;

    @Column(lobType = LobType.BLOB)
    private SimpleObject lobObject;

    @Column(lobType = LobType.BLOB)
    private List<SimpleObject> lobArray;

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

    public byte[] getLobByteArray() {
        return lobByteArray;
    }

    public void setLobByteArray(byte[] lobByteArray) {
        this.lobByteArray = lobByteArray;
    }

    public SimpleObject getLobObject() {
        return lobObject;
    }

    public void setLobObject(SimpleObject lobObject) {
        this.lobObject = lobObject;
    }

    public List<SimpleObject> getLobArray() {
        return lobArray;
    }

    public void setLobArray(List<SimpleObject> lobArray) {
        this.lobArray = lobArray;
    }
}
