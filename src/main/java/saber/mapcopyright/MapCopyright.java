package saber.mapcopyright;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import saber.mapcopyright.commands.CopyrightCommand;
import saber.mapcopyright.database.DataManager;
import saber.mapcopyright.events.ItemCraftListener;
import saber.mapcopyright.events.MapCreationListener;
import saber.mapcopyright.utils.TabComplete;

import java.util.UUID;
import java.util.logging.Level;
public final class MapCopyright extends JavaPlugin {


    private DataManager dataman;
    private int CurMapID = 0;
    public final UUID PublicTrust = new UUID(0,0);
    @Override
    public void onEnable() {
        // Plugin startup logic
        super.onEnable();

        //Load Config
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        //Register listener
        getServer().getPluginManager().registerEvents(new ItemCraftListener(this), this);
        getServer().getPluginManager().registerEvents(new MapCreationListener(this), this);

        //initialize the data manager for all the stored data
        dataman = new DataManager(this);

        //Loop through all mapids, getting the highest available
        initializeCurMapID();

        //Register command
        getCommand("copyright").setExecutor(new CopyrightCommand(this));
        getCommand("copyright").setTabCompleter(new TabComplete());

        //Log successful launch
        this.getLogger().log(Level.INFO, "MapCopyright loaded Successfully");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @SuppressWarnings("deprecation")
    private void findCurMapID(){
        //Linearly search for lowest null mapid
        while(Bukkit.getMap(CurMapID) != null){
            CurMapID++;
        }
    }

    @SuppressWarnings("deprecation")
    private void initializeCurMapID(){
        //This searches for the highest mapid faster that the linear one above
        //Used as it may make a difference and servers with sufficiently large amounts of maps

        //Increase the mapid above 0
        if (Bukkit.getMap(CurMapID) != null) CurMapID = 1;
        else return;

        //Continue to double until we find a null value
        while(Bukkit.getMap(CurMapID) != null) CurMapID = CurMapID*2;

        //Binary search till the range is shrunk down to nothing
        int high = CurMapID;
        int low = CurMapID/2;
        while (low+1 != high){
            CurMapID = (high+low)/2;
            if (Bukkit.getMap(CurMapID) == null) high = CurMapID;
            else low = CurMapID;
        }

        //The high result will be the lowest null mapid
        CurMapID=high;
    }

    public int getCurMapID(){
        findCurMapID();
        return CurMapID;
    }

    public DataManager getDataManager(){
        return dataman;
    }

}
