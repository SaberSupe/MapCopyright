package saber.mapcopyright.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import saber.mapcopyright.MapCopyright;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AreaCommands {

    public static void areaCommand(Player play, NamespacedKey key, String[] args, boolean force, MapCopyright plugin){

        //If the player just enters /copyright area, do nothing
        if (args.length == 1){
            return;
        }

        //Locate the top left chunk in the players current 8x8 chunk map area
        int chunkx = play.getLocation().getBlockX() - 64;
        int chunkz = play.getLocation().getBlockZ() - 64;

        chunkx = chunkx - (chunkx % (16*8)) + 64;
        chunkz = chunkz - (chunkz % (16*8)) + 64;

        chunkx = chunkx/16;
        chunkz = chunkz/16;

        //Get the persistent data container for that chunk
        PersistentDataContainer container = play.getWorld().getChunkAt(chunkx, chunkz).getPersistentDataContainer();

        if (args[1].equalsIgnoreCase("info")){

            //Check if the area is claimed
            if (!container.has(key,PersistentDataType.LONG_ARRAY)){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.NotClaimed")));
                return;
            }

            //Get the trust list for the area
            long[] trustlist = container.get(key, PersistentDataType.LONG_ARRAY);

            //Loop through converting trust list to list of player names and look for public trust
            boolean publictrust = false;
            List<String> players = new ArrayList<>();
            for (int i = 0; i < trustlist.length-1; i+=2){

                //Rebuild UUID from stored longs
                UUID temp = new UUID(trustlist[i],trustlist[i+1]);

                //Check if the UUID denotes public trust
                if (temp.equals(plugin.PublicTrust)) publictrust = true;

                //Add the Player to the list of trusted players
                else players.add(Bukkit.getOfflinePlayer(temp).getName());
            }

            //The first player in the list denotes the owner, send message telling owner name
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.Info.Owner")).replace("{ign}", players.get(0)));

            //Convert list of names into comma separated string
            String trusted = "";
            if (players.size()>1) trusted = players.get(1);
            for (int i = 2; i < players.size(); i++) {
                trusted = trusted + ", " + players.get(i);
            }

            //Send the string of trusted players as well as true/false for public trust
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.Info.TrustList")).replace("{trustlist}", trusted));
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.Info.Public")).replace("{public}", String.valueOf(publictrust)));
            return;
        }

        if (args[1].equalsIgnoreCase("claim")){

            //Check if area is claimed
            if (container.has(key, PersistentDataType.LONG_ARRAY)){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.AlreadyClaimed")));
                return;
            }

            //Generate the trust list, the first player in the list is the owner
            //Since this chunk is just now being claimed, the list will only contain the owner
            long[] trusted = new long[2];
            trusted[0] = play.getUniqueId().getMostSignificantBits();
            trusted[1] = play.getUniqueId().getLeastSignificantBits();

            //Save the list
            container.set(key, PersistentDataType.LONG_ARRAY, trusted);

            //Inform the player
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.AddClaim")));
            return;
        }
        if (args[1].equalsIgnoreCase("unclaim")){

            //Check if the area is claimed
            if (!container.has(key,PersistentDataType.LONG_ARRAY)){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.NotClaimed")));
                return;
            }

            //Retrieve the trust list
            long[] trusted = container.get(key,PersistentDataType.LONG_ARRAY);

            //This should never be true as it indicates an invalid trust list, if it does happen, the chunk will just be unclaimed
            if (trusted.length<2){
                container.remove(key);
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.AreaBroken")));
                return;
            }

            //Get the owner from the trust list, always the first entry
            UUID owner = new UUID(trusted[0],trusted[1]);

            //Check if the player issuing the command is the owner or using force
            if (!owner.equals(play.getUniqueId()) && !force){
                //If not owner, tell them
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.NotOwner")));
                return;
            }

            //Delete the trustlist from the area and inform the player
            container.remove(key);
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.DeleteClaim")));
            return;

        }
        if (args[1].equalsIgnoreCase("trust")){

            //Check if the area is claimed
            if (!container.has(key,PersistentDataType.LONG_ARRAY)){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.NotClaimed")));
                return;
            }

            //Get the trust list
            long[] trusted = container.get(key,PersistentDataType.LONG_ARRAY);

            //This should never be true as it indicates an invalid trust list, if it does happen, the chunk will just be unclaimed
            if (trusted.length<2){
                container.remove(key);
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.AreaBroken")));
                return;
            }

            //Get the owner and check if the issuing player is them or using force
            UUID owner = new UUID(trusted[0],trusted[1]);
            if (!owner.equals(play.getUniqueId()) && !force){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.NotOwner")));
                return;
            }

            //Check if the player entered an ign
            if (args.length < 3){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.PlayerNotFound")));
                return;
            }

            //Get the entered player and inform the command issuer if not found
            Player member = Bukkit.getPlayer(args[2]);
            if (member == null){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.PlayerNotFound")));
                return;
            }

            //Create a new trust list with 2 more spaces to accommodate the new member
            long[] newtrusted = new long[trusted.length+2];

            //Copy the old trustlist
            for (int i = 0; i < trusted.length; i++) newtrusted[i] = trusted[i];

            //Add the new trusted member to the end
            newtrusted[newtrusted.length-2] = member.getUniqueId().getMostSignificantBits();
            newtrusted[newtrusted.length-1] = member.getUniqueId().getLeastSignificantBits();

            //Save the new trust list and inform of success
            container.set(key,PersistentDataType.LONG_ARRAY,newtrusted);
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.GiveTrust")).replace("{ign}", member.getName()));
            return;
        }
        if (args[1].equalsIgnoreCase("untrust")){
            //Check if the area is claimed
            if (!container.has(key,PersistentDataType.LONG_ARRAY)){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.NotClaimed")));
                return;
            }

            //Get the trust list
            long[] trusted = container.get(key,PersistentDataType.LONG_ARRAY);

            //This should never be true as it indicates an invalid trust list, if it does happen, the chunk will just be unclaimed
            if (trusted.length<2){
                container.remove(key);
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.AreaBroken")));
                return;
            }

            //Get the owner and check if the issuer is them or using force
            UUID owner = new UUID(trusted[0],trusted[1]);
            if (!owner.equals(play.getUniqueId()) && !force){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.NotOwner")));
                return;
            }

            //Check if they entered an ign
            if (args.length < 3){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.PlayerNotFound")));
                return;
            }

            //Get the player associated with the entered ign
            OfflinePlayer member = Bukkit.getPlayer(args[2]);

            //If not found look at trusted players for match
            if (member == null){
                for (int i = 2; i < trusted.length-1; i+=2){
                    UUID temp = new UUID(trusted[i],trusted[i+1]);
                    if (Bukkit.getOfflinePlayer(temp).getName() != null && Bukkit.getOfflinePlayer(temp).getName().equalsIgnoreCase(args[2])){
                        member = Bukkit.getOfflinePlayer(temp);
                    }
                }
            }

            //If not found inform the command issuer
            if (member == null){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.PlayerNotFound")));
                return;
            }

            //Create a new trustlist 2 shorter than the old one to accommodate the removal
            long[] newtrusted = new long[trusted.length-2];

            //This should never be true as it indicates an invalid trust list, if it does happen, the chunk will just be unclaimed
            if (trusted.length % 2 != 0){
                container.remove(key);
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.AreaBroken")));
                return;
            }

            //Add the owner to the new trustlist
            newtrusted[0] = trusted[0];
            newtrusted[1] = trusted[1];

            int index = 2;

            //Loop through the old trustlist adding all members who aren't the one being removed
            for (int i = 2; i < trusted.length-1; i+=2){
                //Rebuild UUID from stored longs
                UUID temp = new UUID(trusted[i],trusted[i+1]);

                //Check if it isn't the member slated for removal
                if (!temp.equals(member.getUniqueId())){
                    //If we reached the end of the new list, then the intended player wasn't trusted in the first place, report success
                    if (index == newtrusted.length){
                        play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.TakeTrust")).replace("{ign}", member.getName()));
                        return;
                    }

                    //Add the trusted member to the new list
                    newtrusted[index] = trusted[i];
                    newtrusted[index+1] = trusted[i+1];
                    index+=2;
                }
            }

            //Save the new list and report success
            container.set(key,PersistentDataType.LONG_ARRAY,newtrusted);
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.TakeTrust")).replace("{ign}", member.getName()));
            return;
        }

        if (args[1].equalsIgnoreCase("togglepublic")){

            //Check if the area is claimed
            if (!container.has(key,PersistentDataType.LONG_ARRAY)){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.NotClaimed")));
                return;
            }

            //Retrieve the trust list
            long[] trusted = container.get(key,PersistentDataType.LONG_ARRAY);

            //This should never be true as it indicates an invalid trust list, if it does happen, the chunk will just be unclaimed
            if (trusted.length<2){
                container.remove(key);
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.AreaBroken")));
                return;
            }

            //Get the owner and check if the command issuer is them or using force
            UUID owner = new UUID(trusted[0],trusted[1]);
            if (!owner.equals(play.getUniqueId()) && !force){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.NotOwner")));
                return;
            }

            //Check if the area has public trust
            boolean publictrust = false;

            //Loop through the trust list skipping the first entry as it is the owner
            for (int i = 2; i < trusted.length-1; i+=2) {
                if (!publictrust) { //If not already found
                    //Rebuild UUID from longs
                    UUID temp = new UUID(trusted[i],trusted[i+1]);
                    //Check if it is the UUID representing public trust
                    publictrust = plugin.PublicTrust.equals(temp);
                }
            }
            if (publictrust){ //If the public is trusted remove it

                //Make a new list 2 shorter to accommodate public trust removal
                long[] newtrusted = new long[trusted.length-2];

                //This should never be true as it indicates an invalid trust list, if it does happen, the chunk will just be unclaimed
                if (trusted.length % 2 != 0){
                    container.remove(key);
                    play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.AreaBroken")));
                    return;
                }

                //Copy owner to new list
                newtrusted[0] = trusted[0];
                newtrusted[1] = trusted[1];

                int index = 2;

                //Loop through old list copying any who aren't public trust
                for (int i = 2; i < trusted.length-1; i+=2){
                    //Rebuild UUID from longs
                    UUID temp = new UUID(trusted[i],trusted[i+1]);

                    //Check if public trust
                    if (!temp.equals(plugin.PublicTrust)){

                        //This should never be true but left to avoid index out of bounds errors if data is corrupted
                        if (index == newtrusted.length){
                            return;
                        }

                        //Move the member to the new list
                        newtrusted[index] = trusted[i];
                        newtrusted[index+1] = trusted[i+1];
                        index+=2;
                    }
                }

                //Save the new list and inform of success
                container.set(key,PersistentDataType.LONG_ARRAY,newtrusted);
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.TakePublicTrust")));
                return;
            }

            //Add public trust
            //Create a new list 2 longer to accommodate public trust being added
            long[] newtrusted = new long[trusted.length+2];

            //Copy the old list to the new list
            for (int i = 0; i < trusted.length; i++) newtrusted[i] = trusted[i];

            //Add the UUID representing public trust
            newtrusted[newtrusted.length-2] = plugin.PublicTrust.getMostSignificantBits();
            newtrusted[newtrusted.length-1] = plugin.PublicTrust.getLeastSignificantBits();

            //Save the new list
            container.set(key,PersistentDataType.LONG_ARRAY,newtrusted);
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.GivePublicTrust")));
            return;
        }
    }
}
