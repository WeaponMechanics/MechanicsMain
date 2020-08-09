package me.deecaad.weaponmechanics;

import me.deecaad.core.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class Tools {
    
    public static void main(String[] args) {
        System.out.println(StringUtils.didYouMean("endermen", Arrays.asList("enderdragon", "enderman", "endermite")));
    }
    
    public static void entityHitBox() {
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

    public static void blockDamageData() {
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
