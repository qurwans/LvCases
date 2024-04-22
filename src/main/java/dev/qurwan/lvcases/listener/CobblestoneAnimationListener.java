package dev.qurwan.lvcases.listener;

import dev.qurwan.lvcases.Animation;
import dev.qurwan.lvcases.LvCases;
import dev.qurwan.lvcases.Status;
import dev.qurwan.lvcases.util.Colorize;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

public class CobblestoneAnimationListener implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        if (e.getClickedBlock() == null) return;
        Player p = e.getPlayer();
        if (LvCases.getCase() == null ||
                !LvCases.getCase().getAnimation().equals(Animation.COBBLESTONE) ||
                !LvCases.getCase().getPlayer().equals(p) ||
                e.getClickedBlock() == null
        ) return;
        Block block = e.getClickedBlock();
        if (!block.getState().hasMetadata("lvcase")) return;
        e.setCancelled(true);
        if (LvCases.getCase().getStatus().equals(Status.CHOOSED)) return;
        String win = getRandomWin(LvCases.getCase().getType());
        Material material = Material.valueOf(LvCases.getInstance().getConfig().getString("cases." + LvCases.getCase().getType() + ".default-item"));

        LvCases.getCase().setStatus(Status.CHOOSED);

        Bukkit.getScheduler().runTaskLater(LvCases.getInstance(), () -> {
            LvCases.getCase().getLocations().keySet().forEach(integer -> {
                Block b = LvCases.getCase().getLocations().get(integer).getBlock();
                b.breakNaturally(new ItemStack(Material.AIR));
                b.getLocation().getNearbyPlayers(15).forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1));
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
        }, 7 * 20);
        Set<String> cmds = new HashSet<>();
        Bukkit.getScheduler().runTaskLater(LvCases.getInstance(), () -> {
            LvCases.getCase().destroy();
            cmds.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        }, 15 * 20);

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
        Location l = e.getClickedBlock().getLocation();

        Bukkit.getScheduler().runTaskLater(LvCases.getInstance(), () -> {
            ArmorStand sword = (ArmorStand) block.getLocation().getWorld().spawnEntity(add(l, 1, 0.1, 1.2), EntityType.ARMOR_STAND);
            sword.setVisible(false);
            sword.setGravity(false);
            sword.setRightArmPose(new EulerAngle(Math.toRadians(90), 0, 0));
            Bukkit.getScheduler().runTaskLater(LvCases.getInstance(), sword::remove, 2 * 20);
            l.getNearbyPlayers(15).forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1));
            ItemStack stoneSword = new ItemStack(Material.IRON_SWORD);
            ItemMeta stoneSwordMeta = stoneSword.getItemMeta();
            stoneSword.setItemMeta(stoneSwordMeta);
            sword.getEquipment().setItemInMainHand(stoneSword);
        }, 2 * 20);

        ArmorStand sword = (ArmorStand) block.getLocation().getWorld().spawnEntity(add(l, 1, 0.3, 1.2), EntityType.ARMOR_STAND);
        sword.setVisible(false);
        sword.setGravity(false);
        sword.setRightArmPose(new EulerAngle(Math.toRadians(90), 0, 0));
        Bukkit.getScheduler().runTaskLater(LvCases.getInstance(), sword::remove, 2 * 20);
        ItemStack stoneSword = new ItemStack(Material.IRON_SWORD);
        ItemMeta stoneSwordMeta = stoneSword.getItemMeta();
        stoneSword.setItemMeta(stoneSwordMeta);
        sword.getEquipment().setItemInMainHand(stoneSword);
        l.getNearbyPlayers(15).forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1));

        Material finalMaterial = material;
        Bukkit.getScheduler().runTaskLater(LvCases.getInstance(), () -> {
            e.getClickedBlock().breakNaturally(new ItemStack(Material.AIR));
            l.getNearbyPlayers(15).forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1));
            ItemStack itemStack = new ItemStack(finalMaterial);
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
        }, 4 * 20);


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
