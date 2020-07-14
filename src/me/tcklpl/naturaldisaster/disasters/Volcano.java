package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.ArenaBiomeType;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import me.tcklpl.naturaldisaster.schematics.SchematicLoadPosition;
import me.tcklpl.naturaldisaster.schematics.SchematicManager;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicInteger;

public class Volcano extends Disaster {

    public Volcano(DisasterMap map, JavaPlugin main) {
        super(map, main);
        name = "Volcano";
        hint = "Te fode lek kkk";
        playable = false;
        icon = Material.LAVA_BUCKET;
        precipitationType = ReflectionUtils.PrecipitationType.ALL;
        arenaBiomeType = ArenaBiomeType.SPECIFIC;
        arenaSpecificBiome = Biome.BASALT_DELTAS;
    }

    private Location calcVolcanoSpawnPosition() {
        // true = X axis
        // false = Z axis
        boolean xAxis = random.nextBoolean();
        // true = positive axis
        // false = negative axis
        boolean positive = random.nextBoolean();

        int x = 0, y = map.floor, z = 0;

        if (xAxis) {
            x = positive ? map.minX + map.gapX + 10 : map.minX - 10;
            z = (map.minZ - 10) + random.nextInt(map.gapZ + 20);
        } else {
            z = positive ? map.minZ + map.gapZ + 10 : map.minZ - 10;
            x = (map.minX - 10) + random.nextInt(map.gapX + 20);
        }

        return new Location(map.getWorld(), x, y, z);
    }

    @Override
    public void startDisaster() {
        super.startDisaster();

        Location volcanoPos = calcVolcanoSpawnPosition();
        SchematicManager.getInstance().loadSchematic("volcano", volcanoPos, true, SchematicLoadPosition.FLOOR_CENTER);

        map.getWorld().setGameRule(GameRule.DO_FIRE_TICK, true);

        AtomicInteger timesRunned = new AtomicInteger(0);

        BlockData magmaData = Bukkit.createBlockData(Material.MAGMA_BLOCK);

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            timesRunned.incrementAndGet();
            if (timesRunned.get() % 3 == 0) {

                for (Location loc : map.getRandomXZPoints(5, false, 25)) {
                    FallingBlock fb = map.getWorld().spawnFallingBlock(loc, magmaData);
                    fb.setDropItem(false);
                    fb.setHurtEntities(true);
                }

            }

        }, startDelay, 20L);

    }
}