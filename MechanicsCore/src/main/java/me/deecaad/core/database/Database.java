package me.deecaad.core.database;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.Arrays;
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
     * @param statement the statement used
     * @param resultSet the result set used
     */
    public void close(@Nullable Connection connection, @Nullable Statement statement, @Nullable ResultSet resultSet) {
        if (connection != null) try { connection.close(); } catch (SQLException e) { MechanicsCore.debug.log(LogLevel.ERROR, e); }
        if (statement != null) try { statement.close(); } catch (SQLException e) { MechanicsCore.debug.log(LogLevel.ERROR, e); }
        if (resultSet != null) try { resultSet.close(); } catch (SQLException e) { MechanicsCore.debug.log(LogLevel.ERROR, e); }
    }

    /**
     * Runs given SQL statement in async.
     * Used to e.g. INSERT, UPDATE or DELETE.
     *
     * @param forceSync should only be true on reload and disable
     * @param sql the sql statement to run
     */
    public void executeUpdate(boolean forceSync, String... sql) {
        if (sql == null || sql.length == 0) throw new IllegalArgumentException("Empty statement");

        if (forceSync) {
            executeUpdate(sql);
            return;
        }

        new BukkitRunnable() {
            public void run() {
                executeUpdate(sql);
            }
        }.runTaskAsynchronously(MechanicsCore.getPlugin());
    }

    private void executeUpdate(String... sql) {
        if (sql.length == 1) {
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            try {
                connection = getConnection();
                preparedStatement = connection.prepareStatement(sql[0]);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                MechanicsCore.debug.log(LogLevel.ERROR, e);
            } finally {
                close(connection, preparedStatement, null);
            }
            return;
        }

        // When there is multiple SQL statements use batches
        Connection connection = null;
        Statement statement = null;

        try {
            connection = getConnection();
            statement = connection.createStatement();

            // Disable auto commit
            connection.setAutoCommit(false);

            // Add batches
            for (String sqlStatement : sql) {
                statement.addBatch(sqlStatement);
            }

            // Execute batches
            statement.executeBatch();

            // Finally actually commit the changes when everything is done
            connection.commit();
        } catch (SQLException e) {
            MechanicsCore.debug.log(LogLevel.ERROR, e);
        } finally {
            close(connection, statement, null);
        }
    }

    /**
     * Runs given SQL query in async.
     * Used to SELECT.
     * <p>
     * Keep in mind that the consumer is still ran in async.
     * You should wrap the data first and then do what you
     * wish with it using sync task.
     *
     * @param sql the sql query to run
     * @param consumer the consumer for result set of query
     */
    public void executeQuery(String sql, Consumer<ResultSet> consumer) {
        if (sql == null || sql.isEmpty() || consumer == null) throw new IllegalArgumentException("Empty statement or null consumer");
        //new BukkitRunnable() {
        //    public void run() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();

            aa(resultSet);
            //consumer.accept(resultSet);

        } catch (SQLException e) {
            MechanicsCore.debug.log(LogLevel.ERROR, e);
        } finally {
            close(connection, preparedStatement, resultSet);
        }
        //    }
        //}.runTaskAsynchronously(MechanicsCore.getPlugin());
    }

    private void aa(ResultSet rs) {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(" | ");
                    System.out.print(rs.getString(i));
                }
                System.out.println("-");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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