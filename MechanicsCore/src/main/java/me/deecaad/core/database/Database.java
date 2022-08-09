package me.deecaad.core.database;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.LogLevel;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Database {

    private final DatabaseType type;

    public Database(DatabaseType type) {
        this.type = type;
    }

    /**
     * @return the type of this database
     */
    public DatabaseType getType() {
        return type;
    }

    /**
     * Closes all given variables.
     * This has to be called after every query.
     *
     * @param connection the connection used
     * @param preparedStatement the prepared statement used
     * @param resultSet the result set used
     */
    public void close(@Nullable Connection connection, @Nullable PreparedStatement preparedStatement, @Nullable ResultSet resultSet) {
        if (connection != null) try { connection.close(); } catch (SQLException e) { MechanicsCore.debug.log(LogLevel.ERROR, e); }
        if (preparedStatement != null) try { preparedStatement.close(); } catch (SQLException e) { MechanicsCore.debug.log(LogLevel.ERROR, e); }
        if (resultSet != null) try { resultSet.close(); } catch (SQLException e) { MechanicsCore.debug.log(LogLevel.ERROR, e); }
    }

    /**
     * Allows getting connections to the database.
     * When using MySQL this fetches one connection from the pool.
     * When using SQLite this creates new connection in most cases.
     *
     * @return one connection to the database
     */
    public abstract Connection getConnection() throws SQLException;

    /**
     * Has to be called on when server disables or plugin is reloaded
     */
    public abstract void close() throws SQLException;
}