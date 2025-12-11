package io.github.jinghui70.rainbow.dbaccess.mapper;

import cn.hutool.core.map.CamelCaseMap;

import java.util.Map;

public class CamelCaseMapMapper extends MapRowMapper {

    protected Map<String, Object> createColumnMap(int columnCount) {
        return new CamelCaseMap<>(columnCount);
    }

}
