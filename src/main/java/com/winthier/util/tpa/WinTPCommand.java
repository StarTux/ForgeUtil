package com.winthier.util.tpa;


import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class WinTPCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "wtp";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/wtp [dim] <x y z>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(!(sender instanceof EntityPlayerMP)) return;
		if(args == null || args.length < 1) return;
		int dim, x, y, z;
		try{
			dim = Integer.parseInt(args[0]);
		} catch (NumberFormatException e){
			sender.addChatMessage(new TextComponentString(getCommandUsage(sender)));
			return;
		}

		if(args.length > 3){
			try{
				x = Integer.parseInt(args[1]);
				y = Integer.parseInt(args[2]);
				z = Integer.parseInt(args[3]);
			} catch (NumberFormatException e){
				sender.addChatMessage(new TextComponentString(getCommandUsage(sender)));
				return;
			}
		} else {
			x = 0;
			y = 200;
			z = 0;
		}
		SimpleTeleporter.teleportToDimension((EntityPlayerMP)sender, dim, x, y, z);
	}
}
