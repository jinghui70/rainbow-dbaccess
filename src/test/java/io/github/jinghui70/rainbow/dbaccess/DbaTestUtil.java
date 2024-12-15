package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.jinghui70.rainbow.dbaccess.memory.DataType;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDba;
import io.github.jinghui70.rainbow.dbaccess.memory.Table;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.*;

public class DbaTestUtil {

    public static void initTable(MemoryDba dba, String... tables) {
        Set<String> tableSet = new HashSet<>();
        for (String table : tables)
            tableSet.add(table.toUpperCase());

        String resourcePath = "classpath:dba.pdma.json";
        Resource resource = ResourceUtil.getResourceObj(resourcePath);
        // 读取资源内容
        String jsonContent = resource.readUtf8Str();
        JSONObject json = JSONUtil.parseObj(jsonContent);
        // 读取数据类型
        Map<String, DataType> typeMap = readTypeMap(json);
        // 读取数据域
        Map<String, DataDomain> domainMap = readDomainMap(json, typeMap);

        JSONArray entities = json.getJSONArray("entities");
        for (int i = 0; i < entities.size(); i++) {
            JSONObject entity = entities.getJSONObject(i);
            String name = entity.getStr("defKey").toUpperCase();
            if (!tableSet.contains(name)) continue;
            dba.sql("DROP TABLE IF EXISTS " + name).execute();
            dba.createTable(readTable(name, entity.getJSONArray("fields"), typeMap, domainMap));
            tableSet.remove(name);
            if (tableSet.isEmpty()) break;
        }
    }

    private static Map<String, DataType> readTypeMap(JSONObject json) {
        Map<String, DataType> map = MapUtil.<String, DataType>builder()
                .put("string", DataType.VARCHAR)
                .put("double", DataType.NUMERIC)
                .put("int", DataType.INT)
                .put("date", DataType.TIMESTAMP)
                .put("bytes", DataType.BLOB)
                .put("largeText", DataType.CLOB)
                .build();
        JSONArray typeArray = json.getJSONObject("dataTypeMapping").getJSONArray("mappings");
        Map<String, DataType> result = new HashMap<>();
        typeArray.jsonIter().forEach(m -> {
            String id = m.getStr("id");
            String defKey = m.getStr("defKey");
            result.put(id, map.get(defKey));
        });
        return result;
    }

    private static Map<String, DataDomain> readDomainMap(JSONObject json, Map<String, DataType> typeMap) {
        Map<String, DataDomain> result = new HashMap<>();
        json.getJSONArray("domains").jsonIter().forEach(domain -> {
            DataDomain dd = new DataDomain();
            String id = domain.getStr("id");
            String type = domain.getStr("applyFor");
            dd.type = typeMap.get(type);
            dd.length = domain.getInt("len", 0);
            dd.scale = domain.getInt("scale", 0);
            result.put(id, dd);
        });
        return result;
    }

    private static Table readTable(String name, JSONArray fieldsJson, Map<String, DataType> typeMap, Map<String, DataDomain> domainMap) {
        List<Field> fields = new ArrayList<>(fieldsJson.size());
        fieldsJson.jsonIter().forEach(json -> {
            Field field = new Field();
            field.setName(json.getStr("defKey"));
            field.setKey(json.getBool("primaryKey", false));
            field.setAutoIncrement(json.getBool("autoIncrement", false));
            field.setMandatory(json.getBool("notNull"));
            Object defaultValue = json.getObj("defaultValue");
            if (defaultValue != null && !"".equals(defaultValue))
                field.setDefaultValue(defaultValue);
            DataDomain dd = domainMap.get(json.getStr("domain"));
            if (dd != null) {
                field.setType(dd.type);
                field.setLength(dd.length);
                field.setPrecision(dd.scale);
            } else {
                DataType type = typeMap.get(json.getStr("baseType"));
                if (type == null) {
                    String typeStr = json.getStr("type");
                    try {
                        type = DataType.valueOf(typeStr.toUpperCase());
                    } catch(IllegalArgumentException e) {
                        throw new IllegalArgumentException(StrUtil.format("字段[{}]的数据类型[{}]不支持", field.getName(), typeStr));
                    }
                }
                field.setType(type);
                field.setLength(json.getInt("len", 0));
                field.setPrecision(json.getInt("scale", 0));
            }
            fields.add(field);
        });
        return new Table(name, fields);
    }

    static class DataDomain {
        DataType type;
        int length;
        int scale;
    }
}
