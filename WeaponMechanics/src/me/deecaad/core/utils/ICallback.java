package me.deecaad.core.utils;

/**
 * Simple interface to help handling asynchronous tasks
 */
public interface ICallback {

    /**
     * Used with me.deecaad.core.utils.AsyncUtil class to run tasks asynchronously.
     * This execution is ran in sync with server.
     *
     * @param value the value that asynchronous task returned
     */
    void execute(Object value);
}
