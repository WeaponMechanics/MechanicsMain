package me.deecaad.core.database;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariBased extends Database {

    protected HikariDataSource dataSource;

    public HikariBased(DatabaseType type) {
        super(type);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() throws SQLException {
        if (dataSource != null && !dataSource.isClosed())
            dataSource.close();
    }

    @Override
    public boolean isClosed() {
        return dataSource == null || dataSource.isClosed();
    }
}
