package com.winthier.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;

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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        // Check syntax
        if (args.length != 0) {
            usage(sender);
            return;
        }
        // Fetch player
        EntityPlayerMP player = sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null;
        if (player == null) {
            sender.addChatMessage(new TextComponentString("Player expected"));
            return;
        }
        // Get and set last use
        UUID uuid = player.getUniqueID();
        Long lastUse = lastUses.get(uuid);
        if (lastUse != null) {
            long dist = System.currentTimeMillis() - lastUse;
            dist /= 1000;
            if (dist < 60 * 60 * 24) {
                sender.addChatMessage(new TextComponentString("You are still on cooldown."));
                return;
            }
        }
        lastUses.put(uuid, System.currentTimeMillis());
        // Give items
        String name = player.getCommandSenderEntity().getName();
        consoleCommand("give " + name + " minecraft:stone_pickaxe");
        consoleCommand("give " + name + " minecraft:stone_sword");
        consoleCommand("give " + name + " minecraft:planks 32");
        consoleCommand("give " + name + " minecraft:bread 32");
        consoleCommand("give " + name + " minecraft:torch 32");
        sender.addChatMessage(new TextComponentString("Enjoy your starter kit!"));
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        return null;
    }

    void consoleCommand(String cmd) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager().executeCommand(UtilMod.getServer(), cmd);
    }

    void usage(ICommandSender sender) {
        sender.addChatMessage(new TextComponentString("/Kit"));
    }

    @Override
    public boolean isUsernameIndex(String[] astring, int i)
    {
        return false;
    }


    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}
