package dev.qurwan.lvcases;

import dev.qurwan.lvcases.db.Database;
import dev.qurwan.lvcases.listener.ChestAnimationListener;
import dev.qurwan.lvcases.listener.CobblestoneAnimationListener;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@Getter
public final class LvCases extends JavaPlugin {
    @Getter
    private static LvCases instance;
    @Getter
    private static Map<String, String> users = new HashMap<>();
    private Database database;
    @Getter @Setter
    private static Case Case;

    @Override
    public void onEnable() {

        instance = this;
        database = new Database(new File(this.getDataFolder(), "users.db"));
        database.load();


        Bukkit.getPluginManager().registerEvents(new ChestAnimationListener(), this);
        Bukkit.getPluginManager().registerEvents(new CobblestoneAnimationListener(), this);
        Bukkit.getPluginManager().registerEvents(new CaseMenu(), this);
        saveDefaultConfig();
        getCommand("lvcases").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
                if (!commandSender.isOp())
                {
                    commandSender.sendMessage("Нет прав.");
                    return true;
                }

                if (strings[0].equals("give"))
                {
                    if (strings.length != 4)
                    {
                        commandSender.sendMessage("Используйте: /case give [nick] [type] [amount]");
                        commandSender.sendMessage("Пример: /case give qurwan donate 12");
                        return true;
                    }
                    String target = strings[1];
                    String type = strings[2];
                    for(int i = 0; i < Integer.parseInt(strings[3]); i++) Database.give(target, type);
                    return true;
                }

                commandSender.sendMessage("Неизвестная сабкоманда.");

                return true;
            }
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Case != null)
                {
                    spawnParticle(
                            new Location(
                                    Bukkit.getWorld(getConfig().getString("case-location").split(";")[3]),
                                    Double.parseDouble(getConfig().getString("case-location").split(";")[0]) + 0.5,
                                    Double.parseDouble(getConfig().getString("case-location").split(";")[1]) + 2.8,
                                    Double.parseDouble(getConfig().getString("case-location").split(";")[2]) + 0.5
                            ), Particle.FLAME, 100, 100, 3.0
                    );
                }
            }
        }.runTaskTimerAsynchronously(this, 10L, 10L);

    }

    @Override
    public void onDisable()
    {
        database.save();
        getCase().destroy();
    }

    private void spawnParticle(@NotNull Location center, Particle particle, int smooth, int count, double radius) {
        World world = center.getWorld();

        IntStream.range(0, smooth).mapToDouble(i -> i * 2 * Math.PI / count).forEach(angle -> {
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + 0.15;
            double z = center.getZ() + radius * Math.sin(angle);

            world.spawnParticle(particle, new Location(world, x, y, z), 0);
        });
    }


}
