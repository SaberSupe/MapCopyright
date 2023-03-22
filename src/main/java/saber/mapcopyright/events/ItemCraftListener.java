package saber.mapcopyright.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import saber.mapcopyright.MapCopyright;
import saber.mapcopyright.utils.Copyright;

public class ItemCraftListener implements Listener {


    private final MapCopyright plugin;

    public ItemCraftListener(MapCopyright p1) {
        plugin = p1;
    }

    @EventHandler
    public void onCraft(CraftItemEvent e){

        //If they are making something other than a filled map, return
        if (e.getInventory().getResult().getType() != Material.FILLED_MAP) return;

        //Get the map data
        MapMeta mapmeta = (MapMeta) e.getInventory().getResult().getItemMeta();
        MapView mapview = mapmeta.getMapView();

        //If it isn't locked, return
        if (!mapview.isLocked()) return;

        int mapid = mapview.getId();

        //Get the associated map copyright
        Copyright copyr = plugin.getDataManager().getCopyright(mapid);

        //If not copyrighted return
        if (copyr == null) return;

        Player play = (Player) e.getWhoClicked();

        //If the player is the owner, trusted or fully trusted by owner return
        if (copyr.getOwner() == play.getUniqueId() || copyr.getMembers().contains(play.getUniqueId()) || plugin.getDataManager().isTrustAll(copyr.getOwner(), play.getUniqueId())) return;

        //If the map has public trust return
        if (copyr.getMembers().contains(plugin.PublicTrust) || plugin.getDataManager().isTrustAll(copyr.getOwner(),plugin.PublicTrust)) return;

        //Stop the player from copying the map and tell them
        e.setCancelled(true);
        play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.NoCopyPerms")));
    }

    @EventHandler
    public void onCartography(InventoryClickEvent e){
        //If they aren't clicking a cartography table, return
        if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CARTOGRAPHY) return;

        //If they aren't clicking the result of crafting, return
        if (e.getSlot() != 2) return;

        //Get the item they are crafting
        ItemStack map = e.getCurrentItem();

        //If it isn't a filled map return
        if (map == null || map.getType() != Material.FILLED_MAP) return;

        //Get the map data
        MapMeta mapmeta = (MapMeta) map.getItemMeta();
        MapView mapview = mapmeta.getMapView();

        //If the map isn't locked, and they aren't locking a map return
        if (!mapview.isLocked() && e.getClickedInventory().getItem(1).getType() != Material.GLASS_PANE) return;

        int mapid = mapview.getId();

        //Get the copyright associated with the map
        Copyright copyr = plugin.getDataManager().getCopyright(mapid);

        Player play = (Player) e.getWhoClicked();

        //If the copyright doesn't exist return
        if (copyr == null){
            //if they are locking a map, make a copyright
            if (e.getClickedInventory().getItem(1).getType() == Material.GLASS_PANE) plugin.getDataManager().addCopyright(plugin.getCurMapID(), e.getWhoClicked().getUniqueId());
            return;
        }

        //If they are owner, trusted or fully trusted by owner return
        if (copyr.getOwner().equals(play.getUniqueId()) || copyr.getMembers().contains(play.getUniqueId()) || plugin.getDataManager().isTrustAll(copyr.getOwner(), play.getUniqueId())) return;

        //If the map is public return
        if (copyr.getMembers().contains(plugin.PublicTrust) || plugin.getDataManager().isTrustAll(copyr.getOwner(),plugin.PublicTrust)) return;

        //Stop them from copying the map and tell them
        e.setCancelled(true);
        play.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg.NoCopyPerms")));
    }
}
