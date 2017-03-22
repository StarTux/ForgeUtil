package com.winthier.util;

import com.winthier.connect.Connect;

import java.util.*;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class ChannelCommand extends CommandBase
{
    String name;
    String shortcut;

    ChannelCommand(String name, String shortcut) {
        this.name = name;
        this.shortcut = shortcut;
    }

    @Override
    public String getCommandName()
    {
        return "ch" + shortcut;
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/" + shortcut + " <message...>";
    }

    @Override
    public List getCommandAliases()
    {
        return Arrays.asList(name, shortcut, "ch"+shortcut);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null;
        if (player == null) {
            sender.addChatMessage(new TextComponentString("Player expected"));
            return;
        }
        if (args.length == 0) return;
        StringBuilder sb = new StringBuilder(args[0]);
        for (int i = 1; i < args.length; ++i) sb.append(" ").append(args[i]);
        ChatMessage message = UtilMod.makeMessage(player, sb.toString());
        message.channel = name;
        Connect.getInstance().broadcastAll("Chat", message.serialize());
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            return Perm.instance().playerHasPermission(player.getUniqueID(), "chat.channel." + name);
        }
        return false;
    }

    String getFormatString() {
        if (false) {
            return "";
        } else if ("g".equals(shortcut)) {
            return "[§3G§r]§7%s§r: %s";
        } else if ("a".equals(shortcut)) {
            return "§b{§r%s§b} %s";
        } else if ("mc".equals(shortcut)) {
            return "§c%s§7: %s";
        } else if ("tr".equals(shortcut)) {
            return "§9%s§r: §6%s";
        } else {
            return "[?]%s: %s";
        }
    }

    String getColorString() {
        if (false) {
            return "";
        } else if ("a".equals(shortcut)) {
            return "§b";
        } else if ("mc".equals(shortcut)) {
            return "§7";
        } else if ("tr".equals(shortcut)) {
            return "§6";
        } else {
            return "";
        }
    }

    String colorizeMessage(String message) {
        String[] words = message.split(" ");
        if (words.length == 0) return "";
        String color = getColorString();
        StringBuilder sb = new StringBuilder(words[0]);
        for (int i = 1; i < words.length; ++i) {
            sb.append(" ");
            sb.append(color);
            sb.append(words[i]);
        }
        return sb.toString();
    }

    void message(ChatMessage message) {
        System.out.println(String.format("[Chat] [%s]%s: %s", name, message.senderName, message.message));
        String msg = colorizeMessage(message.message);
        msg = String.format(getFormatString(), message.senderName, msg);
        for (Object o: UtilMod.getServer().getPlayerList().getPlayerList()) {
            if (!(o instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer)o;

            //TODO: fix mod chat permission
            if (!Perm.instance().playerHasPermission(player.getUniqueID(), "chat.channel." + name)) continue;
            UtilMod.addChatMessage(player, msg);
        }
    }
}
