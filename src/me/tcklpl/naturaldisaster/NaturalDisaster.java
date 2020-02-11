package me.tcklpl.naturaldisaster;

import me.tcklpl.naturaldisaster.admin.AdminInventoryClick;
import me.tcklpl.naturaldisaster.admin.ArenaAdmin;
import me.tcklpl.naturaldisaster.auth.AuthCommands;
import me.tcklpl.naturaldisaster.auth.AuthManager;
import me.tcklpl.naturaldisaster.commands.*;
import me.tcklpl.naturaldisaster.database.Database;
import me.tcklpl.naturaldisaster.events.*;
import me.tcklpl.naturaldisaster.events.arena.Damage;
import me.tcklpl.naturaldisaster.events.arena.Death;
import me.tcklpl.naturaldisaster.map.MapManager;
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayerManager;
import me.tcklpl.naturaldisaster.player.friends.FriendsGUI;
import me.tcklpl.naturaldisaster.player.monetaryPlayer.CustomPlayerManager;
import me.tcklpl.naturaldisaster.player.skins.RefreshSkin;
import me.tcklpl.naturaldisaster.player.skins.SkinManager;
import me.tcklpl.naturaldisaster.shop.ShopCommand;
import me.tcklpl.naturaldisaster.shop.ShopInventoryClick;
import me.tcklpl.naturaldisaster.worlds.WorldCommands;
import me.tcklpl.naturaldisaster.worlds.WorldManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;

public class NaturalDisaster extends JavaPlugin {

    WorldManager worldManager;
    private static Database database;
    private static JavaPlugin mainReference;
    private static AuthManager authManager;
    private static CPlayerManager cPlayerManager;

    @Override
    public void onEnable() {

        mainReference = this;
        authManager = new AuthManager();

        List<String> managedWorlds = getConfig().getStringList("worlds");
        worldManager = new WorldManager(managedWorlds);

        MapManager.getInstance().setMainReference(this);
        MapManager.getInstance().setupDisasters();
        MapManager.getInstance().setupArenas();
        MapManager.getInstance().setCurrentStatus(GameStatus.IN_LOBBY);

        SkinManager.getInstance().setMainInstance(this);
        SkinManager.getInstance().setupSkins();

        cPlayerManager = new CPlayerManager();
        cPlayerManager.loadPlayers();

        registerEvents();
        registerCommands();

        database = new Database(getDataFolder() + "/database.db");
        database.assertDefaults();

    }

    @Override
    public void onDisable() {

        getConfig().set("worlds", worldManager.getManagedWorlds());
        MapManager.getInstance().saveArenas();
        cPlayerManager.savePlayers();
        try {
            SkinManager.getInstance().saveSkins();
        } catch (IOException e) {
            e.printStackTrace();
        }

        saveConfig();
    }

    private void registerEvents() {

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Motd(), this);
        pm.registerEvents(new Join(this), this);
        pm.registerEvents(new Death(), this);
        pm.registerEvents(new PickItem(), this);
        pm.registerEvents(new FoodLevel(), this);
        pm.registerEvents(new IceMelt(), this);
        pm.registerEvents(new Leave(), this);
        pm.registerEvents(new Move(), this);
        pm.registerEvents(new AdminInventoryClick(), this);
        pm.registerEvents(new Chat(), this);
        pm.registerEvents(new Damage(), this);
        pm.registerEvents(new Chair(), this);
        pm.registerEvents(new MobSpawn(), this);
        pm.registerEvents(new ShopInventoryClick(), this);
        pm.registerEvents(new FriendsGUI(), this);

    }

    private void registerCommands() {

        Objects.requireNonNull(getCommand("world")).setExecutor(new WorldCommands(worldManager));
        GamemodeCommands gmc = new GamemodeCommands();
        Objects.requireNonNull(getCommand("creative")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("survival")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("adventure")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("spectator")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("arena")).setExecutor(new MapCreator(this));
        Objects.requireNonNull(getCommand("start")).setExecutor(new Start(this));
        Objects.requireNonNull(getCommand("balance")).setExecutor(new Balance());
        Objects.requireNonNull(getCommand("admin")).setExecutor(new ArenaAdmin());
        Objects.requireNonNull(getCommand("sch")).setExecutor(new SchematicCommand());
        Objects.requireNonNull(getCommand("heal")).setExecutor(new Heal());
        Objects.requireNonNull(getCommand("shop")).setExecutor(new ShopCommand());
        Objects.requireNonNull(getCommand("friends")).setExecutor(new FriendsGUI());
        Objects.requireNonNull(getCommand("refresh")).setExecutor(new RefreshSkin());
        AuthCommands authCommands = new AuthCommands();
        Objects.requireNonNull(getCommand("register")).setExecutor(authCommands);
        Objects.requireNonNull(getCommand("login")).setExecutor(authCommands);

    }

    public static AuthManager getAuthManager() { return authManager; }

    public static JavaPlugin getMainReference() {
        return mainReference;
    }

    public static Database getDatabase() {
        return database;
    }

    public static CPlayerManager getPlayerManager() { return  cPlayerManager; }

    public static AuthManager getAuthenticationManager() {
        return authManager;
    }
}
