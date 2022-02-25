package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.MainCommand;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.commands.testcommands.TestCommand;

public class WeaponMechanicsMainCommand extends MainCommand {

    public WeaponMechanicsMainCommand() {
        super("weaponmechanics", "weaponmechanics.admin");
        setDescription("WeaponMechanics main command");
        setAliases(StringUtil.getList("wea", "weapon", "wm"));

        commands.register(new TestCommand());
        commands.register(new LegacyGiveCommand());
        commands.register(new LegacyInfoCommand());
        commands.register(new LegacyReloadCommand());
        commands.register(new LegacyWikiCommand());
        commands.register(new LegacyListWeaponsCommand());
        commands.register(new CrackShotConvertCommand());
    }
}
