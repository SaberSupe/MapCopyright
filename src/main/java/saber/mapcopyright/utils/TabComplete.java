package saber.mapcopyright.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TabComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        //This is the function that determines the tab able options when entering copyright command
        List<String> result = new ArrayList<>();
        if (args.length == 1){

            //If on the first args, add all possible first args to list
            result.add("Create");
            result.add("Delete");
            result.add("FullTrust");
            result.add("Trust");
            result.add("Untrust");
            result.add("Area");
            result.add("TogglePublic");
            result.add("Info");
            result.add("Give");

            //Remove any that don't match what the user has already typed
            result.removeIf(x -> !x.toLowerCase().startsWith(args[0].toLowerCase()));
        }
        else if (args.length == 2){
            if (args[0].equalsIgnoreCase("area")){

                //If on area, add all subcommands to list
                result.add("Claim");
                result.add("Unclaim");
                result.add("Trust");
                result.add("Untrust");
                result.add("Info");
                result.add("TogglePublic");

                //Remove any that don't match what the user has already typed
                result.removeIf(x -> !x.toLowerCase().startsWith(args[1].toLowerCase()));
            }

            if (args[0].equalsIgnoreCase("fulltrust")){
                result.add("Add");
                result.add("Remove");
                result.add("List");
                result.add("TogglePublic");

                //Remove any that don't match what the user has already typed
                result.removeIf(x -> !x.toLowerCase().startsWith(args[1].toLowerCase()));
            }

            if (args[0].equalsIgnoreCase("Trust") ||
                    args[0].equalsIgnoreCase("Untrust") ||
                    args[0].equalsIgnoreCase("Give")){

                //returning null to make the tab list the players list
                return null;
            }
        }
        else if (args.length == 3){
            if (args[0].equalsIgnoreCase("area") && (args[1].equalsIgnoreCase("trust") || args[1].equalsIgnoreCase("untrust"))){
                //returning null to make the tab list the players list
                return null;
            }
            if (args[0].equalsIgnoreCase("fulltrust") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))){
                //returning null to make the tab list the players list
                return null;
            }
        }
        return result;
    }
}
