package net.yorunina.maa.model;

public interface IPlayer {
    boolean shouldKeepInventory();

    void setKeepInventory(boolean bypass);

    boolean canMapTeleport();

    void setMapTeleportBypass(boolean bypass);
}