package me.deecaad.compatibility.vault;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface IVaultCompatibility {

    /**
     * Gets the balance of the player. If the player does not
     * yet have an account, a new one is created. If vault
     * is not installed, this method will return 0. Getting
     * 0 does not mean that vault is not installed!
     *
     * @param player The player to get the balance of
     * @return The balance of the player
     */
    double getBalance(OfflinePlayer player);

    /**
     * Sets the balance of a player utilizing withdraw and
     * deposit methods. If the player does not yet have an
     * account, a new one is created.
     *
     * @param player  The player to set the balance of
     * @param balance The balance to set
     */
    void setBalance(OfflinePlayer player, double balance);

    /**
     * Determines if the player has <code>amount</code> balance
     * in their account. If the player does not yet have an
     * account, this method will return false (No account is
     * created)
     *
     * @param player The player to check
     * @param amount The amount of money to check
     * @return If the player has enough money
     */
    boolean hasBalance(OfflinePlayer player, double amount);

    /**
     * Withdraws the given amount of money from the player.
     * If the player does not have an account, a new one is
     * created.
     *
     * @param player The player to take from
     * @param amount The amount of money to take
     */
    void withdrawBalance(OfflinePlayer player, double amount);

    /**
     * Gives the given amount of money to the player. If the
     * player does not have an account, a new one is created.
     *
     * @param player The player to give money to
     * @param amount The amount of money to deposit
     */
    void depositBalance(OfflinePlayer player, double amount);

    /**
     * Gets the prefix of the given player
     *
     * @param player The player to get the prefix from
     * @return The prefix of the player
     */
    String getPrefix(Player player);

    /**
     * Gets the suffix of the given player
     *
     * @param player The player to get the suffix from
     * @return The suffix of the player
     */
    String getSuffix(Player player);

    /**
     * Gets all of the registered groups from the permissions
     * plugin. Useful for determining if player input for group
     * is valid.
     *
     * @return All registered groups
     */
    String[] getGroups();

    /**
     * Gets all groups that the given player has.
     *
     * @param player The player to pull groups from
     * @return The groups the player is in
     */
    String[] getGroups(Player player);

    /**
     * Gets the primary/top group from the given player
     *
     * @param player The player to pull from
     * @return The primary group of the player
     */
    String getGroup(Player player);

}