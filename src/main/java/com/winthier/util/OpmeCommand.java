package com.winthier.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class OpmeCommand extends CommandBase
{
    private List<String> aliases = new ArrayList<String>();
    
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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
    {
        EntityPlayerMP player = sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null;
        if (player == null) {
            sender.addChatMessage(new TextComponentString("Player expected"));
            return;
        }
        UtilMod.getServer().getCommandManager().executeCommand(UtilMod.getServer(), "op " + UtilMod.getCommandSenderName(player));
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            return Perm.instance().playerHasPermission(player.getUniqueID(), "ftb.opme");
        }
        return false;
    }
}
