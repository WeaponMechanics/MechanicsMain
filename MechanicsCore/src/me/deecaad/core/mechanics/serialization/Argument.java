package me.deecaad.core.mechanics.serialization;

import me.deecaad.core.mechanics.serialization.datatypes.DataType;

public class Argument {

    private String name;
    private DataType<?> type;
    private String[] aliases;

    public Argument(String name, DataType<?> type, String...aliases) {
        this.name = name;
        this.type = type;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType<?> getType() {
        return type;
    }

    public void setType(DataType<?> type) {
        this.type = type;
    }

    public String[] getAliases() {
        return aliases;
    }

    public void setAliases(String[] aliases) {
        this.aliases = aliases;
    }
    
    public boolean isArgument(String name) {
        name = name.toLowerCase();
        
        if (this.name.equalsIgnoreCase(name)) {
            return true;
        }
        
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(name)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public String toString() {
        return name + "=" + type;
    }
}
