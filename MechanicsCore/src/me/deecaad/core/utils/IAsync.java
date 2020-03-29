package me.deecaad.core.utils;

/**
 * Simple interface to help handling asynchronous tasks
 */
public interface IAsync {

    /**
     * Used with me.deecaad.core.utils.AsyncUtil class to run tasks asynchronously.
     * This execution is ran in asynchronously apart from server thread.
     * If this is null and there is callback, callback wont be ran
     *
     * @return the object to be used in callback or null
     */
    Object execute();
}
