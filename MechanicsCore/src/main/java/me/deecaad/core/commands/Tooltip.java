package me.deecaad.core.commands;

public interface Tooltip {

    String suggestion();

    String tip();

    static Tooltip of(String str) {
        return of(str, null);
    }

    static Tooltip of(String suggestion, String tip) {
        return new Tooltip() {
            @Override
            public String suggestion() {
                return suggestion;
            }

            @Override
            public String tip() {
                return tip;
            }
        };
    }
}
