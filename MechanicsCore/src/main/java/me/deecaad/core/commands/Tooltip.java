package me.deecaad.core.commands;

import java.util.Objects;

public interface Tooltip {

    String suggestion();

    String tip();

    static Tooltip of(Object str) {
        return of(str, null);
    }

    static Tooltip of(Object suggestion, Object tip) {
        return new Tooltip() {
            @Override
            public String suggestion() {
                return Objects.toString(suggestion, "null");
            }

            @Override
            public String tip() {
                return Objects.toString(tip, null);
            }
        };
    }
}
