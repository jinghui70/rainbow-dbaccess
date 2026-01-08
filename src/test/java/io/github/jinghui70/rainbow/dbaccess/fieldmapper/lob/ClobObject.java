package io.github.jinghui70.rainbow.dbaccess.fieldmapper.lob;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;
import io.github.jinghui70.rainbow.dbaccess.object.SimpleObject;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Table(name = "LOB_OBJECT")
public class ClobObject {
    @Id
    private int id;

    @Column(sqlType = Types.CLOB)
    private String lobString;

    @Column(sqlType = Types.CLOB)
    private SimpleObject lobObject;

    @Column(sqlType = Types.CLOB)
    private SimpleObject[] lobArray;

    @Column(sqlType = Types.CLOB)
    private List<SimpleObject> lobList;

    @Column(sqlType = Types.CLOB)
    private Set<SimpleObject> lobSet;

    @Column(sqlType = Types.CLOB)
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

    public Set<SimpleObject> getLobSet() {
        return lobSet;
    }

    public void setLobSet(Set<SimpleObject> lobSet) {
        this.lobSet = lobSet;
    }

    public List<SimpleObject> getLobList() {
        return lobList;
    }

    public void setLobList(List<SimpleObject> lobList) {
        this.lobList = lobList;
    }

    public Map<String, List<SimpleObject>> getLobMap() {
        return lobMap;
    }

    public void setLobMap(Map<String, List<SimpleObject>> lobMap) {
        this.lobMap = lobMap;
    }
}

