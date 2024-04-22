package dev.qurwan.lvcases.listener;

import dev.qurwan.lvcases.Animation;
import dev.qurwan.lvcases.LvCases;
import dev.qurwan.lvcases.Status;
import dev.qurwan.lvcases.util.Colorize;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.*;

public class ChestAnimationListener implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        if (e.getClickedBlock() == null || !e.getClickedBlock().getType().equals(Material.CHEST)) return;
        Player p = e.getPlayer();
        Block block = e.getClickedBlock();
        Chest chest = (Chest) block.getState();
        if (!chest.getCustomName().startsWith("lvcase:")) return;

        e.setCancelled(true);
        if (LvCases.getCase() == null ||
                !LvCases.getCase().getAnimation().equals(Animation.CHEST) ||
                !LvCases.getCase().getPlayer().equals(p) ||
                e.getClickedBlock() == null
        ) return;
        if (LvCases.getCase().getStatus().equals(Status.CHOOSED)) return;

        String win = getRandomWin(LvCases.getCase().getType());
        Material material = Material.valueOf(LvCases.getInstance().getConfig().getString("cases." + LvCases.getCase().getType() + ".default-item"));

        LvCases.getCase().setStatus(Status.CHOOSED);
        chest.open();
        chest.update();

        Bukkit.getScheduler().runTaskLater(LvCases.getInstance(), () -> {
            LvCases.getCase().getLocations().keySet().forEach(integer -> {
                Block b = LvCases.getCase().getLocations().get(integer).getBlock();
                Chest c = (Chest) b.getState();
                c.open();
                c.update();

                if (integer == 1) rotate(b, BlockFace.EAST);
                if (integer == 2) rotate(b, BlockFace.EAST);
                if (integer == 3) rotate(b, BlockFace.SOUTH);
                if (integer == 4) rotate(b, BlockFace.SOUTH);
                if (integer == 5) rotate(b, BlockFace.WEST);
                if (integer == 6) rotate(b, BlockFace.WEST);
                if (integer == 7) rotate(b, BlockFace.NORTH);
                if (integer == 8) rotate(b, BlockFace.NORTH);
            });
            for (int i : LvCases.getCase().getLocations().keySet()) {
                Location otherChest = LvCases.getCase().getLocations().get(i);
                String otherWin = getRandomWin(LvCases.getCase().getType());
                if (!otherChest.equals(e.getClickedBlock().getLocation()))
                {
                    ItemStack itemStack = new ItemStack(Material.valueOf(LvCases.getInstance().getConfig().getString("cases." + LvCases.getCase().getType() + ".default-item")));
                    for (String action : LvCases.getInstance().getConfig().getStringList("cases." + LvCases.getCase().getType() + ".drops." + otherWin))
                    {
                        if (action.startsWith("[material] ")) itemStack.setType(Material.valueOf(action.replace("[material] ", "")));
                    }
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(UUID.randomUUID().toString());
                    itemStack.setItemMeta(itemMeta);
                    Entity itemEntity = e.getClickedBlock().getWorld().dropItem(add(otherChest, 0.5, 2.2, 0.5), itemStack);
                    Item item = (Item) itemEntity;
                    item.setCustomName(Colorize.a(otherWin));
                    item.setCustomNameVisible(true);
                    item.setVelocity(new Vector(0, 0, 0));
                    item.setCanMobPickup(false);
                    item.setCanPlayerPickup(false);
                    LvCases.getCase().getDrops().add(item);
                }
            }
        }, 5 * 20);
        Set<String> cmds = new HashSet<>();
        Bukkit.getScheduler().runTaskLater(LvCases.getInstance(), () -> {
            LvCases.getCase().destroy();
            cmds.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        }, 10 * 20);

        for (String string : LvCases.getInstance().getConfig().getStringList("cases." + LvCases.getCase().getType() + ".drops." + win))
        {
            String arg = "";
            if (string.startsWith("[command] "))
            {
                arg = string.replace("[command] ", "").replace("{player}", p.getName());
                cmds.add(arg);
            }
            if (string.startsWith("[item] "))
            {
                arg = string.replace("[item] ", "");
                material = Material.valueOf(arg);
            }
            if (string.startsWith("[permission]"))
            {
                arg = string.replace("[permission] ", "");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + p.getName() + " permission set " + arg);
            }
        }
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(UUID.randomUUID().toString());
        itemStack.setItemMeta(itemMeta);
        Entity itemEntity = e.getClickedBlock().getWorld().dropItem(add(e.getClickedBlock().getLocation(), 0.5, 2.2, 0.5), itemStack);
        Item item = (Item) itemEntity;
        item.setCustomName(Colorize.a(win));
        item.setCustomNameVisible(true);
        item.setVelocity(new Vector(0, 0, 0));
        item.setCanMobPickup(false);
        item.setCanPlayerPickup(false);
        LvCases.getCase().getDrops().add(item);
    }



    private void rotate(Block block, BlockFace direction) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Directional) {
            Directional directionalData = (Directional) blockData;
            directionalData.setFacing(direction);
            block.setBlockData(directionalData);
        }
    }

    private String getRandomWin(String c) {
        ConfigurationSection dropsSection = LvCases.getInstance().getConfig().getConfigurationSection("cases." + c + ".drops");
        if (dropsSection != null) {
            List<String> keys = new ArrayList<>(dropsSection.getKeys(false));

            if (!keys.isEmpty()) {
                Random random = new Random();
                return keys.get(random.nextInt(keys.size()));
            }
        }
        return null;
    }

    private Location add(Location loc, double x, double y, double z)
    {
        return new Location(
                loc.getWorld(), loc.getX() + x, loc.getY() + y, loc.getZ() + z
        );
    }

}
