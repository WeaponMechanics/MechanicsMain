package me.deecaad.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQL extends HikariBased {

    public MySQL(String hostname, int port, String database, String username, String password) {
        super(DatabaseType.MYSQL);
        HikariConfig config = new HikariConfig();

        config.setPoolName("WMMySQL");
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?createDatabaseIfNotExist=true");
        config.setUsername(username);
        config.setPassword(password);

        dataSource = new HikariDataSource(config);
    }
}