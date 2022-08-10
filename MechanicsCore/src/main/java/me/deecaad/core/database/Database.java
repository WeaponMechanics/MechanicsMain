package me.deecaad.core.database;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

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
     * Runs given SQL statement in async.
     * Used to e.g. INSERT, UPDATE or DELETE.
     *
     * @param sql the sql statement to run
     */
    public void executeUpdate(String sql) {
        new BukkitRunnable() {
            public void run() {
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                try {
                    connection = getConnection();
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    MechanicsCore.debug.log(LogLevel.ERROR, e);
                } finally {
                    close(connection, preparedStatement, null);
                }
            }
        }.runTaskAsynchronously(MechanicsCore.getPlugin());
    }

    /**
     * Runs given SQL query in async.
     * Used to SELECT.
     *
     * @param sql the sql query to run
     * @param consumer the consumer for result set of query
     */
    public void executeQuery(String sql, Consumer<ResultSet> consumer) {
        new BukkitRunnable() {
            public void run() {
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet = null;
                try {
                    connection = getConnection();
                    preparedStatement = connection.prepareStatement(sql);
                    resultSet = preparedStatement.executeQuery();
                    consumer.accept(resultSet);
                } catch (SQLException e) {
                    MechanicsCore.debug.log(LogLevel.ERROR, e);
                } finally {
                    close(connection, preparedStatement, resultSet);
                }
            }
        }.runTaskAsynchronously(MechanicsCore.getPlugin());
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