# MapCopyright
Spigot plugin to add copyright functionality to maps
\
Allows users to copyright maps and control who can and cannot make copies\
Usage:\
/copyright create - copyrights are automatically created when a map is locked, this command is for ones created before this plugin was added or for maps who have had their copyrights removed\
/copyright delete - delete the copyright\
/copyright trust {ign} - trust someone to copy the map\
/copyright untrust {ign} - untrust someone\
/copyright togglepublic - sets the held map to public access\
/copyright give {ign} - give ownership of the copyright\
/copyright info - list the current owner and trusted members of a copyright\
\
/copyright area {subcommand} - Manages map grid slots to stop others from making new maps of areas\
Area subcommands:
- Claim - Claim the 8x8 chunk map area you are currently standing in
- Unclaim - unclaim the area you are standing in
- Trust {ign} - trust the player to make maps in the area
- Untrust {ign} - untrust the player to make maps in the area
- togglepublic - toggle public access to the area
- Info - get the area ownership info\
\
/copyright FullTrust {subcommand} - Allows you to manage a list of players who have full access to all your copyrights and areas\
FullTrust Subcommands:
- Add - Add someone to your full trust list
- Remove - Remove someone from your full trust list
- List - Show your full trust list
- TogglePublic - Toggle whether the public has full trust access\
\
Permissions:\
mapcopyright.copyright - access to use /copyright command (defaults true)\
mapcopyright.force - players with this permission can add "force" to the end of any copyright command to skip the ownership check allowing full access to change copyrights and areas (defaults op)
