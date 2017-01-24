package com.winthier.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;

public class OpmeCommand extends CommandBase
{
    private List aliases;
    
    @Override
    public String getCommandName()
    {
        return "opme";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "opme";
    }

    @Override
    public List getCommandAliases()
    {
        return this.aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        EntityPlayerMP player = sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null;
        if (player == null) {
            sender.addChatMessage(new ChatComponentText("Player expected"));
            return;
        }
        MinecraftServer.getServer().getCommandManager().executeCommand(MinecraftServer.getServer(), "op " + player.getCommandSenderName());
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            return Perm.instance().playerHasPermission(player.getUniqueID(), "ftb.opme");
        }
        return false;
    }
}
