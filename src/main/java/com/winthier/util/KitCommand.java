package com.winthier.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class KitCommand implements ICommand
{
    private List aliases;
    final Map<UUID, Long> lastUses = new HashMap<UUID, Long>();
    
    @Override
    public String getCommandName()
    {
        return "kit";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "kit";
    }

    @Override
    public List getCommandAliases()
    {
        return this.aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        // Check syntax
        if (args.length != 0) {
            usage(sender);
            return;
        }
        // Fetch player
        EntityPlayerMP player = sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null;
        if (player == null) {
            sender.addChatMessage(new ChatComponentText("Player expected"));
            return;
        }
        // Get and set last use
        UUID uuid = player.getUniqueID();
        Long lastUse = lastUses.get(uuid);
        if (lastUse != null) {
            long dist = System.currentTimeMillis() - lastUse;
            dist /= 1000;
            if (dist < 60 * 60 * 24) {
                sender.addChatMessage(new ChatComponentText("You are still on cooldown."));
                return;
            }
        }
        lastUses.put(uuid, System.currentTimeMillis());
        // Give items
        String name = player.getCommandSenderName();
        consoleCommand("give " + name + " minecraft:stone_pickaxe");
        consoleCommand("give " + name + " minecraft:stone_sword");
        consoleCommand("give " + name + " minecraft:planks 32");
        consoleCommand("give " + name + " minecraft:bread 32");
        consoleCommand("give " + name + " minecraft:torch 32");
        sender.addChatMessage(new ChatComponentText("Enjoy your starter kit!"));
    }

    void consoleCommand(String cmd) {
        MinecraftServer.getServer().getCommandManager().executeCommand(MinecraftServer.getServer(), cmd);
    }

    void usage(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText("/Kit"));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender icommandsender,
                                        String[] astring)
    {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] astring, int i)
    {
        return false;
    }

    @Override
    public int compareTo(Object o)
    {
        return 0;
    }
}
