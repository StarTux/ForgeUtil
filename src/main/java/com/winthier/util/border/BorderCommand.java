package com.winthier.util.border;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class BorderCommand extends CommandBase{
	@Override
	public String getCommandName() {
		return "border";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return String.format("/%s [dimID] [radius]", getCommandName());
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args == null || args.length < 1) {
			argumentError(sender, "Commands: ");
			return;
		}
		String subcommand = args[0].toLowerCase();
		if(subcommand.equals("list"))
			listCommand(sender, args);
		else setCommand(sender, args);
	}

	void setCommand(ICommandSender sender, String[] args){
		int dimID;
		int radius;
		try{
			dimID = Integer.parseInt(args[0]);
			radius = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			argumentError(sender, "This command requires exactly 2 Integer arguments.");
			return;
		}
		try{
			BorderManager.instance.addBorder(dimID, radius);
			sender.addChatMessage(new TextComponentString(String.format("Dimension: %d, Radius: %d", dimID, radius)));
		} catch (IllegalArgumentException e){
			e.printStackTrace();
			argumentError(sender, "Dimension " + dimID + "has no registered world provider");
		}
	}

	void listCommand(ICommandSender sender, String[] args){
		String message ="\nDim\tradius\n";
		for(Border border : BorderManager.instance.worldborders.values()){
			message += String.format("%d\t%d\n", border.dimID, border.radius);
		}
		sender.addChatMessage(new TextComponentString(message));
	}

	public void argumentError(ICommandSender sender, String message){
		sender.addChatMessage(new TextComponentString(message));
		sender.addChatMessage(new TextComponentString(getCommandUsage(sender)));
	}
}
