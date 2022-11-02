package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.ArrayField;
import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Transient;
import io.github.jinghui70.rainbow.utils.StringBuilderX;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.JdbcUtils;

import java.lang.reflect.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.INSERT;
import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.MERGE;

class PropInfo {
    String fieldName;
    PropDesc prop;
    int type;
    int index;
    boolean key;

    PropInfo(String fieldName, PropDesc prop, int type, boolean key) {
        this(fieldName, prop, type, -1);
        this.key = key;
    }

    PropInfo(String fieldName, PropDesc prop, int type, int index) {
        this.fieldName = fieldName;
        this.prop = prop;
        this.type = type;
        this.index = index;
    }

    Object getValue(Object object) {
        Object value = prop.getValue(object);
        if (value == null) return null;
        if (index >= 0) {
            try {
                value = Array.get(value, index);
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
        return DbaUtil.enumCheck(value);
    }
}

public class ObjectDba<T> {

    protected Dba dba;
    protected Class<T> clazz;
    protected PropInfo[] propArray;

    public ObjectDba(Dba dba, Class<T> clazz) {
        this.dba = dba;
        this.clazz = clazz;
        List<PropInfo> props = new ArrayList<>();
        BeanUtil.descForEach(clazz, propDesc -> {
            if (propDesc.getField().getAnnotation(Transient.class) != null)
                return;
            Column column = propDesc.getField().getAnnotation(Column.class);
            int type = column == null ? SqlTypeValue.TYPE_UNKNOWN : column.type();
            String fieldName = column == null || StrUtil.isEmpty(column.name()) ?
                    StrUtil.toUnderlineCase(propDesc.getRawFieldName()) : column.name();
            ArrayField arrayAnnotation = propDesc.getField().getAnnotation(ArrayField.class);
            if (arrayAnnotation == null) {
                boolean key = propDesc.getField().getAnnotation(Id.class) != null;
                props.add( new PropInfo(fieldName, propDesc, type, key));
            } else {
                String join = arrayAnnotation.underline() ? "_" : "";
                for (int i = 0; i < arrayAnnotation.length(); i++) {
                    String field = String.format("%s%s%d", fieldName, join, i + arrayAnnotation.start());
                    props.add(new PropInfo(field, propDesc, type, i));
                }
            }
        });
        propArray = props.toArray(new PropInfo[0]);
    }

    protected String sql(String action, String table) {
        return new StringBuilderX(action).append(" into ").append(table).append("(")
                .join(propArray, p -> p.fieldName, StrUtil.COMMA)
                .append(") values(").repeat("?", propArray.length).append(")")
                .toString();
    }

    public int insert(T object) {
        return doInsert(DbaUtil.tableName(clazz), object, INSERT);
    }

    public int merge(T object) {
        return doInsert(DbaUtil.tableName(clazz), object, MERGE);
    }

    public int insert(String table, T object) {
        return doInsert(table, object, INSERT);
    }

    public int merge(String table, T object) {
        return doInsert(table, object, MERGE);
    }

    private int doInsert(String table, T object, String action) {
        Sql sql = dba.sql(action).append(" into ").append(table).append("(");
        for (PropInfo p : propArray) {
            Object value = p.prop.getValue(object);
            if (value == null) continue;
            if (p.index >= 0) {
                try {
                    value = Array.get(value, p.index);
                    if (value != null) {
                        sql.append(p.fieldName).appendTemp(StrUtil.COMMA);
                        sql.addTypeParam(DbaUtil.enumCheck(value), p.type);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // do Nothing
                }
            } else {
                sql.append(p.fieldName).appendTemp(StrUtil.COMMA);
                sql.addTypeParam(DbaUtil.enumCheck(value), p.type);
            }
        }
        sql.clearTemp().append(") VALUES (").repeat("?", sql.getParams().size()).append(")");
        return sql.execute();
    }

    public void insert(Collection<T> objects) {
        doInsert(DbaUtil.tableName(clazz), objects, INSERT, 0);
    }

    public void insert(String table, Collection<T> objects) {
        doInsert(table, objects, INSERT, 0);
    }

    public void insert(Collection<T> objects, int batchSize) {
        doInsert(DbaUtil.tableName(clazz), objects, INSERT, batchSize);
    }

    public void insert(String table, Collection<T> objects, int batchSize) {
        doInsert(table, objects, INSERT, batchSize);
    }

    public void merge(Collection<T> objects) {
        doInsert(DbaUtil.tableName(clazz), objects, MERGE, 0);
    }

    public void merge(String table, Collection<T> objects) {
        doInsert(table, objects, MERGE, 0);
    }

    public void merge(Collection<T> objects, int batchSize) {
        doInsert(DbaUtil.tableName(clazz), objects, MERGE, batchSize);
    }

    public void merge(String table, Collection<T> objects, int batchSize) {
        doInsert(table, objects, MERGE, batchSize);
    }

    public void doInsert(String table, Collection<T> objects, String action, int batchSize) {
        dba.getJdbcTemplate().execute(sql(action, table), (PreparedStatementCallback<int[]>) ps -> {
            if (JdbcUtils.supportsBatchUpdates(ps.getConnection())) {
                int i = 0;
                for (T t : objects) {
                    setValues(ps, t);
                    ps.addBatch();
                    if (batchSize > 0 && ++i == batchSize) {
                        ps.executeBatch();
                        i = 0;
                    }
                }
                if (batchSize == 0 || i > 0)
                    ps.executeBatch();
            } else {
                for (T t : objects) {
                    setValues(ps, t);
                    ps.executeUpdate();
                }
            }
            return new int[0]; // we don't care row's affect
        });
    }

    private void setValues(PreparedStatement ps, T object) throws SQLException {
        for (int i = 1; i <= propArray.length; i++) {
            PropInfo p = propArray[i - 1];
            StatementCreatorUtils.setParameterValue(ps, i, p.type, p.getValue(object));
        }
    }

    public int update(T object) {
        return update(DbaUtil.tableName(clazz), object);
    }

    public int update(String table, T object) {
        Sql sql = dba.update(table);
        for (PropInfo propInfo: propArray) {
            if (propInfo.key) {
                sql.where(propInfo.fieldName, propInfo.getValue(object));
            } else
                sql.set(propInfo.fieldName, propInfo.getValue(object));
        }
       return sql.execute();
    }

}
