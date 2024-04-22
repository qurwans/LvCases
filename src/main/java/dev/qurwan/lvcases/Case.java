package dev.qurwan.lvcases;

import dev.qurwan.lvcases.db.Database;
import dev.qurwan.lvcases.util.Colorize;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.*;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.checkerframework.checker.lock.qual.Holding;

import java.util.*;

@Getter
public class Case {

    @Getter
    private final String type;
    @Getter
    private final Animation animation;
    @Getter
    private final Player player;
    @Getter @Setter
    private Status status;
    @Getter
    private Map<Integer, Location> locations = new HashMap<>();
    @Getter
    private Set<Item> drops = new HashSet<>();
    public Case(Player player, String type, Animation animation)
    {
        this.type = type;
        this.animation = animation;
        this.player = player;
        this.open();
        LvCases.setCase(this);
    }

    private void open()
    {
        Database.remove(this.player.getName(), this.type);
        setStatus(Status.WAITING);
        Bukkit.getScheduler().runTaskLater(LvCases.getInstance(), () -> {
            if (this.getStatus().equals(Status.WAITING) || LvCases.getCase() == null || !LvCases.getCase().equals(this))
                return;
            this.destroy();
        }, 30 * 20);

        Location c = new Location(
                Bukkit.getWorld(LvCases.getInstance().getConfig().getString("case-location").split(";")[3]),
                Double.parseDouble(LvCases.getInstance().getConfig().getString("case-location").split(";")[0]),
                Double.parseDouble(LvCases.getInstance().getConfig().getString("case-location").split(";")[1]),
                Double.parseDouble(LvCases.getInstance().getConfig().getString("case-location").split(";")[2])
        );

        locations.put(1, a(c, -3, 1));
        locations.put(2, a(c, -3, -1));
        locations.put(3, a(c, -1, -3));
        locations.put(4, a(c, 1, -3));
        locations.put(5, a(c, 3, -1));
        locations.put(6, a(c, 3, 1));
        locations.put(7, a(c, 1, 3));
        locations.put(8, a(c, -1, 3));

        player.sendTitle(Colorize.a(LvCases.getInstance().getConfig().getString("cases." + type + ".title").split(";")[0]),
               Colorize.a( (LvCases.getInstance().getConfig().getString("cases." + type + ".title").split(";")[1])));

        if (animation.equals(Animation.CHEST))
        {
            locations.keySet().forEach(i -> {
                Location l = locations.get(i);
                l.getBlock().setType(Material.CHEST);
                Chest chest = (Chest) l.getBlock().getState();
                chest.setCustomName("lvcase:" + i);
                chest.update();
                if (i == 1) rotate(l.getBlock(), BlockFace.EAST);
                if (i == 2) rotate(l.getBlock(), BlockFace.EAST);
                if (i == 3) rotate(l.getBlock(), BlockFace.SOUTH);
                if (i == 4) rotate(l.getBlock(), BlockFace.SOUTH);
                if (i == 5) rotate(l.getBlock(), BlockFace.WEST);
                if (i == 6) rotate(l.getBlock(), BlockFace.WEST);
                if (i == 7) rotate(l.getBlock(), BlockFace.NORTH);
                if (i == 8) rotate(l.getBlock(), BlockFace.NORTH);
            });
        }

        if (animation.equals(Animation.COBBLESTONE))
        {
            locations.keySet().forEach(i -> {
                Location l = locations.get(i);
                l.getBlock().setType(Material.COBBLESTONE);
                l.getBlock().getState().setMetadata("lvcase", new FixedMetadataValue(LvCases.getInstance(), String.valueOf(i)));
            });
        }

    }

    public void destroy() {
        this.drops.forEach(Entity::remove);
        this.getLocations().keySet().forEach(integer -> this.getLocations().get(integer).getBlock().setType(Material.AIR));
        LvCases.setCase(null);
    }




    private Location a(Location location, double x, double z)
    {
        return new Location(
                location.getWorld(), location.getX() + x, location.getY(), location.getZ() + z
        );
    }

    private void rotate(Block block, BlockFace direction) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Directional) {
            Directional directionalData = (Directional) blockData;
            directionalData.setFacing(direction);
            block.setBlockData(directionalData);
        }
    }


}
