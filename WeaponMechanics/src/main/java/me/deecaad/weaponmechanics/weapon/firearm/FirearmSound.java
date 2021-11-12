package me.deecaad.weaponmechanics.weapon.firearm;

public enum FirearmSound {

    MAIN_HAND(1),
    OFF_HAND(2);

    private final int id;

    FirearmSound(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static String getDataKeyword() {
        return "firearm-action";
    }
}