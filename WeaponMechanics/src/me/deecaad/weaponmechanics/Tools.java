package me.deecaad.weaponmechanics;

import me.deecaad.core.utils.NumberUtils;
import me.deecaad.core.utils.ProbabilityMap;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.concurrent.ThreadLocalRandom;

public class Tools {
    
    public static void main(String[] args) {
        testProbabilityMap();
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

    public static void testProbabilityMap() {
        ProbabilityMap<String> map = new ProbabilityMap<>();
        final int max = 10000000;
        final int entries = 64 * 64;
        long time;
        double tookMillis;

        // Fill arrays;
        String[] objects = new String[entries];
        double[] chances = new double[entries];
        int[] occurrences = new int[entries];
        double totalChance = 0.0;

        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        int spaceToFill = (entries / chars.length) + 1;
        int count = 0;
        for (int i = 0; i < entries; i++) {
            StringBuilder builder = new StringBuilder(spaceToFill);
            while (builder.length() < spaceToFill) {
                builder.append(chars[Math.abs(count - i * chars.length) % chars.length]);
                count++;
            }

            //System.out.println("Generated id: " + builder);
            objects[i] = builder.toString();
            chances[i] = NumberUtils.getAsRounded(NumberUtils.minMax(1.0 / 255.0, Math.random(), 255.0), 2);

            totalChance += chances[i];
        }

        ThreadLocalRandom rand = ThreadLocalRandom.current();
        time = System.nanoTime();
        for (int i = 0; i < max; i++) {
            String random = null;

            while (random == null) {
                double rng = rand.nextDouble();
                int toCheck = rand.nextInt(objects.length);

                if (chances[toCheck] >= rng) {
                    occurrences[toCheck]++;
                    random = objects[toCheck];
                }
            }
        }
        tookMillis = (System.nanoTime() - time) / 1e9;

        System.out.println("Calculation of " + max + " random objects took " + tookMillis + " seconds");
        for (int i = 0; i < objects.length; i++) {
            //System.out.println(objects[i] + "(Expected: " + chances[i] / totalChance + ", Actual: " + (occurrences[i] / (double) max) + ")");
            occurrences[i] = 0;
        }

        for (int i = 0; i < objects.length; i++) {
            map.add(objects[i], chances[i]);
        }

        System.out.println();

        time = System.nanoTime();
        for (int i = 0; i < max; i++) {
            String get = map.get();

            for (int j = 0; j < objects.length; j++) {
                if (get.equals(objects[j])) {
                    occurrences[j]++;
                }
            }
        }
        tookMillis = (System.nanoTime() - time) / 1e9;

        System.out.println("Calculation of " + max + " random objects took " + tookMillis + " seconds");
        for (int i = 0; i < objects.length; i++) {
            //System.out.println(objects[i] + "(Expected: " + chances[i] / totalChance + ", Actual: " + (occurrences[i] / (double) max) + ")");
            occurrences[i] = 0;
        }
    }
}
