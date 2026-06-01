package me.deecaad.weaponmechanics.weapon.scope;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import me.deecaad.core.file.*;
import me.deecaad.core.file.simple.DoubleSerializer;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.MechanicManager;
import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.placeholder.PlaceholderMessage;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerListener;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponScopeEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.ZoomData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.VRAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SearcherFilter(SearchMode.ON_DEMAND)
public class ScopeHandler implements IValidator, TriggerListener {

    private static final Pattern COMPACT_ATTRIBUTE = Pattern.compile("^(.+?)([-+]\\d+(?:\\.\\d+)?)$");

    private WeaponHandler weaponHandler;

    /**
     * Defualt constructor for validator
     */
    public ScopeHandler() {
    }

    public ScopeHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    @Override
    public boolean allowOtherTriggers() {
        return false;
    }

    @Override
    public boolean tryUse(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield, @Nullable LivingEntity victim) {
        Configuration config = WeaponMechanics.getInstance().getWeaponConfigurations();

        if (Bukkit.getPluginManager().getPlugin("Vivecraft_Spigot_Extensions") != null
            && entityWrapper.isPlayer() && VRAPI.instance().isVRPlayer((Player) entityWrapper.getEntity())) {
            // Don't try to use scope this way when player is in VR
            return false;
        }

        ZoomData zoomData;
        // Only allow using zoom at one hand at time
        if (slot == EquipmentSlot.HAND) {
            if (entityWrapper.getOffHandData().getZoomData().isZooming()) {
                return false;
            }
            zoomData = entityWrapper.getMainHandData().getZoomData();
        } else {
            if (entityWrapper.getMainHandData().getZoomData().isZooming()) {
                return false;
            }
            zoomData = entityWrapper.getOffHandData().getZoomData();
        }

        Trigger trigger = config.getObject(weaponTitle + ".Scope.Trigger", Trigger.class);
        if (trigger == null)
            return false;

        LivingEntity shooter = entityWrapper.getEntity();

        // Handle permissions
        boolean hasPermission = weaponHandler.getInfoHandler().hasPermission(shooter, weaponTitle);

        // Check if entity is already zooming
        if (zoomData.isZooming()) {

            Trigger offTrigger = config.getObject(weaponTitle + ".Scope.Zoom_Off.Trigger", Trigger.class);
            // If off trigger is valid -> zoom out even if stacking hasn't reached maximum stacks
            if (offTrigger != null && offTrigger.check(triggerType, slot, entityWrapper)) {
                return zoomOut(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
            }

            // If trigger is valid zoom in or out depending on situation
            if (trigger.check(triggerType, slot, entityWrapper)) {

                // Handle permissions
                if (!hasPermission) {
                    if (shooter instanceof Player player) {
                        PlaceholderMessage permissionMessage = new PlaceholderMessage(WeaponMechanics.getInstance().getConfiguration().getString("Messages.Permissions.Use_Weapon", ChatColor.RED
                            + "You do not have permission to use " + weaponTitle));
                        Component component = permissionMessage.replaceAndDeserialize(PlaceholderData.of(player, weaponStack, weaponTitle, slot));
                        player.sendMessage(component);
                    }
                    return false;
                }

                List<?> zoomStacks = config.getObject(weaponTitle + ".Scope.Zoom_Stacking.Stacks", List.class);
                if (zoomStacks == null) { // meaning that zoom stacking is not used
                    // Should turn off
                    return zoomOut(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
                }

                // E.g. when there is 2 defined values in stacks:
                // 0 < 2 // TRUE
                // 1 < 2 // TRUE
                // 2 < 2 // FALSE

                if (zoomData.getZoomStacks() < zoomStacks.size()) { // meaning that zoom stacks have NOT reached maximum stacks
                    // Should not turn off and stack instead
                    return zoomIn(weaponStack, weaponTitle, entityWrapper, zoomData, slot); // Zoom in handles stacking on its own
                }
                // Should turn off (because zoom stacks have reached maximum stacks)
                return zoomOut(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
            }
        } else if (trigger.check(triggerType, slot, entityWrapper)) {

            // Handle permissions
            if (!hasPermission) {
                if (shooter instanceof Player player) {
                    PlaceholderMessage permissionMessage = new PlaceholderMessage(WeaponMechanics.getInstance().getConfiguration().getString("Messages.Permissions.Use_Weapon", ChatColor.RED
                        + "You do not have permission to use " + weaponTitle));
                    Component component = permissionMessage.replaceAndDeserialize(PlaceholderData.of(player, weaponStack, weaponTitle, slot));
                    player.sendMessage(component);
                }
                return false;
            }

            // Try zooming in since entity is not zooming
            return zoomIn(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
        }
        return false;
    }

    /**
     * @return true if successfully zoomed in or stacked
     */
    private boolean zoomIn(ItemStack weaponStack, String weaponTitle, EntityWrapper entityWrapper, ZoomData zoomData, EquipmentSlot slot) {
        Configuration config = WeaponMechanics.getInstance().getWeaponConfigurations();
        LivingEntity entity = entityWrapper.getEntity();

        // If the entity is on shoot cooldown and the weapon uses Unscope_After_Shot,
        // then the entity is not allowed to enter scope until the cooldown is over.
        boolean unscopeAfterShot = config.getBoolean(weaponTitle + ".Scope.Unscope_After_Shot");
        int delayBetweenShots = config.getInt(weaponTitle + ".Shoot.Delay_Between_Shots");
        if (unscopeAfterShot && delayBetweenShots != 0 && !NumberUtil.hasMillisPassed(zoomData.getHandData().getLastShotTime(), delayBetweenShots)) {
            return false;
        }

        if (zoomData.isZooming()) { // zoom stack

            List<?> zoomStacks = config.getObject(weaponTitle + ".Scope.Zoom_Stacking.Stacks", List.class);
            if (zoomStacks != null) {
                int currentStacks = zoomData.getZoomStacks();
                double zoomAmount = Double.parseDouble(zoomStacks.get(currentStacks).toString());
                int zoomStack = currentStacks + 1;
                MechanicManager zoomStackingMechanics = config.getObject(weaponTitle + ".Scope.Zoom_Stacking.Mechanics", MechanicManager.class);

                WeaponScopeEvent weaponScopeEvent = new WeaponScopeEvent(weaponTitle, weaponStack, entity, slot, WeaponScopeEvent.ScopeType.STACK, zoomAmount, zoomStack, zoomStackingMechanics);
                Bukkit.getPluginManager().callEvent(weaponScopeEvent);
                if (weaponScopeEvent.isCancelled()) {
                    return false;
                }

                zoomData.setScopeData(weaponTitle, weaponStack);

                updateZoom(entityWrapper, zoomData, weaponScopeEvent.getZoomAmount());
                zoomData.setZoomStacks(zoomStack);

                weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);
                useNightVision(entityWrapper, zoomData, weaponScopeEvent.isNightVision());

                if (weaponScopeEvent.getMechanics() != null)
                    weaponScopeEvent.getMechanics().use(new CastData(entity, weaponTitle, weaponStack));

                return true;
            } else {
                WeaponMechanics.getInstance().getDebugger().warning("For some reason zoom in was called on entity when it shouldn't have.",
                    "Entity was already zooming so it should have stacked zoom, but now zoom stacking wasn't used at all?",
                    "Ignoring this call, but this shouldn't even happen...",
                    "Are you sure you have defined both Zoom_Stacking.Stacks for weapon " + weaponTitle + "?");
                return false;
            }
        }

        double zoomAmount = config.getDouble(weaponTitle + ".Scope.Zoom_Amount");
        if (zoomAmount == 0)
            return false;

        MechanicManager scopeMechanics = config.getObject(weaponTitle + ".Scope.Mechanics", MechanicManager.class);

        // zoom stack = 0, because its not used OR this is first zoom in
        WeaponScopeEvent weaponScopeEvent = new WeaponScopeEvent(weaponTitle, weaponStack, entity, slot, WeaponScopeEvent.ScopeType.IN, zoomAmount, 0, scopeMechanics);
        Bukkit.getPluginManager().callEvent(weaponScopeEvent);
        if (weaponScopeEvent.isCancelled()) {
            return false;
        }

        zoomData.setScopeData(weaponTitle, weaponStack);
        updateZoom(entityWrapper, zoomData, weaponScopeEvent.getZoomAmount());
        applyScopeAttributes(entityWrapper, zoomData, weaponTitle);

        if (weaponScopeEvent.getMechanics() != null)
            weaponScopeEvent.getMechanics().use(new CastData(entity, weaponTitle, weaponStack));

        weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);
        useNightVision(entityWrapper, zoomData, weaponScopeEvent.isNightVision());

        HandData handData = slot == EquipmentSlot.HAND ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();
        handData.setLastScopeTime(System.currentTimeMillis());

        return true;
    }

    /**
     * @return true if successfully zoomed out
     */
    private boolean zoomOut(ItemStack weaponStack, String weaponTitle, EntityWrapper entityWrapper, ZoomData zoomData, EquipmentSlot slot) {

        if (!zoomData.isZooming())
            return false;
        LivingEntity entity = entityWrapper.getEntity();

        MechanicManager zoomOffMechanics = WeaponMechanics.getInstance().getWeaponConfigurations().getObject(weaponTitle + ".Scope.Zoom_Off.Mechanics", MechanicManager.class);

        // Zoom amount and stack 0 because zooming out
        WeaponScopeEvent weaponScopeEvent = new WeaponScopeEvent(weaponTitle, weaponStack, entity, slot, WeaponScopeEvent.ScopeType.OUT, 0, 0, zoomOffMechanics);
        Bukkit.getPluginManager().callEvent(weaponScopeEvent);
        if (weaponScopeEvent.isCancelled()) {
            return false;
        }

        zoomData.setScopeData(null, null);

        removeScopeAttributes(entityWrapper, zoomData);
        updateZoom(entityWrapper, zoomData, weaponScopeEvent.getZoomAmount());
        zoomData.setZoomStacks(0);

        if (weaponScopeEvent.getMechanics() != null)
            weaponScopeEvent.getMechanics().use(new CastData(entity, weaponTitle, weaponStack));

        weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);
        useNightVision(entityWrapper, zoomData, false);

        return true;
    }

    private void applyScopeAttributes(EntityWrapper entityWrapper, ZoomData zoomData, String weaponTitle) {
        removeScopeAttributes(entityWrapper, zoomData);

        Configuration config = WeaponMechanics.getInstance().getWeaponConfigurations();
        List<?> attributes = config.getObject(weaponTitle + ".Scope.Attributes", List.class);
        if (attributes == null || attributes.isEmpty())
            return;

        LivingEntity entity = entityWrapper.getEntity();
        for (int i = 0; i < attributes.size(); i++) {
            ScopeAttribute scopeAttribute = parseScopeAttribute(attributes.get(i).toString());
            if (scopeAttribute == null)
                continue;

            AttributeInstance instance = entity.getAttribute(scopeAttribute.attribute());
            if (instance == null)
                continue;

            NamespacedKey key = new NamespacedKey(WeaponMechanics.getInstance(), "scope_" + sanitize(weaponTitle) + "_" + i);
            removeModifier(instance, key);

            AttributeModifier modifier = new AttributeModifier(key, scopeAttribute.amount(), scopeAttribute.operation());
            instance.addTransientModifier(modifier);
            zoomData.addScopeAttributeModifier(scopeAttribute.attribute(), modifier);
        }
    }

    public void removeScopeAttributes(EntityWrapper entityWrapper, ZoomData zoomData) {
        if (zoomData.getScopeAttributeModifiers().isEmpty())
            return;

        LivingEntity entity = entityWrapper.getEntity();
        for (Map.Entry<Attribute, List<AttributeModifier>> entry : zoomData.getScopeAttributeModifiers().entrySet()) {
            AttributeInstance instance = entity.getAttribute(entry.getKey());
            if (instance != null) {
                for (AttributeModifier modifier : entry.getValue()) {
                    instance.removeModifier(modifier);
                }
            }
        }
        zoomData.clearScopeAttributeModifiers();
    }

    private void removeModifier(AttributeInstance instance, NamespacedKey key) {
        for (AttributeModifier modifier : new ArrayList<>(instance.getModifiers())) {
            if (modifier.getKey().equals(key))
                instance.removeModifier(modifier);
        }
    }

    /**
     * Updates the zoom amount of entity.
     */
    public void updateZoom(EntityWrapper entityWrapper, ZoomData zoomData, double newZoomAmount) {
        if (entityWrapper.getEntity().getType() != EntityType.PLAYER) {
            // Not player so no need for FOV changes
            zoomData.setZoomAmount(newZoomAmount);
            return;
        }

        Player player = (Player) entityWrapper.getEntity();

        zoomData.setZoomAmount(newZoomAmount);

        // Update abilities sets the FOV change
        WrapperPlayServerPlayerAbilities abilities = new WrapperPlayServerPlayerAbilities(
            player.isInvulnerable(),
            player.isFlying(),
            player.getAllowFlight(),
            player.getGameMode() == GameMode.CREATIVE,
            player.getFlySpeed() / 2,
            player.getWalkSpeed() / 2 // divide by 2, since spigot multiplies this by 2
        );

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, abilities);
    }

    /**
     * Toggles night vision on or off whether it was on before
     */
    public void useNightVision(EntityWrapper entityWrapper, ZoomData zoomData, boolean isEnable) {
        if (entityWrapper.getEntity().getType() != EntityType.PLAYER) {
            // Not player so no need for night vision
            return;
        }
        Player player = (Player) entityWrapper.getEntity();

        if (isEnable) {
            // Already on
            if (zoomData.hasZoomNightVision())
                return;

            zoomData.setZoomNightVision(true);
            WrapperPlayServerEntityEffect entityEffect = new WrapperPlayServerEntityEffect(
                player.getEntityId(),
                PotionTypes.NIGHT_VISION,
                2,
                -1, // infinite duration
                (byte) 0);

            entityEffect.setVisible(false);
            entityEffect.setShowIcon(false);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, entityEffect);
        }

        else {
            // Already off
            if (!zoomData.hasZoomNightVision())
                return;

            zoomData.setZoomNightVision(false);

            // Remove the fake night vision effect from the player
            WrapperPlayServerRemoveEntityEffect removeEntityEffect = new WrapperPlayServerRemoveEntityEffect(
                player.getEntityId(),
                PotionTypes.NIGHT_VISION);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, removeEntityEffect);

            // If the player has night vision effect from other source, show it to them again
            PotionEffect nightVision = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
            if (nightVision != null) {
                WrapperPlayServerEntityEffect entityEffect = new WrapperPlayServerEntityEffect(
                    player.getEntityId(),
                    PotionTypes.NIGHT_VISION,
                    nightVision.getAmplifier(),
                    nightVision.getDuration(),
                    (byte) 0);
                entityEffect.setVisible(nightVision.hasParticles());
                entityEffect.setShowIcon(nightVision.hasIcon());
                entityEffect.setAmbient(nightVision.isAmbient());
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, entityEffect);
            }
        }
    }

    @Override
    public String getKeyword() {
        return "Scope";
    }

    public List<String> getAllowedPaths() {
        return Collections.singletonList(".Scope");
    }

    @Override
    public void validate(Configuration configuration, SerializeData data) throws SerializerException {
        data.of("Trigger").assertExists();

        double zoomAmount = data.of("Zoom_Amount").assertExists().assertRange(1, 10).getDouble().getAsDouble();
        List<List<Optional<Object>>> splitStacksList = data.ofList("Zoom_Stacking.Stacks")
            .addArgument(new DoubleSerializer(1.0, 10.0))
            .requireAllPreviousArgs()
            .assertList();

        int shootDelayAfterScope = data.of("Shoot_Delay_After_Scope").getInt().orElse(0);
        if (shootDelayAfterScope != 0) {
            // Convert to millis
            configuration.set(data.getKey() + ".Shoot_Delay_After_Scope", shootDelayAfterScope * 50);
        }

        List<?> attributes = data.of("Attributes").get(List.class).orElse(List.of());
        for (Object rawAttribute : attributes) {
            if (parseScopeAttribute(rawAttribute.toString()) == null) {
                throw data.exception("Attributes",
                    "Expected attributes in the format '<attribute> <amount>'",
                    "For example, 'movement_speed -0.1' or 'GENERIC_MOVEMENT_SPEED--0.1'");
            }
        }
    }

    private ScopeAttribute parseScopeAttribute(String raw) {
        if (raw == null || raw.isBlank())
            return null;

        String[] split = raw.trim().split("\\s+");
        String attributeName;
        String amountText;
        AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_NUMBER;

        if (split.length >= 2) {
            attributeName = split[0];
            amountText = split[1];
            if (split.length >= 3) {
                operation = parseOperation(split[2]);
                if (operation == null)
                    return null;
            }
        } else {
            int negativeSeparator = raw.lastIndexOf("--");
            if (negativeSeparator > 0) {
                attributeName = raw.substring(0, negativeSeparator);
                amountText = "-" + raw.substring(negativeSeparator + 2);
            } else {
                Matcher matcher = COMPACT_ATTRIBUTE.matcher(raw.trim());
                if (!matcher.matches())
                    return null;

                attributeName = matcher.group(1);
                amountText = matcher.group(2);
            }
        }

        Attribute attribute = parseAttribute(attributeName);
        if (attribute == null)
            return null;

        try {
            return new ScopeAttribute(attribute, Double.parseDouble(amountText), operation);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Attribute parseAttribute(String attributeName) {
        String normalized = attributeName.toLowerCase(Locale.ROOT)
            .replace("minecraft:", "")
            .replace("generic_", "")
            .replace("generic.", "");

        Attribute attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(normalized));
        if (attribute != null)
            return attribute;

        return null;
    }

    private AttributeModifier.Operation parseOperation(String operation) {
        return switch (operation.toUpperCase(Locale.ROOT)) {
            case "ADD", "ADD_NUMBER" -> AttributeModifier.Operation.ADD_NUMBER;
            case "ADD_SCALAR", "ADD_PERCENTAGE" -> AttributeModifier.Operation.ADD_SCALAR;
            case "MULTIPLY", "MULTIPLY_SCALAR_1", "MULTIPLY_PERCENTAGE" -> AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            default -> null;
        };
    }

    private String sanitize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._/-]", "_");
    }

    private record ScopeAttribute(Attribute attribute, double amount, AttributeModifier.Operation operation) {
    }
}
