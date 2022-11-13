package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.reflection.ReflectionWorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TNTRain extends Disaster {

    private Random r;

    public TNTRain() {
        super("TNT Rain", true, Material.TNT, ReflectionWorldUtils.Precipitation.ALL);
    }

    @Override
    public void startDisaster() {

        super.startDisaster();

        r = random;

        map.makeRain(true);

        AtomicInteger tntToSpawn = new AtomicInteger(1);
        AtomicInteger timesRunned = new AtomicInteger(0);

        int taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {

            for (int i = 0; i < tntToSpawn.get(); i++) {
                int x = map.minX + r.nextInt(map.gapX);
                int z = map.minZ + r.nextInt(map.gapZ);
                Location loc = new Location(map.getWorld(), x, map.top, z);

                TNTPrimed tnt = Objects.requireNonNull(map.getWorld()).spawn(loc, TNTPrimed.class);

                tnt.setTicksLived(5);

                tnt.setFuseTicks(20 + 20 * Math.floorDiv(map.top - map.floor, 20)); // 1s inicial + 1s p/ cada 20 blocos
            }

            if ((timesRunned.incrementAndGet() % 10) == 0)
                tntToSpawn.addAndGet(1);

        }, startDelay, 20L);

        registerTasks(taskId);

    }

}