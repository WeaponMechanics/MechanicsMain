package me.deecaad.weaponmechanics;

import me.deecaad.core.utils.StringUtils;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class Tools {
    
    public static void main(String[] args) {
        splitTest();
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
    
    public static void splitTest() {
        StringBuilder builder = new StringBuilder();

        builder.append("AK-47.Damage.Head.");
        int length = builder.length();

        builder.append("Test");
        builder.setLength(length);
        builder.append("Test2");
    }

}
