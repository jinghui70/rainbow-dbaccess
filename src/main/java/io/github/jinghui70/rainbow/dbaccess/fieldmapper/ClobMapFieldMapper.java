package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.json.JSONUtil;

import java.util.Map;

public class ClobMapFieldMapper extends ClobFieldMapper<Map<String, Object>> {

    @Override
    public Map<String, Object> parse(String str) {
        return JSONUtil.parseObj(str);
    }

    @Override
    public String getString(Object value) {
        return JSONUtil.toJsonStr(value);
    }
}
