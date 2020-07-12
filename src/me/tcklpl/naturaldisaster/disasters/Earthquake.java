package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.ArenaBiomeType;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Earthquake extends Disaster {

    private final int gravityBuffer = 100;

    private static class GravityCandidates {

        private final List<Block> highPriorityBlocks;
        private final List<Block> lowPriorityBlocks;

        public GravityCandidates(List<Block> highPriorityBlocks, List<Block> lowPriorityBlocks) {
            this.highPriorityBlocks = highPriorityBlocks;
            this.lowPriorityBlocks = lowPriorityBlocks;
        }

        public List<Block> getHighPriorityBlocks() {
            return highPriorityBlocks;
        }

        public List<Block> getLowPriorityBlocks() {
            return lowPriorityBlocks;
        }
    }

    public Earthquake(DisasterMap map, JavaPlugin main) {
        super(map, main);
        name = "Earthquake";
        hint = "Evite locais altos.";
        playable = true;
        icon = Material.COBBLESTONE;
        precipitationType = ReflectionUtils.PrecipitationType.ALL;
        arenaBiomeType = ArenaBiomeType.RANDOM_PER_PRECIPITATION;
    }

    private GravityCandidates getYColumnBlocks(int x, int z, int divisor) {

        List<Block> blocksToBreak = new ArrayList<>();
        List<Block> blocksToDisappear = new ArrayList<>();
        int counter = 0;

        for (int y = map.floor; y <= map.top; y++) {
            Block b = map.getWorld().getBlockAt(x, y, z);
            if (counter++ % divisor == 0) {
                blocksToBreak.add(b);
            } else {
                blocksToDisappear.add(b);
            }
        }

        return new GravityCandidates(blocksToBreak, blocksToDisappear);
    }

    private List<Block> generateInitialFloorCrack(int xz) {
        List<Block> initialCrack = new ArrayList<>();

        // Get floor level
        Random r = new Random();
        int floorYlevel = map.floor - 1;
        boolean foundFloor = false;

        for (; floorYlevel < map.top && !foundFloor; floorYlevel++) {
            for (int x = map.minX; x <= map.minX + map.gapX && !foundFloor; x++)
                for (int z = map.minZ; z <= map.minZ + map.gapZ && !foundFloor; z++)
                    if (map.getWorld().getBlockAt(x, floorYlevel, z).getType() != Material.AIR)
                        foundFloor = true;
        }
        floorYlevel--;

        int epicenterX = map.minX + Math.floorDiv(map.gapX, 3) + r.nextInt(Math.floorDiv(map.gapX, 3));
        int epicenterZ = map.minZ + Math.floorDiv(map.gapZ, 3) + r.nextInt(Math.floorDiv(map.gapZ, 3));

        int direcaoExata;

        Block epicenter = map.getWorld().getBlockAt(epicenterX, floorYlevel, epicenterZ);
        initialCrack.add(epicenter);
        Block nextBlockToBreak = epicenter;

        // NORTE - SUL
        if (xz == 0) {

            // Primeiro expandir pro norte
            while (nextBlockToBreak.getType() != Material.AIR) {

                direcaoExata = r.nextInt(3);
                switch (direcaoExata) {
                    case 0:
                        nextBlockToBreak = nextBlockToBreak.getRelative(BlockFace.NORTH_WEST);
                        break;
                    case 1:
                        nextBlockToBreak = nextBlockToBreak.getRelative(BlockFace.NORTH);
                        break;
                    case 2:
                        nextBlockToBreak = nextBlockToBreak.getRelative(BlockFace.NORTH_EAST);
                        break;
                }
                initialCrack.add(nextBlockToBreak);
            }

            nextBlockToBreak = epicenter;

            // Agora expandir pro sul
            while (nextBlockToBreak.getType() != Material.AIR) {

                direcaoExata = r.nextInt(3);
                switch (direcaoExata) {
                    case 0:
                        nextBlockToBreak = nextBlockToBreak.getRelative(BlockFace.SOUTH_WEST);
                        break;
                    case 1:
                        nextBlockToBreak = nextBlockToBreak.getRelative(BlockFace.SOUTH);
                        break;
                    case 2:
                        nextBlockToBreak = nextBlockToBreak.getRelative(BlockFace.SOUTH_EAST);
                        break;
                }
                initialCrack.add(nextBlockToBreak);
            }
            // LESTE - OESTE
        } else {

            // Primeiro expandir pro leste
            while (nextBlockToBreak.getType() != Material.AIR) {

                direcaoExata = r.nextInt(3);
                switch (direcaoExata) {
                    case 0:
                        nextBlockToBreak = nextBlockToBreak.getRelative(BlockFace.NORTH_EAST);
                        break;
                    case 1:
                        nextBlockToBreak = nextBlockToBreak.getRelative(BlockFace.EAST);
                        break;
                    case 2:
                        nextBlockToBreak = nextBlockToBreak.getRelative(BlockFace.SOUTH_EAST);
                        break;
                }
                initialCrack.add(nextBlockToBreak);
            }

            nextBlockToBreak = epicenter;

            // Agora expandir pro oeste
            while (nextBlockToBreak.getType() != Material.AIR) {

                direcaoExata = r.nextInt(3);
                switch (direcaoExata) {
                    case 0:
                        nextBlockToBreak = nextBlockToBreak.getRelative(BlockFace.NORTH_WEST);
                        break;
                    case 1:
                        nextBlockToBreak = nextBlockToBreak.getRelative(BlockFace.WEST);
                        break;
                    case 2:
                        nextBlockToBreak = nextBlockToBreak.getRelative(BlockFace.SOUTH_WEST);
                        break;
                }
                initialCrack.add(nextBlockToBreak);
            }
        }

        return initialCrack;
    }

    private List<Block> expandFloorCrack(int distance, int xz, List<Block> initialCrack) {

        List<Block> expandedCrack = new ArrayList<>();
        // crack de norte a sul, expandir de leste a oeste
        if (xz == 0) {
            for (Block b : initialCrack) {
                expandedCrack.add(b.getRelative(BlockFace.EAST, distance));
                expandedCrack.add(b.getRelative(BlockFace.WEST, distance));
            }
        }
        // crack de leste a oeste, expandir de norte a sul
        else {
            for (Block b : initialCrack) {
                expandedCrack.add(b.getRelative(BlockFace.NORTH, distance));
                expandedCrack.add(b.getRelative(BlockFace.SOUTH, distance));
            }
        }
        return expandedCrack;
    }

    private GravityCandidates generateGravityCandidates() {

        List<Block> gravityCandidates = new ArrayList<>();
        List<Block> lowPriorityGravityCandidates = new ArrayList<>();

        Random r = new Random();
        // Blocos podem ser afetados por gravidade acima de 1/2 da altura do mapa + [0, 1/4 da altura do mapa]
        int yTreshold = Math.floorDiv(map.top - map.floor, 2) + r.nextInt(Math.floorDiv(map.top - map.floor, 4));

        for (int y = yTreshold; y <= map.top; y++) {
            for (int x = map.minX; x <= map.minX + map.gapX; x++)
                for (int z = map.minZ; z <= map.minZ + map.gapZ; z++)
                    if (map.getWorld().getBlockAt(x, y, z).getType() != Material.AIR) {
                        Block b = map.getWorld().getBlockAt(x, y, z);
                        Block downRelative = b.getRelative(BlockFace.DOWN);
                        if (!b.getType().toString().contains("LEAVES"))
                        if (downRelative.getType() == Material.AIR || downRelative.isPassable())
                            gravityCandidates.add(b);
                        else if (gravityCandidates.contains(downRelative))
                            lowPriorityGravityCandidates.add(b);
                    }
        }

        return new GravityCandidates(gravityCandidates, lowPriorityGravityCandidates);
    }

    private void destroyCrackPattern(List<Block> blocks) {
        List<Block> highBlocks = new ArrayList<>();
        List<Block> lowBlocks = new ArrayList<>();
        for (Block b : blocks) {
            GravityCandidates candidates = getYColumnBlocks(b.getX(), b.getZ(), 2);
            highBlocks.addAll(candidates.getHighPriorityBlocks());
            lowBlocks.addAll(candidates.getLowPriorityBlocks());
        }

        map.bufferedReplaceBlocks(lowBlocks, Material.AIR, 300, false);
        map.bufferedReplaceBlocks(highBlocks, Material.AIR, 300, true);
    }

    @Override
    public void startDisaster() {
        super.startDisaster();

        map.makeRain(true);

        Random r = random;
        // XZ = 0 norte - sul
        // XZ = 1 leste - oeste
        int xz = r.nextInt(2);
        List<Block> initialCrack = generateInitialFloorCrack(xz);

        AtomicInteger timesRunned = new AtomicInteger(0);
        AtomicInteger currentExpansion = new AtomicInteger(0);
        AtomicBoolean hasCracked = new AtomicBoolean(false);

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            timesRunned.addAndGet(1);
            if ((timesRunned.get() % 3) == 0) {

                // 40% de chance
                if (r.nextInt(100) < 70) {
                    if (hasCracked.get()) {
                        List<Block> expansion = expandFloorCrack(currentExpansion.getAndIncrement(), xz, initialCrack);
                        destroyCrackPattern(expansion);
                    } else {
                        hasCracked.set(true);
                        destroyCrackPattern(initialCrack);
                    }

                    if (currentExpansion.get() <= 2)
                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                        GravityCandidates gravityCandidates = generateGravityCandidates();
                        map.bufferedReplaceBlocks(gravityCandidates.getLowPriorityBlocks(), Material.AIR, gravityBuffer, false);
                        map.bufferedReplaceBlocks(gravityCandidates.getHighPriorityBlocks(), Material.AIR, gravityBuffer, true);
                    }, 20L);
                }

            }


        }, startDelay, 20L);
    }
}
