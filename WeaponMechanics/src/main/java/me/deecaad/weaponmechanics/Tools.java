package me.deecaad.weaponmechanics;

import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static me.deecaad.core.utils.StringUtil.LOWER_ALPHABET;

public class Tools {

    private final Object a = "before a";
    private static final Object b = "before b";

    public static void main(String[] args) {
        //System.out.println(StringUtils.color("&#FFFFFF/&6test&#efefef&r"));
        //

        int MAXIMUM_CAPACITY = 1 << 30;
        int n = -1 >>> Integer.numberOfLeadingZeros(700 - 1);
        int cap = (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
        System.out.println(cap);

        if (true)
            return;

        String str = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~\u007fÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƑáíóúñÑªº¿®¬½¼¡«»";
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            MapFont.CharacterSprite sprite = MinecraftFont.Font.getChar(c);
            System.out.println(c + ": " + sprite.getWidth());
        }

        //calculateStats(0, 0.9, 0.05, 0.0, 0.8, 0);
    }

    private static void calculateStats(double accuracy, double damage, double range,
                                       double firerate, double mobility, double control) {
        // https://callofduty.fandom.com/wiki/Call_of_Duty:_Modern_Warfare_(2019)#Weapons
        // https://www.gamesatlas.com/cod-modern-warfare/weapons/
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
        System.out.println("-> Sniper rifle, rocket launcher, marksman rifle: 0");
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

    private static void fontData() {
        final String ALL_CHARS = " !\"#$%&'()*+,-./0123456789:;<=>?" +
                "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_" +
                "'abcdefghijklmnopqrstuvwxyz{|}~\u007F";

        for (int i = 0; i < ALL_CHARS.length(); i++) {
            char c = ALL_CHARS.charAt(i);

            System.out.println(c + ": " + MinecraftFont.Font.getChar(c).getWidth());
        }

        System.out.println();
        System.out.println("=============== /wm list (Page 1) ===============");
        System.out.println(MinecraftFont.Font.getWidth("================== WeaponMechanics =================="));
    }

    private static int countDifferentCharacters(LinkedList<Character> characters) {
        int[] table = mapToCharTable(characters.toString());
        int count = 0;
        for (int i : table)
            count++;

        return count;
    }

    private static int[] mapToCharTable(String str) {
        int[] table = new int[LOWER_ALPHABET.length()];
        for (int i = 0; i < str.length(); i++) {
            try {
                table[Character.toLowerCase(str.charAt(i)) - 97]++;
            } catch (ArrayIndexOutOfBoundsException ignore) {
                // Sometimes a string will contain something like an underscore.
                // We can safely ignore those characters and count the ones that
                // matter.
            }
        }
        return table;
    }
}
