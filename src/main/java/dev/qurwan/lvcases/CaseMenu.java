package dev.qurwan.lvcases;

import dev.qurwan.lvcases.db.Database;
import dev.qurwan.lvcases.util.Colorize;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class CaseMenu implements Listener {
    public static void open(Player p)
    {
        Inventory menu = Bukkit.createInventory(null
                , LvCases.getInstance().getConfig().getInt("menu.size") * 9
                , Colorize.a(LvCases.getInstance().getConfig().getString("menu.title")));
        LvCases.getInstance().getConfig().getConfigurationSection("cases").getKeys(false).forEach(c ->
                {
                    int count = Database.count(p.getName(), c);
                    ItemStack itemStack = new ItemStack(Material.CHEST, count);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(Colorize.a(LvCases.getInstance().getConfig().getString("cases." + c + ".display")));
                    itemMeta.setCustomModelData(LvCases.getInstance().getConfig().getInt("cases." + c + ".id"));
                    itemStack.setItemMeta(itemMeta);
                    menu.addItem(itemStack);
                }
        );
        p.openInventory(menu);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e)
    {
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (!e.getView().getTitle().equals(Colorize.a(LvCases.getInstance().getConfig().getString("menu.title")))) return;
        e.setCancelled(true);
        String name = ChatColor.stripColor( removeFormatting(e.getCurrentItem().getItemMeta().getDisplayName()));
        String CASE = "";
        for (String c : LvCases.getInstance().getConfig().getConfigurationSection("cases").getKeys(false))
        {
            String f = ChatColor.stripColor(removeFormatting(LvCases.getInstance().getConfig().getString("cases." + c + ".display")));
            if (
                    f.equals(name)
            ) CASE = c;
        }

        if (LvCases.getCase() != null)
        {
            p.sendMessage(Colorize.a(LvCases.getInstance().getConfig().getString("already-open")).replace("{player}", LvCases.getCase().getPlayer().getName()));
            return;
        }
        new Case(p, CASE, Animation.valueOf(LvCases.getInstance().getConfig().getString("cases." + CASE + ".open-type")));
        p.closeInventory();

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        Location c = new Location(
                Bukkit.getWorld(LvCases.getInstance().getConfig().getString("case-location").split(";")[3]),
                Double.parseDouble(LvCases.getInstance().getConfig().getString("case-location").split(";")[0]),
                Double.parseDouble(LvCases.getInstance().getConfig().getString("case-location").split(";")[1]),
                Double.parseDouble(LvCases.getInstance().getConfig().getString("case-location").split(";")[2])
        );
        if (e.getClickedBlock() != null && e.getClickedBlock().getLocation().equals(c))
        {
            if (LvCases.getCase() != null)
            {
                e.getPlayer().sendMessage(Colorize.a(LvCases.getInstance().getConfig().getString("already-open")).replace("{player}", LvCases.getCase().getPlayer().getName()));
                return;
            }
            open(e.getPlayer());
        }
    }

    public String removeFormatting(String input) {
        return input.replaceAll("&[a-zA-Z0-9]", "");
    }
}
