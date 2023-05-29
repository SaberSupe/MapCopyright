package saber.mapcopyright.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import saber.mapcopyright.MapCopyright;

import java.util.ArrayList;
import java.util.List;

public class ItemFrameListener implements Listener {

    public MapCopyright instance;
    private final List<Location> dropLocations = new ArrayList<>();
    private final ItemStack invisibleItemFrame;

    public ItemFrameListener(MapCopyright instance){
        this.instance = instance;
        invisibleItemFrame = instance.getConfig().getItemStack("InvisibleItemFrame");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemFrameBreak(HangingBreakEvent event){
        Hanging hanging = event.getEntity();
        if (!hanging.getType().equals(EntityType.ITEM_FRAME)) return;
        ItemFrame frame = (ItemFrame) hanging;

        if (frame.isVisible() && !frame.isGlowing()) return;

        dropLocations.add(frame.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemFrameDrop(ItemSpawnEvent event){
        if (!event.getEntity().getItemStack().getType().equals(Material.ITEM_FRAME)) return;
        for (int i = 0; i < dropLocations.size(); i++){
            if (dropLocations.get(i).getBlockX() == event.getLocation().getBlockX() &&
                    dropLocations.get(i).getBlockY() == event.getLocation().getBlockY() &&
                    dropLocations.get(i).getBlockZ() == event.getLocation().getBlockZ()){
                event.getEntity().setItemStack(invisibleItemFrame.clone());
                dropLocations.remove(i);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemFramePlace(HangingPlaceEvent event){
        if (!event.getEntity().getType().equals(EntityType.ITEM_FRAME)) return;
        ItemFrame frame = (ItemFrame) event.getEntity();
        if (frame.isVisible()) return;
        frame.setVisible(true);
        frame.setGlowing(true);

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemFrameInteract(PlayerInteractEntityEvent event){
        if (!event.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) return;
        ItemFrame frame = (ItemFrame) event.getRightClicked();
        if (!frame.isGlowing() || event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.AIR)) return;
        frame.setGlowing(false);
        frame.setVisible(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemFrameDamage(EntityDamageEvent event){
        if (!event.getEntity().getType().equals(EntityType.ITEM_FRAME)) return;
        ItemFrame frame = (ItemFrame) event.getEntity();
        if (frame.isVisible()) return;
        frame.setVisible(true);
        frame.setGlowing(true);
    }
}
