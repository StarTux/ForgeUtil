package com.winthier.util;

import com.winthier.connect.*;
import com.winthier.connect.packet.*;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.init.Blocks;

@Mod(modid = UtilMod.MODID, version = UtilMod.VERSION, acceptableRemoteVersions="*")
public class UtilMod extends AbstractConnectHandler
{
    public static final String MODID = "util";
    public static final String VERSION = "0.1";
    final Perm perm = new Perm(this);
    Connect connect = null;
    final List<ChatMessage> chatMessages = Collections.synchronizedList(new ArrayList<ChatMessage>());
    final List<Runnable> syncTasks = Collections.synchronizedList(new ArrayList<Runnable>());
    final List<ChannelCommand> chatChannels = Arrays.asList(
        new ChannelCommand("global", "g"),
        new ChannelCommand("admin", "a"),
        new ChannelCommand("mods", "mc"),
        new ChannelCommand("trusted", "tr")
        );
    final WhisperCommand whisperCommand = new WhisperCommand();
    final OnlineListCommand onlineListCommand = new OnlineListCommand();
    long ticks = 0;
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        FMLCommonHandler.instance().bus().register(new PlayerListener(this));
        connect = new Connect("ftb", new File("/home/mc/public/config/Connect/servers.txt"), this);
        connect.start();
        FMLCommonHandler.instance().bus().register(this);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        // event.registerServerCommand(new KitCommand());
        event.registerServerCommand(new OpmeCommand());
        for (ChannelCommand channel: chatChannels) {
            event.registerServerCommand(channel);
        }
        event.registerServerCommand(whisperCommand);
        event.registerServerCommand(onlineListCommand);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        ticks += 1;
        if (connect != null && ticks % 200 == 0) {
            connect.pingAll();
        }
        try {
            if (!chatMessages.isEmpty()) {
                synchronized(chatMessages) {
                    for (ChatMessage message: chatMessages) {
                        if (message.channel.equals("pm")) {
                            whisperCommand.whisper(message);
                        } else {
                            for (ChannelCommand channelCmd: chatChannels) {
                                if (channelCmd.name.equals(message.channel)) {
                                    channelCmd.message(message);
                                }
                            }
                        }
                    }
                    chatMessages.clear();
                }
            }
            if (!syncTasks.isEmpty()) {
                for (Runnable task: syncTasks) {
                    task.run();
                }
                syncTasks.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            chatMessages.clear();
        }
    }
    
    // ConnectHandler

    @Override
    public void runThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    List<OnlinePlayer> onlinePlayers() {
        List<OnlinePlayer> result = new ArrayList<OnlinePlayer>();
        for (Object o: MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (!(o instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer)o;
            OnlinePlayer onlinePlayer = new OnlinePlayer(player.getUniqueID(), player.getCommandSenderName());
            result.add(onlinePlayer);
        }
        return result;
    }

    @Override
    public void handleClientConnect(Client client) {
        System.out.println("[Connect] Client Connect: " + client.getName());
        final String clientName = client.getName();
        syncTasks.add(new Runnable() {
            @Override public void run() {
                connect.send(clientName, "Connect", PlayerList.Type.LIST.playerList(onlinePlayers()).serialize());
            }
        });
    }
    
    @Override
    public void handleClientDisconnect(Client client) {
        System.out.println("[Connect] Client Disconnect: " + client.getName());
    }

    @Override
    public void handleServerConnect(ServerConnection connection) {
        System.out.println("[Connect] Server Connect: " + connection.getName());
    }
    
    @Override
    public void handleServerDisconnect(ServerConnection connection) {
        System.out.println("[Connect] Server Disconnect: " + connection.getName());
    }

    @Override
    public void handleMessage(Message message) {
        try {
            if ("Chat".equals(message.getChannel())) {
                Map<String, Object> payload = (Map<String, Object>)message.getPayload();
                ChatMessage chatMessage = ChatMessage.deserialize(payload);
                chatMessages.add(chatMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleRemoteCommand(OnlinePlayer sender, String server, String[] args) {}

    static ChatMessage makeMessage(EntityPlayer player, String text) {
        ChatMessage message = new ChatMessage();
        if (player != null) {
            message.sender = player.getUniqueID();
            message.senderName = player.getCommandSenderName();
        }
        message.senderServer = "ftb";
        message.senderServerDisplayName = "FTB";
        message.message = text;
        return message;
    }
}
