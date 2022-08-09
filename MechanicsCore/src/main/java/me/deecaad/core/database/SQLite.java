package me.deecaad.core.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLite extends Database {

    private final String absolutePath;
    private Connection connection;

    public SQLite(String absolutePath) throws IOException, SQLException {
        super(DatabaseType.SQLITE);

        if (!absolutePath.endsWith(".db")) throw new IllegalArgumentException("Database has to end in .db " + "(" + absolutePath + ")");

        File db = new File(absolutePath);
        if (!db.exists()) {
            db.getParentFile().mkdirs();
            db.createNewFile();
        }

        this.absolutePath = absolutePath;
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + absolutePath);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.connection == null || this.connection.isClosed()
                ? this.connection = DriverManager.getConnection("jdbc:sqlite:" + absolutePath)
                : this.connection;
    }

    @Override
    public void close() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) this.connection.close();
    }
}