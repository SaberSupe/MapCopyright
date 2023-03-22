package saber.mapcopyright.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import saber.mapcopyright.MapCopyright;
import saber.mapcopyright.utils.Copyright;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlatFileManager {

    private final MapCopyright plugin;
    protected FlatFileManager(MapCopyright p1){

        plugin = p1;

    }


    protected Copyright getCopyright(int map_id){

        //Get the file holding the copyright if it exists
        File f = getFile(map_id);
        if (!f.exists()) return null;

        //Read the values in the file
        FileConfiguration fcg = YamlConfiguration.loadConfiguration(f);
        UUID owner = UUID.fromString(fcg.getString("owner"));
        List<UUID> trusted = new ArrayList<>();
        for (String x : fcg.getStringList("trusted")) trusted.add(UUID.fromString(x));

        //Return a copyright containing those values
        return new Copyright(map_id, owner, trusted);

    }

    protected void addCopyright(Copyright copyr) {
        if (copyr == null) return;

        //Create the file that will hold the copyright data
        File f = getFile(copyr.getMapID());
        FileConfiguration fcg = YamlConfiguration.loadConfiguration(f);

        //Convert the trusted member from UUID to String
        List<String> trusted = new ArrayList<>();
        for (UUID x : copyr.getMembers()) trusted.add(x.toString());

        //Put the owner and trusted members in the file
        fcg.set("owner", copyr.getOwner().toString());
        fcg.set("trusted",trusted);

        //Save the file
        saveFile(fcg, f.getAbsolutePath());
    }

    protected void addMember(int mapid, UUID member){
        //Get the File
        File f = getFile(mapid);

        //If it doesn't exist, create it, this will include the new member so no need to continue
        if (!f.exists()) {
            addCopyright(plugin.getDataManager().getCopyright(mapid));
            return;
        }

        //Get the trusted list
        FileConfiguration fcg = YamlConfiguration.loadConfiguration(f);
        List<String> trusted = fcg.getStringList("trusted");

        //Add the new member
        trusted.add(member.toString());
        fcg.set("trusted",trusted);

        //Save the file
        saveFile(fcg, f.getAbsolutePath());
    }

    protected void remMember(int mapid, UUID member) {
        //Get the File
        File f = getFile(mapid);

        //If it doesn't exist, create it, this will include the member removal so no need to continue
        if (!f.exists()) {
            addCopyright(plugin.getDataManager().getCopyright(mapid));
            return;
        }

        //Get the trusted list
        FileConfiguration fcg = YamlConfiguration.loadConfiguration(f);
        List<String> trusted = fcg.getStringList("trusted");

        //Remove the member
        trusted.remove(member.toString());
        fcg.set("trusted",trusted);

        //Save the file
        saveFile(fcg, f.getAbsolutePath());
    }

    protected void remCopyright(Copyright copyr) {
        //Get the file and delete it if it exists
        File f = getFile(copyr.getMapID());
        if (f.exists()){
            f.delete();
        }
    }

    protected void giveOwner(int mapid, UUID newOwner) {
        //Get the File
        File f = getFile(mapid);

        //If it doesn't exist, create it, this will include the owner change so no need to continue
        if (!f.exists()) {
            addCopyright(plugin.getDataManager().getCopyright(mapid));
            return;
        }

        //Load the file and set the new owner
        FileConfiguration fcg = YamlConfiguration.loadConfiguration(f);
        fcg.set("owner",newOwner.toString());

        //Save the file
        saveFile(fcg, f.getAbsolutePath());
    }

    protected List<UUID> getTrustAll(UUID owner) {
        //Load the trustall List
        File f = getTrustAll();
        if (!f.exists()) return null;

        //Get all players who have trustall for the given player
        FileConfiguration fcg = YamlConfiguration.loadConfiguration(f);
        List<UUID> trusted = new ArrayList<>();
        for (String x : fcg.getStringList(owner.toString())) trusted.add(UUID.fromString(x));

        //Return the result
        if (trusted.size() == 0) return null;
        return trusted;
    }

    protected void addTrustAll(UUID owner, UUID player) {

        //Get the trust all file
        File f = getTrustAll();
        FileConfiguration fcg = YamlConfiguration.loadConfiguration(f);

        //Get the current trusted list for the owner
        List<String> trusted = fcg.getStringList(owner.toString());

        //Add the new player
        trusted.add(player.toString());
        fcg.set(owner.toString(), trusted);

        //Save the file
        saveFile(fcg, f.getAbsolutePath());
    }
    protected void remTrustAll(UUID owner, UUID player) {
        //Get the trustall file
        File f = getTrustAll();
        FileConfiguration fcg = YamlConfiguration.loadConfiguration(f);

        //Get the list for the owner
        List<String> trusted = fcg.getStringList(owner.toString());

        //Remove the given player
        trusted.remove(player.toString());
        fcg.set(owner.toString(), trusted);

        //Save the file
        saveFile(fcg, f.getAbsolutePath());
    }

    private File getFile(int map_id){
        //Return the file path for the given mapid, having a central command makes it easier to change if needed
        return new File(plugin.getDataFolder().getAbsolutePath()+ "/maps/" + map_id + ".yml");
    }
    private File getTrustAll(){
        //Return the file path for the trustall file, having a central command makes it easier to change if needed
        return new File(plugin.getDataFolder().getAbsolutePath() + "/trustall.yml");
    }

    private void saveFile(FileConfiguration fcg, String path){
        //Saves the file and handles the error
        try {
            fcg.save(path);
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    protected List<Integer> getAllCopyrightIDS() {

        //Get a list of all mapids by getting all files in maps folder
        List<Integer> result = new ArrayList<>();
        File dir = new File(plugin.getDataFolder().getAbsolutePath() + "/maps");
        if (!dir.exists()) return result;

        //remove .yml and convert the ids to integers
        String[] files = dir.list();
        for (String x : files) result.add(Integer.parseInt(x.replace(".yml","")));

        return result;
    }
}
