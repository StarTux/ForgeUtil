package com.winthier.util;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.server.MinecraftServer;
/*
 * Stops the current weather. Players will have to enter the command again when it rains again.
 */
public class StopRainCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "stoprain";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/stoprain - stops the current weather on your client";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        if(player == null) return;
        player.connection.sendPacket(new SPacketChangeGameState(1, 0f));
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
        return true;
    }
}