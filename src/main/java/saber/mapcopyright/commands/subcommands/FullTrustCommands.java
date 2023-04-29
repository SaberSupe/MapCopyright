package saber.mapcopyright.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import saber.mapcopyright.MapCopyright;

import java.util.List;
import java.util.UUID;

public class FullTrustCommands {

    public static void fullTrustCommand(Player play, String[] args, MapCopyright instance){
        if (args[1].equalsIgnoreCase("add")){

            //Check if they entered an ign
            if (args.length == 2) {
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("msg.PlayerNotFound")));
                return;
            }

            //Check if the entered ign can be found
            Player trustee = Bukkit.getPlayer(args[2]);
            if (trustee == null){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("msg.PlayerNotFound")));
                return;
            }

            //Give the entered player trustall for the command issuer
            instance.getDataManager().addTrustAll(play.getUniqueId(), trustee.getUniqueId());
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("msg.GiveFullTrust")).replace("{ign}", trustee.getName()));
            return;
        }

        if (args[1].equalsIgnoreCase("remove")){

            //Check if they entered an ign
            if (args.length == 2) {
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("msg.PlayerNotFound")));
                return;
            }

            //Check if the entered ign can be found
            OfflinePlayer trustee = Bukkit.getPlayer(args[2]);

            //Check trusted players if not found online
            if (trustee == null){
                for (UUID x : instance.getDataManager().getTrustAll(play.getUniqueId())){
                    if (Bukkit.getOfflinePlayer(x).getName() != null && Bukkit.getOfflinePlayer(x).getName().equalsIgnoreCase(args[2])){
                        trustee = Bukkit.getOfflinePlayer(x);
                        break;
                    }
                }
            }

            //If player not found
            if (trustee == null){
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("msg.PlayerNotFound")));
                return;
            }

            //Remove trustall from the entered player for the command issuer
            instance.getDataManager().remTrustAll(play.getUniqueId(), trustee.getUniqueId());
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("msg.TakeFullTrust")).replace("{ign}", trustee.getName()));
            return;
        }

        if (args[1].equalsIgnoreCase("list")){
            //Collect full trust list and put members in comma seperated string
            boolean publictrust = false;
            String trusted = "";
            for (UUID x : instance.getDataManager().getTrustAll(play.getUniqueId())) {
                if (instance.PublicTrust.equals(x)) publictrust = true;
                else trusted = trusted + ", " + Bukkit.getOfflinePlayer(x).getName();
            }

            //Send full trust info to player
            trusted = trusted.replaceFirst(", ","");
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("msg.FullTrustList.Owner")).replace("{ign}", play.getName()));
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("msg.FullTrustList.List")).replace("{trustlist}", trusted));
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("msg.FullTrustList.Public")).replace("{public}", String.valueOf(publictrust)));
            return;
        }

        if (args[1].equalsIgnoreCase("togglepublic")){

            //If public trust, remove it
            if (instance.getDataManager().isTrustAll(play.getUniqueId(),instance.PublicTrust)){
                instance.getDataManager().remTrustAll(play.getUniqueId(), instance.PublicTrust);
                play.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("msg.TakeFullTrustPublic")));
                return;
            }

            //Else add it
            instance.getDataManager().addTrustAll(play.getUniqueId(), instance.PublicTrust);
            play.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("msg.GiveFullTrustPublic")));
            return;
        }

        play.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("msg.InvalidSubCommand")));
    }
}
