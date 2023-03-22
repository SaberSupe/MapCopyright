package saber.mapcopyright.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import saber.mapcopyright.MapCopyright;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MapCreationListener implements Listener {

    private final MapCopyright plugin;
    private final NamespacedKey key;
    public MapCreationListener(MapCopyright p1){
        plugin = p1;
        key = new NamespacedKey(plugin, "copyright");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){

        //If they aren't right-clicking while holding a map, return
        if (!e.hasItem()) return;
        if (!(e.getItem().getType() == Material.MAP)) return;
        if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        Player play = e.getPlayer();

        //Get the top left chunk of the map area
        int chunkx = play.getLocation().getBlockX() - 64;
        int chunkz = play.getLocation().getBlockZ() - 64;

        chunkx = chunkx - (chunkx % (16*8)) + 64;
        chunkz = chunkz - (chunkz % (16*8)) + 64;

        //Get the persistent data container for that chunk
        PersistentDataContainer container = play.getWorld().getChunkAt(chunkx, chunkz).getPersistentDataContainer();

        //Check for an area trust list, return if it doesn't exist
        if (!container.has(key, PersistentDataType.LONG_ARRAY)) return;

        //Get the trust list
        long[] trusted;
        trusted = container.get(key, PersistentDataType.LONG_ARRAY);

        //Rebuild UUID list using stored long array
        List<UUID> members = new ArrayList<>();
        for (int i = 0; i < trusted.length-1; i+=2) members.add(new UUID(trusted[i],trusted[i+1]));

        //If the player is the owner, trusted or Fully trusted by the owner return
        if (trusted.length == 0 || members.contains(e.getPlayer().getUniqueId()) || plugin.getDataManager().isTrustAll(members.get(0),e.getPlayer().getUniqueId())) return;

        //If the area is public
        if (members.contains(plugin.PublicTrust) || plugin.getDataManager().isTrustAll(members.get(0),plugin.PublicTrust)) return;

        //Stop them from filling a map and inform them
        e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.Area.NoPerms")));
        e.setCancelled(true);
    }
}
