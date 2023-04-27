package saber.mapcopyright.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import saber.mapcopyright.MapCopyright;
import saber.mapcopyright.commands.subcommands.AreaCommands;
import saber.mapcopyright.commands.subcommands.FullTrustCommands;
import saber.mapcopyright.utils.Copyright;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CopyrightCommand implements CommandExecutor {

    private final MapCopyright plugin;
    private final NamespacedKey key;
    private final String[] SubCommands = {"area", "create", "delete", "fulltrust", "give", "help", "info", "togglepublic", "trust", "untrust"};

    public CopyrightCommand(MapCopyright p1){

        plugin = p1;
        key = new NamespacedKey(plugin, "copyright");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

        if (!cmd.getName().equalsIgnoreCase("copyright")) return true;

        //Check Perms
        if (!sender.hasPermission("mapcopyright.copyright")){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.NoCommandPerms")));
            return true;
        }

        //Check if player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.NotPlayer")));
            return true;
        }

        //Check that they entered a subcommand
        if (args.length == 0 || !Arrays.asList(SubCommands).contains(args[0].toLowerCase())) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.InvalidSubCommand")));
            return true;
        }

        if (args[0].equalsIgnoreCase("Help") || args[0].equalsIgnoreCase("?")){
            int pageNumber = 1;
            if (args.length > 1 && args[1].matches("-?\\d+")) pageNumber = Integer.parseInt(args[1]);
            List<String> helpPage = plugin.getConfig().getStringList("msg.Help." + pageNumber);
            if (helpPage.isEmpty()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("msg.Help.PageNotFound").replace("{pagenumber}", args[1])));
                return true;
            }
            for (String x : helpPage) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', x));
            return true;
        }

        Player play = (Player) sender;

        //Check if the player entered force and if they have the perms to use force
        boolean force = args[args.length - 1].equalsIgnoreCase("force") && sender.hasPermission("mapcopyright.force");

        if (args[0].equalsIgnoreCase("FullTrust")){
            //Process area command
            FullTrustCommands.fullTrustCommand(play,args,plugin);
            return true;
        }


        if (args[0].equalsIgnoreCase("Area")){
            //Process area command
            AreaCommands.areaCommand(play,key,args,force,plugin);
            return true;
        }

        //Get the item they are holding
        ItemStack itemmap = play.getInventory().getItemInMainHand();

        //Check if it is a filled map
        if (itemmap.getType() != Material.FILLED_MAP){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.HoldingLockedMap")));
            return true;
        }

        //Get the map data
        MapMeta mapmeta = (MapMeta) itemmap.getItemMeta();
        MapView mapview = mapmeta.getMapView();

        //Check if the map is locked
        if (!mapview.isLocked()){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.HoldingLockedMap")));
            return true;
        }

        int mapid = mapview.getId();

        if (args[0].equalsIgnoreCase("create")) {

            //Attempt to add the copyright, will return false if it already exists
            if (plugin.getDataManager().addCopyright(mapid,play.getUniqueId())){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.AddCopyright")));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.CopyrightAlreadyExists")));
            }
            return true;
        }

        //Get the copyright associated with the map and check if it exists
        Copyright copyr = plugin.getDataManager().getCopyright(mapid);
        if (copyr == null){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.MapNotCopyrighted")));
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            //Send messages showing the info from the copyright
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Info.MapID")).replace("{mapid}", String.valueOf(mapid)));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Info.Owner")).replace("{ign}", Bukkit.getOfflinePlayer(copyr.getOwner()).getName()));

            //Loop through trusted members making comma seperated list and checking for public UUID
            String members = "";
            boolean publictrust = false;
            for (UUID x : copyr.getMembers()){
                if (plugin.PublicTrust.equals(x)) publictrust = true;
                else members = members + ", " + Bukkit.getOfflinePlayer(x).getName();
            }

            //Remove leading comma and send results to player
            members = members.replaceFirst(", ","");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Info.TrustList")).replace("{trustlist}", members));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Info.Public")).replace("{public}", String.valueOf(publictrust)));
            return true;
        }

        if (args[0].equalsIgnoreCase("trust")) {

            //Check if they are the copyright owner or using force
            if (!copyr.getOwner().equals(play.getUniqueId()) && !force){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.NotOwner")));
                return true;
            }

            //Check if they entered an ign
            if (args.length == 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.PlayerNotFound")));
                return true;
            }

            //Check if the entered ign can be found
            Player member = Bukkit.getPlayer(args[1]);
            if (member == null){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.PlayerNotFound")));
                return true;
            }

            //Add the player to the trust list and inform the command issuer
            plugin.getDataManager().addMember(mapid,member.getUniqueId());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.GiveTrust")).replace("{ign}", member.getName()));
            return true;
        }

        if (args[0].equalsIgnoreCase("untrust")) {

            //Check if they are the copyright owner or using force
            if (!copyr.getOwner().equals(play.getUniqueId()) && !force){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.NotOwner")));
                return true;
            }

            //Check if they entered an ign
            if (args.length == 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.PlayerNotFound")));
                return true;
            }

            //Get the player
            OfflinePlayer member = Bukkit.getPlayer(args[1]);

            //if not found look at trusted players
            if (member == null) {
                //Check if any trusted players have that name
                for (UUID x : copyr.getMembers()) {
                    if (Bukkit.getOfflinePlayer(x).getName() != null && Bukkit.getOfflinePlayer(x).getName().equalsIgnoreCase(args[1])) {
                        member = Bukkit.getOfflinePlayer(x);
                        break;
                    }
                }
            }

            //If not found, inform command issuer
            if (member == null){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.PlayerNotFound")));
                return true;
            }

            //Remove the player form the trust list and inform the command issuer
            plugin.getDataManager().remMember(mapid,member.getUniqueId());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.TakeTrust")).replace("{ign}", member.getName()));
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")){

            //Check if they are the copyright owner or using force
            if (!copyr.getOwner().equals(play.getUniqueId()) && !force){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.NotOwner")));
                return true;
            }

            //Delete the copyright and inform the command issuer
            plugin.getDataManager().remCopyright(mapid);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.DeleteCopyright")));
            return true;
        }

        if (args[0].equalsIgnoreCase("give")){

            //Check if they are the copyright owner or using force
            if (!copyr.getOwner().equals(play.getUniqueId()) && !force){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.NotOwner")));
                return true;
            }

            //Check if they entered an ign
            if (args.length == 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.PlayerNotFound")));
                return true;
            }

            //Check if the entered ign can be found
            Player newowner = Bukkit.getPlayer(args[1]);
            if (newowner == null){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.PlayerNotFound")));
                return true;
            }

            //Change the owner on the copy right and inform the command issuer
            plugin.getDataManager().giveCopyright(mapid, newowner.getUniqueId());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.GiveOwner")).replace("{ign}", newowner.getName()));
            return true;
        }

        if (args[0].equalsIgnoreCase("togglepublic")) {

            //Check if they are the copyright owner or using force
            if (!copyr.getOwner().equals(play.getUniqueId()) && !force){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.NotOwner")));
                return true;
            }

            //Check if the copyright has the UUID associated with public trust
            if (copyr.getMembers().contains(plugin.PublicTrust)){

                //If so remove it
                plugin.getDataManager().remMember(mapid,plugin.PublicTrust);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.TakePublicTrust")));
                return true;
            }

            //If not, add it
            plugin.getDataManager().addMember(mapid,plugin.PublicTrust);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.GivePublicTrust")));
            return true;
        }

        //tell the command issuer that no matching sub command was found
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.InvalidSubCommand")));
        return true;
    }


}
