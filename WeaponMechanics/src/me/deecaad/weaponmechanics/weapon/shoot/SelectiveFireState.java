package me.deecaad.weaponmechanics.weapon.shoot;

public enum SelectiveFireState {

    SINGLE(0),
    BURST(1),
    AUTO(2);

    private final int id;

    SelectiveFireState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}