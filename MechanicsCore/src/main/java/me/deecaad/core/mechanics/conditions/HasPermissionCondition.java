package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasPermissionCondition extends Condition {

    private String permission;

    /**
     * Default constructor for serializer.
     */
    public HasPermissionCondition() {
    }

    public HasPermissionCondition(String permission) {
        this.permission = permission;
    }

    @Override
    protected boolean isAllowed0(CastData cast) {
        LivingEntity target = cast.getTarget();
        if (target == null)
            return false;

        return target.hasPermission(permission);
    }

    @Override
    public String getKeyword() {
        return "HasPermission";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/conditions/has-permission";
    }

    @NotNull
    @Override
    public Condition serialize(@NotNull SerializeData data) throws SerializerException {
        String permission = data.of("Permission").assertExists().get();

        return applyParentArgs(data, new HasPermissionCondition(permission));
    }
}