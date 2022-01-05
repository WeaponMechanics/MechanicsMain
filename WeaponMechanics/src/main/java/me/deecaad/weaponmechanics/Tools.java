package me.deecaad.weaponmechanics;

import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;

public class Tools {

    private final Object a = "before a";
    private static final Object b = "before b";

    public static void main(String[] args) {
        //System.out.println(StringUtils.color("&#FFFFFF/&6test&#efefef&r"));
        //

        calculateStats(0.54, 0.65, 0.52, 0.53, 0.77, 0.6);
    }

    private static void calculateStats(double accuracy, double damage, double range,
                                       double firerate, double mobility, double control) {
        System.out.println("Accuracy: ");
        System.out.println("-> spread: " + NumberUtil.lerp(12.5, 0, accuracy));
        System.out.println("-> spread image FOV: " + (30 * NumberUtil.lerp(1.25, 0, accuracy)));
        System.out.println("Damage:");
        System.out.println("-> generally: " + NumberUtil.lerp(-5, 15, damage));
        System.out.println("-> armor: " + Math.round(NumberUtil.lerp(-2, 5, damage)));
        System.out.println("Range (damage drop off): ");
        double damageDropOff = NumberUtil.lerp(-3, -1, range);
        System.out.println("-> Default: " + "10: " + damageDropOff + " 25: " + (damageDropOff*2) + " 50: " + (damageDropOff*3));
        System.out.println("-> Shotgun: " + "5: " + damageDropOff + " 10: " + (damageDropOff*2.5) + " 20: " + (damageDropOff*4));
        System.out.println("-> Sniper rifle: 0");
        System.out.println("Fire rate: ");
        System.out.println("-> full/semi auto: " + NumberUtil.lerp(-5, 20, firerate));
        System.out.println("-> firearm actions/delay between shots: " + NumberUtil.lerp(20, -5, firerate));
        System.out.println("Mobility:");
        System.out.println("-> movement attribute: " + NumberUtil.lerp(-0.08, 0.025, mobility));
        System.out.println("-> reload duration: " + Math.round(NumberUtil.lerp(140, 0, mobility)));
        System.out.println("Control: ");
        System.out.println("-> Full auto: " + NumberUtil.lerp(10, 0, control));
        System.out.println("-> Slow firing: " + NumberUtil.lerp(15, 5, control));
    }

    private static void reflectionsTest() {
        Tools tools = new Tools();
        Field aField = ReflectionUtil.getField(Tools.class, "a");
        System.out.println(ReflectionUtil.invokeField(aField, tools));
        ReflectionUtil.setField(aField, tools, "After a");
        System.out.println(ReflectionUtil.invokeField(aField, tools));

        Field bField = ReflectionUtil.getField(Tools.class, "b");
        System.out.println(ReflectionUtil.invokeField(bField, null));
        ReflectionUtil.setField(bField, null, "After a");
        System.out.println(ReflectionUtil.invokeField(aField, null));
    }
    
    private static void entityHitBox() {
        EntityType[] types = EntityType.values();
        
        System.out.println("Entity_Hitboxes:");
        for (EntityType type : types) {
            if (!type.isAlive()) continue; // If it can be a livingEntity, I think
            System.out.println("  " + type.name() + ":");
            System.out.println("    " + "Horizontal_Entity: false");
            System.out.println("    " + "HEAD: 0.0");
            System.out.println("    " + "BODY: 0.0");
            System.out.println("    " + "ARMS: true");
            System.out.println("    " + "LEGS: 0.0");
            System.out.println("    " + "FEET: 0.0");
        }
    }

    private static void blockDamageData() {
        System.out.println("        Block_List:");
        for (Material mat : Material.values()) {
            if (mat.isLegacy() || !mat.isBlock() || mat.isAir()) continue;

            int durability = (int) (mat.getBlastResistance() + mat.getHardness()) + 1;
            if (durability > 18) {
                continue;
            }

            System.out.println("          - " + mat.name().toLowerCase() + "~" + durability);
        }
    }
}
