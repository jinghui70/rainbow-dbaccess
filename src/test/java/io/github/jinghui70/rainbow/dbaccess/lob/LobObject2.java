package io.github.jinghui70.rainbow.dbaccess.lob;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.LobType;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;
import io.github.jinghui70.rainbow.utils.CommonObject;

import java.util.List;

@Table(name="LOB_OBJECT")
public class LobObject2 {
    @Id
    private int id;

    @Column(lobType = LobType.BLOB)
    private List<CommonObject> binObject;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<CommonObject> getBinObject() {
        return binObject;
    }

    public void setBinObject(List<CommonObject> binObject) {
        this.binObject = binObject;
    }
}
