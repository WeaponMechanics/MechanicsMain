package me.deecaad.weaponmechanics;

import me.deecaad.core.utils.StringUtils;
import org.bukkit.entity.EntityType;

import java.awt.*;
import java.util.Arrays;

public class Tools {
    
    public static void main(String[] args) {
        System.out.println();
        //entityHitBox();
        splitTest();
        
        Color color = new Color(0x353833);
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
        String in = "ThisIsAPluginName";
        String[] split = StringUtils.splitCapitalLetters(in);
        System.out.println(Arrays.toString(split));
    }
}
