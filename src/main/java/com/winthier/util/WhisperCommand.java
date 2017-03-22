package com.winthier.util;

import com.winthier.connect.Connect;
import com.winthier.connect.OnlinePlayer;
import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class WhisperCommand extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "tell";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/tell <player> <message...>";
    }

    @Override
    public List getCommandAliases()
    {
        return Arrays.asList(
            "msg",
            "whisper",
            "w",
            "pm"
            );
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null;
        if (player == null) {
            sender.addChatMessage(new TextComponentString("Player expected"));
            return;
        }
        if (args.length < 2) return;
        StringBuilder sb = new StringBuilder(args[1]);
        for (int i = 2; i < args.length; ++i) sb.append(" ").append(args[i]);
        ChatMessage message = UtilMod.makeMessage(player, sb.toString());
        message.channel = "pm";
        String targetName = args[0];
        OnlinePlayer op = Connect.getInstance().findOnlinePlayer(targetName);
        if (op == null) {
            sender.addChatMessage(new TextComponentString("Player not found: " + targetName));
            return;
        }
        message.target = op.getUuid();
        message.targetName = op.getName();
        Connect.getInstance().broadcastAll("Chat", message.serialize());
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            return Perm.instance().playerHasPermission(player.getUniqueID(), "chat.pm");
        }
        return false;
    }

    EntityPlayer getPlayerForUsername(String name) {
        for (Object o: UtilMod.getServer().getPlayerList().getPlayerList()) {
            if (!(o instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer)o;
            if (UtilMod.getCommandSenderName(p).equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    String colorizeMessage(String message) {
        String[] words = message.split(" ");
        if (words.length == 0) return "";
        String color = "§d";
        StringBuilder sb = new StringBuilder(words[0]);
        for (int i = 1; i < words.length; ++i) {
            sb.append(" ");
            sb.append(color);
            sb.append(words[i]);
        }
        return sb.toString();
    }

    void whisper(ChatMessage message) {
        boolean ack = message.special != null;
        if (!ack) {
            System.out.println(String.format("[Chat] %s -> %s: %s", message.senderName, message.targetName, message.message));
        }
        EntityPlayer player = getPlayerForUsername(message.targetName);
        if (player == null) return;
        String msg = colorizeMessage(message.message);
        if (ack) {
            UtilMod.addChatMessage(player, String.format("§dTo %s(%s): %s", message.senderName, message.senderServerDisplayName, msg));
        } else {
            UtilMod.addChatMessage(player,String.format("§dFrom %s(%s): %s", message.senderName, message.senderServerDisplayName, msg));
            sendAck(message, player);
        }
    }

    void sendAck(ChatMessage message, EntityPlayer player) {
        message.special = "Ack";
        message.target = message.sender;
        message.targetName = message.senderName;
        message.sender = player.getUniqueID();
        message.senderName = UtilMod.getCommandSenderName(player);
        message.senderTitle = null;
        message.senderTitleDescription = null;
        message.senderServer = "ftb";
        message.senderServerDisplayName = "FTB";
        Connect.getInstance().broadcastAll("Chat", message.serialize());
    }
}
