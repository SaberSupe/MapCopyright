package saber.mapcopyright.database;

import saber.mapcopyright.MapCopyright;
import saber.mapcopyright.utils.Copyright;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DataManager {

    private final MapCopyright instance;
    private List<Copyright> Rights = new ArrayList<>();
    private boolean usesql;
    private final boolean useflat;
    private SQLManager database = null;
    private FlatFileManager flatfile = null;
    private final HashMap<UUID,List<UUID>> trustall = new HashMap<>();

    public DataManager(MapCopyright instance){
        this.instance = instance;
        usesql = this.instance.getConfig().getBoolean("useSQL");
        useflat = this.instance.getConfig().getBoolean("useFlatFileStorage");

        if (usesql){
            //Initialize Database
            database = new SQLManager(this.instance);
            usesql = database.createTables();

            //If the database connection failed
            if (!usesql){
                this.instance.getLogger().log(Level.INFO, "Database connection failed");
            }
        }

        //Initialize flat file
        if (useflat) flatfile = new FlatFileManager(this.instance);

        //Warn if no data store is available
        if (!(useflat || usesql)){
            this.instance.getLogger().log(Level.INFO, "No usable datastore set in config, copyrights will not persist past restart");
        }

        //If both data stores are selected compare number of copyrights and sync if not the same
        if (usesql && useflat){
            syncDatastore();
        }
    }

    public Copyright getCopyright(int map_id){

        //Check if copyright is cached
        for (Copyright x : Rights){
            if (x.getMapID() == map_id) return x;
        }
        Copyright sqltemp, flattemp;

        //Check the database for the copyright
        if (usesql) {
            sqltemp = database.getCopyright(map_id);
            if (sqltemp != null){
                Rights.add(sqltemp);
                return sqltemp;
            }
        }

        //Check the flat file for the copyright
        if (useflat) {
            flattemp = flatfile.getCopyright(map_id);
            if (flattemp != null){
                Rights.add(flattemp);
                return flattemp;
            }
        }

        //null if not found
        return null;
    }

    public boolean addCopyright(int mapid, UUID owner){
        //Check if the copyright already exists
        if (getCopyright(mapid) != null) return false;

        //Create the copyright and cache it
        Copyright copyr = new Copyright(mapid,owner,null);
        Rights.add(copyr);

        //Store copyright in the data stores Asynchronously
        instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
            @Override
            public void run() {
                if (usesql) database.addCopyright(copyr);
                if (useflat) flatfile.addCopyright(copyr);
            }
        });

        return true;

    }

    public void remCopyright(int mapid){
        //Get the Copyright
        Copyright copyr = getCopyright(mapid);
        if (copyr == null) return;

        //Delete from cache and data stores
        Rights.remove(copyr);
        instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
            @Override
            public void run() {
                if (usesql) database.remCopyright(copyr);
                if (useflat) flatfile.remCopyright(copyr);
            }
        });
    }

    public void addMember(int mapid, UUID member){

        //Retrieve the copyright
        Copyright copyr = getCopyright(mapid);
        if (copyr != null) {
            if (copyr.getMembers().contains(member)) return;

            //Add the member to the cache and data store
            copyr.addMember(member);
            instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
                @Override
                public void run() {
                    if (usesql) database.addMember(mapid, member);
                    if (useflat) flatfile.addMember(mapid, member);
                }
            });
        }
    }

    public void remMember(int mapid, UUID member){

        //Retrieve the copyright
        Copyright copyr = getCopyright(mapid);
        if (copyr != null) {
            if (!copyr.getMembers().contains(member)) return;

            //Remove the member from the cache and data store
            copyr.remMember(member);
            instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
                @Override
                public void run() {
                    if (usesql) database.remMember(mapid, member);
                    if (useflat) flatfile.remMember(mapid, member);
                }
            });
        }
    }

    public void giveCopyright(int mapid, UUID newOwner) {

        //Retrieve the copyright
        Copyright copyr = getCopyright(mapid);
        if (copyr != null){

            //Set the new owner in cache and data store
            copyr.setOwner(newOwner);
            instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
                @Override
                public void run() {
                    if (usesql) database.giveOwner(mapid,newOwner);
                    if (useflat) flatfile.giveOwner(mapid,newOwner);
                }
            });
        }
    }

    public List<UUID> getTrustAll(UUID owner){

        //Get the cached trustall list for the player
        List<UUID> trusted = trustall.get(owner);

        //If it exists return the result
        if (trusted != null) return trusted;

        //Check the database for a trustall list for this user
        if (usesql){
            trusted = database.getTrustAll(owner);
            if (trusted != null){

                //Cache the list and return the result
                trustall.put(owner, trusted);
                return trusted;
            }
        }

        //Check the flat file for a trustall list for this user
        if (useflat){
            trusted = flatfile.getTrustAll(owner);
            if (trusted != null){

                //Cache the list and return the result
                trustall.put(owner, trusted);
                return trusted;
            }
        }

        //If no list is found
        trusted = new ArrayList<>();
        return trusted;
    }

    public boolean isTrustAll(UUID owner, UUID player){
        return getTrustAll(owner).contains(player);
    }

    public void addTrustAll(UUID owner, UUID player){

        //Check if they are already trustall, this also caches the list
        if (isTrustAll(owner, player)) return;

        //Load the list from cache and make one if it doesn't exist
        List<UUID> trusted = trustall.get(owner);
        if (trusted == null) {
            trusted = new ArrayList<>();
        }

        //Add the player to the trustall list
        trusted.add(player);
        trustall.put(owner, trusted);


        //Store the list in the data store
        instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
            @Override
            public void run() {
                if (usesql) database.addTrustAll(owner, player);
                if (useflat) flatfile.addTrustAll(owner, player);
            }
        });
    }

    public void remTrustAll(UUID owner, UUID player){

        //Check if they are trustall, also caches the list
        if (!isTrustAll(owner, player)) return;

        //Get the list and remove the player
        List<UUID> trusted = trustall.get(owner);
        trusted.remove(player);
        trustall.put(owner,trusted);

        //Update the data store
        instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
            @Override
            public void run() {
                if (usesql) database.remTrustAll(owner,player);
                if (useflat) flatfile.remTrustAll(owner,player);
            }
        });
    }

    private void syncDatastore() {
        List<Integer> flatcopyrights = flatfile.getAllCopyrightIDS();
        List<Integer> databasecopyrights = database.getAllCopyrightIDS();

        //Check if the database has the same number of copyrights as the flat file
        //This is a pretty shallow comparison check but will catch the majority of mismatch cases
        if (flatcopyrights.size() != databasecopyrights.size()) {
            instance.getLogger().log(Level.INFO, "Database mismatch with flat file detected, syncing");

            //Load all flat file copyrights into database if not already there
            for (Integer x : flatcopyrights) {
                if (!databasecopyrights.contains(x)) {
                    database.addCopyright(flatfile.getCopyright(x));
                }
            }

            //Load all database copyrights into flat file if not already there
            for (Integer x : databasecopyrights) {
                if (!flatcopyrights.contains(x)) {
                    flatfile.addCopyright(database.getCopyright(x));
                }
            }

            //Get all saved owners with a trust all list
            List<UUID> flatfileTrustAll = flatfile.getTrustAllOwners();
            List<UUID> databaseTrustAll = database.getAllTrustAllOwners();

            //Loop through flat file list adding any that don't exist in the database to the database
            for (UUID x : flatfileTrustAll){
                if (!databaseTrustAll.contains(x)){
                    for (UUID y : flatfile.getTrustAll(x)) database.addTrustAll(x,y);
                }
            }

            //Loop through database list adding any that don't exist in the flat file to the flat file
            for (UUID x : databaseTrustAll){
                if (!flatfileTrustAll.contains(x)){
                    for (UUID y : database.getTrustAll(x)) flatfile.addTrustAll(x,y);
                }
            }


            instance.getLogger().log(Level.INFO, "Sync Complete");
        }
    }
}
