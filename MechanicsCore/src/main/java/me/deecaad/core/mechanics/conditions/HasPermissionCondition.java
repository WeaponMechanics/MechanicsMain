package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HasPermissionCondition extends Condition {

    private String permission;

    public HasPermissionCondition() {}

    public HasPermissionCondition(String permission) {
        this.permission = permission;
    }

    @Override
    protected boolean isAllowed0(CastData cast) {
        LivingEntity target = cast.getTarget();
        if (target == null) return false;
        if (!(target instanceof Player)) return false;

        return target.hasPermission(permission);
    }

    @Override
    public String getKeyword() {
        return "HasPermission";
    }

    @NotNull
    @Override
    public Condition serialize(SerializeData data) throws SerializerException {
        String permission = data.of("Permission").assertExists().get();

        return applyParentArgs(data, new HasPermissionCondition(permission));
    }
}