package me.deecaad.weaponmechanics.weapon.firearm;

public enum FirearmState {

    READY(0),

    RELOAD_OPEN(1),
    RELOAD(2),
    RELOAD_CLOSE(3),

    SHOOT_OPEN(4),
    SHOOT_CLOSE(5);

    private final int id;

    FirearmState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}