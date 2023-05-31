package saber.mapcopyright.listeners;

import net.kyori.adventure.text.Component;
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
import org.bukkit.inventory.meta.ItemMeta;
import saber.mapcopyright.MapCopyright;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemFrameListener implements Listener {

    public MapCopyright instance;
    private final HashMap<Location,Component> dropLocations = new HashMap<>();
    private final ItemStack invisibleItemFrame;

    public ItemFrameListener(MapCopyright instance){
        this.instance = instance;
        invisibleItemFrame = instance.getConfig().getItemStack("InvisibleItemFrame");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemFrameBreak(HangingBreakEvent event){
        Hanging hanging = event.getEntity();
        if (!hanging.getType().equals(EntityType.ITEM_FRAME)) return;
        ItemFrame frame = (ItemFrame) hanging;

        if (frame.isVisible() && !frame.isGlowing()) return;

        dropLocations.put(frame.getLocation(),frame.customName());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemFrameDrop(ItemSpawnEvent event){
        if (!event.getEntity().getItemStack().getType().equals(Material.ITEM_FRAME)) return;
        Location dropPlace = null;
        for (Location x : dropLocations.keySet()){
            if (x.getBlockX() == event.getLocation().getBlockX() &&
                    x.getBlockY() == event.getLocation().getBlockY() &&
                    x.getBlockZ() == event.getLocation().getBlockZ()){

                dropPlace = x;

                ItemStack invisFrame = invisibleItemFrame.clone();
                ItemMeta invisMeta = invisFrame.getItemMeta();
                invisMeta.displayName(dropLocations.get(x));
                invisFrame.setItemMeta(invisMeta);
                event.getEntity().setItemStack(invisFrame);
                break;
            }
        }
        if (dropPlace != null) dropLocations.remove(dropPlace);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemFramePlace(HangingPlaceEvent event){
        if (!event.getEntity().getType().equals(EntityType.ITEM_FRAME)) return;
        ItemFrame frame = (ItemFrame) event.getEntity();
        if (frame.isVisible()) return;

        ItemStack frameItem = event.getItemStack();
        if (frameItem != null && frameItem.hasItemMeta() && frameItem.getItemMeta().hasDisplayName()) frame.customName(frameItem.getItemMeta().displayName());

        frame.setVisible(true);
        frame.setGlowing(true);

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemFrameInteract(PlayerInteractEntityEvent event){
        if (!event.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) return;
        ItemFrame frame = (ItemFrame) event.getRightClicked();
        if (!frame.isGlowing() || event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.AIR)) return;
        frame.setGlowing(false);
        frame.setVisible(false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemFrameDamage(EntityDamageEvent event){
        if (!event.getEntity().getType().equals(EntityType.ITEM_FRAME)) return;
        ItemFrame frame = (ItemFrame) event.getEntity();
        if (frame.isVisible()) return;
        frame.setVisible(true);
        frame.setGlowing(true);
    }
}
