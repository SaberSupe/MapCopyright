package saber.mapcopyright.utils;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Copyright {
    private int mapid;
    private UUID own;
    private List<UUID> mem = new ArrayList<UUID>();


    public Copyright(int Map_Id, UUID Owner, List<UUID> Members){
        mapid = Map_Id;
        own = Owner;
        if (Members != null) mem.addAll(Members);
    }

    @Override
    public String toString() {

        //Converts the copyright into a string, was originally intended for flat file storage, is now not used
        String value = String.valueOf(mapid) + ":" + own.toString();

        if (mem == null) return value;

        for (UUID x : mem) value = value + ":" + x.toString();

        return value;
    }

    public int getMapID(){
        return mapid;
    }

    public UUID getOwner(){
        return own;
    }

    public List<UUID> getMembers(){
        return mem;
    }

    public void addMember(UUID Member){
        mem.add(Member);
    }

    public void setOwner(UUID Owner){
        own = Owner;
    }

    public void remMember(UUID member){ mem.remove(member); }
}
