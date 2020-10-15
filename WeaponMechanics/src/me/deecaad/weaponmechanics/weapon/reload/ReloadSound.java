package me.deecaad.weaponmechanics.weapon.reload;

public enum ReloadSound {

    MAIN_HAND(1),
    OFF_HAND(2);

    private final int id;

    ReloadSound(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static String getDataKeyword() {
        return "reloadData";
    }
}