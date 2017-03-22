package com.winthier.util;

import com.winthier.connect.*;
import com.winthier.connect.packet.*;
import java.io.File;
import java.util.*;

import ibxm.Player;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
    static final Map<String, String> colorStrings = new HashMap<String, String>();

    static{
        colorStrings.put("§0", TextFormatting.BLACK.toString());
        colorStrings.put("§1", TextFormatting.DARK_BLUE.toString());
        colorStrings.put("§2", TextFormatting.DARK_GREEN.toString());
        colorStrings.put("§3", TextFormatting.DARK_AQUA.toString());
        colorStrings.put("§4", TextFormatting.DARK_RED.toString());
        colorStrings.put("§5", TextFormatting.DARK_PURPLE.toString());
        colorStrings.put("§6", TextFormatting.GOLD.toString());
        colorStrings.put("§7", TextFormatting.GRAY.toString());
        colorStrings.put("§8", TextFormatting.DARK_GRAY.toString());
        colorStrings.put("§9", TextFormatting.BLUE.toString());
        colorStrings.put("§a", TextFormatting.GREEN.toString());
        colorStrings.put("§b", TextFormatting.AQUA.toString());
        colorStrings.put("§c", TextFormatting.RED.toString());
        colorStrings.put("§d", TextFormatting.LIGHT_PURPLE.toString());
        colorStrings.put("§e", TextFormatting.YELLOW.toString());
        colorStrings.put("§f", TextFormatting.WHITE.toString());
        colorStrings.put("§k", TextFormatting.OBFUSCATED.toString());
        colorStrings.put("§l", TextFormatting.BOLD.toString());
        colorStrings.put("§m", TextFormatting.STRIKETHROUGH.toString());
        colorStrings.put("§n", TextFormatting.UNDERLINE.toString());
        colorStrings.put("§o", TextFormatting.ITALIC.toString());
        colorStrings.put("§r", TextFormatting.RESET.toString());
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new PlayerListener(this));
        connect = new Connect("ftb", new File("/home/mc/public/config/Connect/servers.txt"), this);
        connect.start();
        MinecraftForge.EVENT_BUS.register(this);
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
        for (Object o: getServer().getPlayerList().getPlayerList()) {
            if (!(o instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer)o;
            OnlinePlayer onlinePlayer = new OnlinePlayer(player.getUniqueID(), player.getCommandSenderEntity().getName());
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
            message.senderName = player.getCommandSenderEntity().getName();
        }
        message.senderServer = "ftb";
        message.senderServerDisplayName = "FTB";
        message.message = text;
        return message;
    }

    static String getCommandSenderName(EntityPlayer player){
        return player.getCommandSenderEntity().getName();
    }

    static MinecraftServer getServer(){
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    static String colorConvert(String message){
        for(String key : colorStrings.keySet()){
            message = message.replaceAll(key, colorStrings.get(key));
        }
        return message;
    }

    static void addChatMessage(ICommandSender receiver, String message){
        message = colorConvert(message);
        receiver.addChatMessage(new TextComponentString(message));
    }
}
