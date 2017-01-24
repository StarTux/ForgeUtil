package com.winthier.util;

import com.winthier.connect.*;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import java.util.UUID;

public class PlayerListener
{
    final UtilMod mod;
    
    PlayerListener(UtilMod mod) {
        this.mod = mod;
    }
    
    @SubscribeEvent(priority=EventPriority.HIGHEST, receiveCanceled=true)
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        UUID uuid = event.player.getUniqueID();
        String name = event.player.getCommandSenderName();
        Connect.getInstance().broadcastPlayerStatus(new OnlinePlayer(uuid, name), true);
        mod.onlineListCommand.showOnlineList(event.player);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST, receiveCanceled=true)
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.player.getUniqueID();
        String name = event.player.getCommandSenderName();
        mod.perm.clearPlayer(uuid);
        Connect.getInstance().broadcastPlayerStatus(new OnlinePlayer(uuid, name), false);
    }
}
