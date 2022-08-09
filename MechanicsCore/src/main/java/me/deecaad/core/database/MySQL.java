package me.deecaad.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQL extends Database {

    private final HikariDataSource dataSource;

    public MySQL(String hostname, int port, String database, String username, String password) {
        super(DatabaseType.MYSQL);
        HikariConfig config = new HikariConfig();

        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);

        dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) dataSource.close();
    }
}