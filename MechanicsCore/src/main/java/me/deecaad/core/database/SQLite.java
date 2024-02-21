package me.deecaad.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class SQLite extends HikariBased {

    public SQLite(String absolutePath) throws IOException, SQLException {
        super(DatabaseType.SQLITE);

        if (!absolutePath.endsWith(".db"))
            throw new IllegalArgumentException("Database has to end in .db " + "(" + absolutePath + ")");

        File db = new File(absolutePath);
        if (!db.exists()) {
            db.getParentFile().mkdirs();
            db.createNewFile();
        }

        HikariConfig config = new HikariConfig();

        config.setPoolName("WMSQLite");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + absolutePath);

        dataSource = new HikariDataSource(config);
    }
}