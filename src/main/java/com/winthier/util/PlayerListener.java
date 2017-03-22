package com.winthier.util;

import com.winthier.connect.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.UUID;

public class PlayerListener
{
    final UtilMod mod;
    
    PlayerListener(UtilMod mod) {
        this.mod = mod;
    }
    
    @SubscribeEvent(priority= EventPriority.HIGHEST, receiveCanceled=true)
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        UUID uuid = event.player.getUniqueID();
        String name = UtilMod.getCommandSenderName(event.player);
        if(!Perm.instance().playerHasPermission(uuid, "graylist.login.ftb")){
            ((EntityPlayerMP)event.player).connection
                    .kickPlayerFromServer("You must be of Member rank on the main server or " +
                            "get someone to vouch for you in order to join this server.");
            return;
        }
        Connect.getInstance().broadcastPlayerStatus(new OnlinePlayer(uuid, name), true);
        mod.onlineListCommand.showOnlineList(event.player);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST, receiveCanceled=true)
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.player.getUniqueID();
        String name = UtilMod.getCommandSenderName(event.player);
        mod.perm.clearPlayer(uuid);
        Connect.getInstance().broadcastPlayerStatus(new OnlinePlayer(uuid, name), false);
    }
}
