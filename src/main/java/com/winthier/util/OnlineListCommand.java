package com.winthier.util;

import com.winthier.connect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;

public class OnlineListCommand extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "list";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/list <message...>";
    }

    @Override
    public List getCommandAliases()
    {
        return Arrays.asList("who", "online", "ls");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        showOnlineList(sender);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    void showOnlineList(ICommandSender sender)
    {
        Map<String, List<OnlinePlayer>> serverList = new HashMap<String, List<OnlinePlayer>>();
        int totalCount = 0;
        for (ServerConnection con: new ArrayList<ServerConnection>(Connect.getInstance().getServer().getConnections())) {
            List<OnlinePlayer> conList = new ArrayList<OnlinePlayer>(con.getOnlinePlayers());
            String displayName = con.getName();
            Client client = Connect.getInstance().getClient(displayName);
            if (client != null) displayName = client.getDisplayName();
            List<OnlinePlayer> playerList = serverList.get(displayName);
            if (playerList == null) {
                playerList = new ArrayList<OnlinePlayer>();
                serverList.put(displayName, playerList);
            }
            playerList.addAll(conList);
            totalCount += conList.size();
        }
        String[] serverNames = serverList.keySet().toArray(new String[0]);
        Arrays.sort(serverNames);
        sender.addChatMessage(new ChatComponentText("§3§lPlayer List§r §3(§r"+totalCount+"§3)"));
        for (String serverName: serverNames) {
            OnlinePlayer[] playerArray = serverList.get(serverName).toArray(new OnlinePlayer[0]);
            if (playerArray.length == 0) continue;
            Arrays.sort(playerArray, new Comparator<OnlinePlayer>() {
                @Override public int compare(OnlinePlayer a, OnlinePlayer b) { return String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName()); }
                @Override public boolean equals(Object o) { return this == o; }
            });
            StringBuilder sb = new StringBuilder(" §3"+serverName+"(§r"+playerArray.length+"§3)");
            for (OnlinePlayer player: playerArray) {
                sb.append("§r " + player.getName());
            }
            sender.addChatMessage(new ChatComponentText(sb.toString()));
        }
    }
}
