package io.github.jinghui70.rainbow.dbaccess.fieldmapper.lob;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;
import io.github.jinghui70.rainbow.dbaccess.object.SimpleObject;

import java.sql.Types;
import java.util.List;
import java.util.Map;

@Table(name = "CLOB_OBJECT")
public class ComplexClobObject {

    @Id
    private int id;

    @Column(sqlType = Types.CLOB)
    private Map<String, List<SimpleObject>> lobMap;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, List<SimpleObject>> getLobMap() {
        return lobMap;
    }

    public void setLobMap(Map<String, List<SimpleObject>> lobMap) {
        this.lobMap = lobMap;
    }
}
