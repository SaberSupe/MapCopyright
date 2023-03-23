package saber.mapcopyright.database;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import saber.mapcopyright.MapCopyright;
import saber.mapcopyright.utils.Copyright;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class SQLManager {

    private final MapCopyright plugin;
    private HikariDataSource hikari;

    protected SQLManager(MapCopyright p1){
        plugin = p1;
        FileConfiguration conf = plugin.getConfig();

        //Set up the database connection
        hikari = new HikariDataSource();
        hikari.setJdbcUrl("jdbc:mysql://" + conf.get("MySQL.address") + ":" + conf.get("MySQL.port") + "/" + conf.get("MySQL.database") + "?autoReconnect=true&useSSL=false");
        hikari.addDataSourceProperty("user", conf.get("MySQL.username"));
        hikari.addDataSourceProperty("password", conf.get("MySQL.password"));
    }

    protected boolean createTables(){
        //Create the database tables, returns false if it failed
        try (Connection datacon = hikari.getConnection();
            Statement state = datacon.createStatement()) {

            state.executeUpdate("CREATE TABLE IF NOT EXISTS MapCopyrightTrustAll (OwnerUUID binary(16), TrustedUUID binary(16))");
            state.executeUpdate("CREATE TABLE IF NOT EXISTS MapCopyrights (MapID INT, OwnerUUID binary(16))");
            state.executeUpdate("CREATE TABLE IF NOT EXISTS MapCopyrightMembers (MapID INT, TrustedUUID binary(16))");
            state.close();
            return true;
        } catch (SQLException e){
            //If this failed then hikari already will have put an error in console, no reason to do it here
            return false;
        }
    }

    protected Copyright getCopyright(int map_id){

        try(Connection connection = hikari.getConnection();
            //Set up select statements
            PreparedStatement pstmt = connection.prepareStatement("SELECT BIN_TO_UUID(OwnerUUID) FROM MapCopyrights WHERE MapID = ?;");
            PreparedStatement prep = connection.prepareStatement("SELECT BIN_TO_UUID(TrustedUUID) FROM MapCopyrightMembers WHERE MapID = ?;")) {

            //Get the Owner of the copyright
            pstmt.setInt(1, map_id);
            ResultSet rs = pstmt.executeQuery();
            UUID owner = null;
            if(rs.next()){
                 owner = UUID.fromString(rs.getString(1));
            } else {return null;}
            pstmt.close();

            //Get the trust list for the copyright
            prep.setInt(1, map_id);
            ResultSet rss = prep.executeQuery();


            List<UUID> trusted = new ArrayList<>();
            while (rss.next()) trusted.add(UUID.fromString(rss.getString(1)));

            prep.close();

            return new Copyright(map_id,owner,trusted);

        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "SQL getCopyright Failed");
            e.printStackTrace();
            return null;
        }
    }

    protected void addCopyright(Copyright copyr) {
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO MapCopyrights(MapID, OwnerUUID) VALUES(?, UUID_TO_BIN(?))");
            PreparedStatement prep = connection.prepareStatement("INSERT INTO MapCopyrightMembers(MapID, TrustedUUID) VALUES(?, UUID_TO_BIN(?))")){

            //Prepare and run sql insert statement to put copyright into copyrights table
            pstmt.setInt(1, copyr.getMapID());
            pstmt.setString(2, copyr.getOwner().toString());
            pstmt.execute();
            pstmt.close();

            //Prepare and run sql statement to put trustlist members into members table
            prep.setInt(1,copyr.getMapID());
            for (UUID x : copyr.getMembers()){
                prep.setString(2,x.toString());
                prep.execute();
            }
            prep.close();


        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "SQL addCopyright Failed");
            e.printStackTrace();
        }
    }

    protected void addMember(int mapid, UUID member){
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO MapCopyrightMembers(MapID, TrustedUUID) VALUES(?, UUID_TO_BIN(?))")){

            //Execute insert into members table to add new trustee
            pstmt.setInt(1, mapid);
            pstmt.setString(2, member.toString());
            pstmt.execute();
            pstmt.close();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "SQL addMember Failed");
            e.printStackTrace();
        }
    }

    protected void remMember(int mapid, UUID member) {
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM MapCopyrightMembers WHERE MapID = ? AND TrustedUUID = UUID_TO_BIN(?);")){

            //Delete row the has the trust for the given player and the given map id
            pstmt.setInt(1, mapid);
            pstmt.setString(2, member.toString());
            pstmt.execute();
            pstmt.close();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "SQL remMember Failed");
            e.printStackTrace();
        }
    }

    protected void remCopyright(Copyright copyr) {
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM MapCopyrightMembers WHERE MapID = ?;");
            PreparedStatement prep = connection.prepareStatement("DELETE FROM MapCopyrights WHERE MapID = ?;")){

            //Delete the copyright from the copyrights table
            pstmt.setInt(1, copyr.getMapID());
            pstmt.execute();
            pstmt.close();

            //Delete the trust list for the copyright
            prep.setInt(1, copyr.getMapID());
            prep.execute();
            prep.close();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "SQL remCopyright Failed");
            e.printStackTrace();
        }
    }

    protected void giveOwner(int mapid, UUID newOwner) {
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("UPDATE MapCopyrights SET OwnerUUID = UUID_TO_BIN(?) WHERE MapID = ?;")){

            pstmt.setString(1, newOwner.toString());
            pstmt.setInt(2, mapid);
            pstmt.execute();
            pstmt.close();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "SQL giveOwner Failed");
            e.printStackTrace();
        }
    }

    protected List<UUID> getTrustAll(UUID owner) {
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT BIN_TO_UUID(TrustedUUID) FROM MapCopyrightTrustAll WHERE OwnerUUID = UUID_TO_BIN(?);")) {

            //Execute the statement to get the trustall list for the user
            pstmt.setString(1, owner.toString());
            ResultSet rs = pstmt.executeQuery();

            //loop through converting to UUID
            List<UUID> trusted = new ArrayList<>();
            while(rs.next()){
                trusted.add(UUID.fromString(rs.getString(1)));
            }

            //return the result
            pstmt.close();
            return trusted;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "SQL getTrustAll Failed");
            e.printStackTrace();
            return null;
        }
    }

    protected void remTrustAll(UUID owner, UUID player) {
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM MapCopyrightTrustAll WHERE OwnerUUID = UUID_TO_BIN(?) AND TrustedUUID = UUID_TO_BIN(?);")){

            //Execute delete statement to remove user from the trustall list
            pstmt.setString(1, owner.toString());
            pstmt.setString(2, player.toString());
            pstmt.execute();
            pstmt.close();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "SQL remTrustAll Failed");
            e.printStackTrace();
        }
    }

    protected void addTrustAll(UUID owner, UUID player) {
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO MapCopyrightTrustAll(OwnerUUID, TrustedUUID) VALUES(UUID_TO_BIN(?), UUID_TO_BIN(?))")){

            //Add a row showing that the player has trustall for the given owner
            pstmt.setString(1, owner.toString());
            pstmt.setString(2, player.toString());
            pstmt.execute();
            pstmt.close();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "SQL addTrustAll Failed");
            e.printStackTrace();
        }
    }

    protected List<Integer> getAllCopyrightIDS() {
        List<Integer> result = new ArrayList<>();
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT MapID FROM MapCopyrights;")) {

            //Retrieve all copyright ids from database
            ResultSet rs = pstmt.executeQuery();

            //Loop through adding to integer list
            while(rs.next()){
                result.add(rs.getInt(1));
            }

            //Return the list
            pstmt.close();
            return result;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "SQL getAllCopyrightIDS Failed");
            e.printStackTrace();
            return result;
        }
    }

    protected List<UUID> getAllTrustAllOwners() {
        List<UUID> owners = new ArrayList<>();
        try(Connection connection = hikari.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT DISTINCT BIN_TO_UUID(OwnerUUID) FROM MapCopyrightTrustAll;")) {

            //Retrieve all trust all owners from database
            ResultSet rs = pstmt.executeQuery();

            //Loop through converting uuid and adding to list
            while(rs.next()){
                owners.add(UUID.fromString(rs.getString(1)));
            }

            //Return the list
            pstmt.close();
            return owners;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.INFO, "SQL GetTrustAllOwners Failed");
            e.printStackTrace();
            return owners;
        }
    }
}
