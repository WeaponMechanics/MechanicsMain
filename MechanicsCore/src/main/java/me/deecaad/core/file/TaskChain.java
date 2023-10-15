package me.deecaad.core.file;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.function.Function;

public class TaskChain {

    // * ----- TASK CLASS ----- * //

    private class Task {

        private final Function<Object, Object> function;
        private final boolean async;
        private Task next;

        public Task(Function<Object, Object> function, boolean async) {
            this.function = function;
            this.async = async;
        }

        public void run(Plugin plugin, Object callback) {
            BukkitRunnable bukkitTask = new BukkitRunnable() {
                @Override
                public void run() {
                    Object nextCallBack = function.apply(callback);

                    synchronized (lock) {

                        queue.pop();
                        if (next != null)
                            next.run(plugin, nextCallBack);
                        else {
                            queue.callback = callback;
                            running = false;
                        }
                    }
                }
            };

            if (async)
                bukkitTask.runTaskAsynchronously(plugin);
            else
                bukkitTask.runTask(plugin);
        }
    }

    // * ----- CHAIN CLASS ----- * //

    private class ChainQueue {

        private Task head;
        private Task tail;

        private Object callback;

        ChainQueue() {
        }

        void push(Task task) {
            synchronized (lock) {
                if (head == null) {
                    head = tail = task;
                    return;
                }

                tail.next = task;
                tail = tail.next;
            }
        }

        void pop() {
            synchronized (lock) {
                if (head == null)
                    throw new NoSuchElementException();

                head = head.next;
            }
        }

        void run() {
            synchronized (lock) {
                if (head == null)
                    throw new NoSuchElementException();

                Object temp = callback;
                callback = null;
                head.run(plugin, temp);
            }
        }
    }

    // * ----- END OF INNER CLASSES ----- * //

    private final Plugin plugin;
    private final ChainQueue queue;
    private boolean running;

    private final Object lock = new Object();

    public TaskChain(Plugin plugin) {
        this.plugin = plugin;
        this.queue = new ChainQueue();
    }

    public TaskChain thenRunSync(@NotNull Function<Object, Object> function) {
        if (function == null)
            throw new IllegalArgumentException("Function cannot be null");

        Task task = new Task(function, false);
        run(task);
        return this;
    }

    public TaskChain thenRunAsync(@NotNull Function<Object, Object> function) {
        if (function == null)
            throw new IllegalArgumentException("Function cannot be null");

        Task task = new Task(function, true);
        run(task);
        return this;
    }

    public TaskChain thenRunSync(@NotNull Runnable runnable) {
        if (runnable == null)
            throw new IllegalArgumentException("Function cannot be null");

        Task task = new Task((ignore) -> { runnable.run(); return null; }, false);
        run(task);
        return this;
    }

    public TaskChain thenRunAsync(@NotNull Runnable runnable) {
        if (runnable == null)
            throw new IllegalArgumentException("Function cannot be null");

        Task task = new Task((ignore) -> { runnable.run(); return null; }, true);
        run(task);
        return this;
    }

    private void run(Task task) {
        synchronized (lock) {
            queue.push(task);

            if (!running) {
                queue.run();
                running = true;
            }
        }
    }
}
