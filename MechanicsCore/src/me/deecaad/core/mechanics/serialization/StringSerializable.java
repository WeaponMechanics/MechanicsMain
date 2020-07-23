package me.deecaad.core.mechanics.serialization;

import java.util.Collection;
import java.util.Map;

public abstract class StringSerializable<T> {

    protected String name;
    protected Argument[] args;
    
    protected StringSerializable(String name, Argument...args) {
        this.name = name;
        this.args = args;
    }

    protected StringSerializable(String name, Collection<? extends Argument> args) {
        this.name = name;
        this.args = args.toArray(new Argument[0]);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Argument[] getArgs() {
        return args;
    }

    public void setArgs(Argument[] args) {
        this.args = args;
    }

    public abstract T serialize(Map<String, Object> data);
}
