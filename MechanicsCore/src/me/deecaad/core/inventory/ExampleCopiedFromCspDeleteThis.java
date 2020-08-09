package me.deecaad.core.inventory;

import me.deecaad.core.inventory.api.IButtonListener;
import me.deecaad.core.inventory.api.IWindow;
import me.deecaad.core.inventory.api.IWindowPageHolder;
import me.deecaad.core.inventory.api.Window;
import me.deecaad.core.inventory.api.WindowButton;
import me.deecaad.core.inventory.api.WindowPageHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExampleCopiedFromCspDeleteThis {

    public IWindowPageHolder getMainGUI() {
        //if (API.getS("Main_GUI.Name") == null) {
        //    return null;
        //}
        WindowPageHolder mainWindow = new WindowPageHolder();
        List<String> allWeapons = Arrays.asList("a", "b", "c");//allWeapons();
        int weaponsLeft = allWeapons.size();

        if (weaponsLeft > 45) {
            int fromWhere = 0;
            while (weaponsLeft > 0) {
                int size = decideGuiSizeBasedOnWeapons(weaponsLeft, true);
                int weaponsToIterate;
                if (weaponsLeft <= 45) {
                    weaponsToIterate = weaponsLeft;
                } else {
                    weaponsToIterate = size - 9;
                }
                createMainGuiWindow(mainWindow, size, allWeapons, weaponsToIterate, fromWhere, true, "Info_Item", "Main_GUI.Name");
                fromWhere += weaponsToIterate;
                weaponsLeft -= weaponsToIterate;
            }
        } else {
            int size = decideGuiSizeBasedOnWeapons(weaponsLeft, true);
            createMainGuiWindow(mainWindow, size, allWeapons, weaponsLeft, 0, false, "Info_Item", "Main_GUI.Name");
        }
        int i = 1;
        for (IWindow iwindow : mainWindow.getWindows()) {
            Window window = (Window) iwindow;
            String name = window.getName();
            name = name.replaceAll("#CURRENT_PAGE#", "" + i);
            name = name.replaceAll("#PAGES#", "" + mainWindow.getWindowsAmount());
            window.setName(name);
            ++i;
        }
        return mainWindow;
    }

    public void createMainGuiWindow(WindowPageHolder mainWindow, int size, List<String> allWeapons, int weaponsToIterate, int fromWhere, boolean pages, String infoItem, String guiNameMap) {
        Map<Integer, WindowButton> buttons = new HashMap<>();
        int i = 0;
        while (i < weaponsToIterate) {
            String weapon = allWeapons.get(fromWhere + i);
            ItemStack weaponStack = null;//API.cs().generateWeapon(weapon);
            //weaponStack = API.updateSkin(weapon, null, weaponStack, SkinType.NORMAL);
            if (weaponStack == null) {
                continue;
            }
            ItemMeta m = weaponStack.getItemMeta();
            m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON);
            m.setLore(null);
            weaponStack.setItemMeta(m);
            buttons.put(i, new WindowButton(weaponStack, new IButtonListener() {
                @Override
                public void onClick(IWindow window, InventoryClickEvent event) {
                    //API.getWeaponWindow(weapon).openWindow((Player) event.getWhoClicked(), 0);
                    //API.playSoundForPlayer((Player) event.getWhoClicked(), "Main_GUI.Click_Weapon_Sound");
                }
            }));
            ++i;
        }
        if (pages) {
            buttons.putAll(getPagesItems(mainWindow, size, infoItem, null, false));
        } else {
            buttons.putAll(getPagesItems(mainWindow, size, infoItem, null, true));
        }
        int b = 0;
        while (b < size) {
            if (buttons.get(b) == null) {
                //buttons.put(b, new WindowButton(API.getIS("Fill_Remaining_Slots_Item")));
            }
            ++b;
        }
        mainWindow.addWindows(new Window("name"/*API.getS(guiNameMap)*/, size, buttons, true, true, true));
    }

    public int decideGuiSizeBasedOnWeapons(int weaponsAmount, boolean pages) {
        if (pages) {
            if (weaponsAmount <= 9) {
                return 18;
            } else if (weaponsAmount <= 18) {
                return 27;
            } else if (weaponsAmount <= 27) {
                return 36;
            } else if (weaponsAmount <= 36) {
                return 45;
            } else {
                return 54;
            }
        }
        if (weaponsAmount <= 9) {
            return 9;
        } else if (weaponsAmount <= 18) {
            return 18;
        } else if (weaponsAmount <= 27) {
            return 27;
        } else if (weaponsAmount <= 36) {
            return 36;
        } else if (weaponsAmount <= 45) {
            return 45;
        } else {
            return 54;
        }
    }

    public Map<Integer, WindowButton> getPagesItems(IWindowPageHolder mainWindow, int size, String infoItem, WindowButton backToAll, boolean noNext) {
        Map<Integer, WindowButton> buttons = new HashMap<>();
        int lastRow = lastRow(size);
        int prevSlot = 3;
        int infoSlot = 4;
        int nextSlot = 5;

        for (int i : new int[]{1, 2, 6, 7, 8}) {
            buttons.put(lastRow + i, new WindowButton(null/*API.getIS("Page_Changing_Row_Fill_Item")*/));
        }

        if (backToAll != null) {
            buttons.put(lastRow + 0, backToAll);
        }

        buttons.put(lastRow + infoSlot, new WindowButton(null/*API.getIS(infoItem)*/));

        if (!noNext) {

            // Perhaps use PreviousButton and NextButton classes instead.
            // I added them right before I added this example.
            // Then its only basically
            // buttons.put(lastRow + nextSlot, new NextButton(mainWindow, itemStack)

            buttons.put(lastRow + nextSlot, new WindowButton(null/*API.getIS("Next_Page_Item")*/, new IButtonListener() {
                @Override
                public void onClick(IWindow window, InventoryClickEvent event) {
                    mainWindow.openNextWindow((Player) event.getWhoClicked());
                    //API.playSoundForPlayer((Player) event.getWhoClicked(), "Next_Page_Item.Click_Sound");
                }
            }));

            buttons.put(lastRow + prevSlot, new WindowButton(null/*API.getIS("Previous_Page_Item")*/, new IButtonListener() {
                @Override
                public void onClick(IWindow window, InventoryClickEvent event) {
                    mainWindow.openPreviousWindow((Player) event.getWhoClicked());
                    //API.playSoundForPlayer((Player) event.getWhoClicked(), "Previous_Page_Item.Click_Sound");
                }
            }));
        }

        return buttons;
    }

    public int lastRow(int size) {
        switch (size) {
            case 9:
                return 0;
            case 18:
                return 9;
            case 27:
                return 18;
            case 36:
                return 27;
            case 45:
                return 36;
            case 54:
                return 45;
            default:
                break;
        }
        return -1;
    }
}