package saber.mapcopyright.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TabComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        //This is the function that determines the tab able options when entering copyright command
        List<String> result = new ArrayList<>();
        if (args.length == 1){

            //If on the first args, add all possible first args to list
            result.add("create");
            result.add("delete");
            result.add("fulltrust");
            result.add("trust");
            result.add("untrust");
            result.add("area");
            result.add("togglepublic");
            result.add("info");
            result.add("give");
            result.add("help");

            //Remove any that don't match what the user has already typed
            result.removeIf(x -> !x.toLowerCase().startsWith(args[0].toLowerCase()));
        }
        else if (args.length == 2){
            if (args[0].equalsIgnoreCase("area")){

                //If on area, add all subcommands to list
                result.add("claim");
                result.add("unclaim");
                result.add("trust");
                result.add("untrust");
                result.add("info");
                result.add("togglepublic");

                //Remove any that don't match what the user has already typed
                result.removeIf(x -> !x.toLowerCase().startsWith(args[1].toLowerCase()));
            }

            if (args[0].equalsIgnoreCase("fulltrust")){
                result.add("add");
                result.add("remove");
                result.add("list");
                result.add("togglepublic");

                //Remove any that don't match what the user has already typed
                result.removeIf(x -> !x.toLowerCase().startsWith(args[1].toLowerCase()));
            }

            if (args[0].equalsIgnoreCase("trust") ||
                    args[0].equalsIgnoreCase("untrust") ||
                    args[0].equalsIgnoreCase("give")){

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
