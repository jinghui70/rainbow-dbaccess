package io.github.jinghui70.rainbow.dbaccess.fieldmapper.lob;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.object.SimpleObject;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlobObject {
    @Id
    private int id;

    @Column(sqlType = Types.BLOB)
    private String lobString;

    @Column(sqlType = Types.BLOB)
    private byte[] lobByteArray;

    @Column(sqlType = Types.BLOB)
    private SimpleObject lobObject;

    @Column(sqlType = Types.BLOB)
    private SimpleObject[] lobArray;

    @Column(sqlType = Types.BLOB)
    private List<SimpleObject> LobList;

    @Column(sqlType = Types.BLOB)
    private Set<SimpleObject> lobSet;

    @Column(sqlType = Types.BLOB)
    private Map<String, List<SimpleObject>> lobMap;

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

    public SimpleObject[] getLobArray() {
        return lobArray;
    }

    public void setLobArray(SimpleObject[] lobArray) {
        this.lobArray = lobArray;
    }

    public List<SimpleObject> getLobList() {
        return LobList;
    }

    public void setLobList(List<SimpleObject> lobList) {
        LobList = lobList;
    }

    public Set<SimpleObject> getLobSet() {
        return lobSet;
    }

    public void setLobSet(Set<SimpleObject> lobSet) {
        this.lobSet = lobSet;
    }

    public Map<String, List<SimpleObject>> getLobMap() {
        return lobMap;
    }

    public void setLobMap(Map<String, List<SimpleObject>> lobMap) {
        this.lobMap = lobMap;
    }
}
