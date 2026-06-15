package io.github.jinghui70.rainbow.dbaccess.model;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;

import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * BLOB/CLOB 类型实体，测试对象序列化、压缩等场景。
 */
@Table(name = "T_BLOB")
public class BlobEntity {

    @Id
    private String id;

    /** 字符串 BLOB */
    @Column(sqlType = Types.BLOB)
    private String lobString;

    /** 字节数组 BLOB */
    @Column(sqlType = Types.BLOB)
    private byte[] lobBytes;

    /** 对象 BLOB（JSON 序列化 + 压缩） */
    @Column(sqlType = Types.BLOB, compress = true)
    private ObjectBlob lobObject;

    public BlobEntity() {
    }

    public BlobEntity(String id, String lobString, byte[] lobBytes, ObjectBlob lobObject) {
        this.id = id;
        this.lobString = lobString;
        this.lobBytes = lobBytes;
        this.lobObject = lobObject;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLobString() { return lobString; }
    public void setLobString(String lobString) { this.lobString = lobString; }
    public byte[] getLobBytes() { return lobBytes; }
    public void setLobBytes(byte[] lobBytes) { this.lobBytes = lobBytes; }
    public ObjectBlob getLobObject() { return lobObject; }
    public void setLobObject(ObjectBlob lobObject) { this.lobObject = lobObject; }

    /**
     * 嵌套对象，用于测试 JSON 序列化到 BLOB。
     */
    public static class ObjectBlob {
        private String name;
        private int count;
        private List<String> tags;
        private Map<String, Integer> scores;

        public ObjectBlob() {
        }

        public ObjectBlob(String name, int count, List<String> tags, Map<String, Integer> scores) {
            this.name = name;
            this.count = count;
            this.tags = tags;
            this.scores = scores;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public Map<String, Integer> getScores() { return scores; }
        public void setScores(Map<String, Integer> scores) { this.scores = scores; }
    }
}
