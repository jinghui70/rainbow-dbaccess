package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Abstract base class for field mappers.
 */
public abstract class FieldMapper<T> {

    /**
     * Retrieve a field value from the JDBC {@code ResultSet}.
     *
     * @param rs    JDBC {@code ResultSet} to retrieve the field from.
     * @param index Column index of the field.
     * @return The processed field value.
     * @throws SQLException If a database access error occurs.
     */
    public abstract T formDB(ResultSet rs, int index) throws SQLException;

    /**
     * Save a field value to the JDBC {@code PreparedStatement}. Derived classes need to convert the value
     * into values that the database can accept.
     *
     * @param ps         JDBC {@code PreparedStatement} to save the field to.
     * @param paramIndex Parameter index of the field.
     * @param value      The field value to save.
     * @throws SQLException If a database access error occurs.
     */
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        ps.setObject(paramIndex, value);
    }

}
