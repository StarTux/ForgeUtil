package com.winthier.util.tpa;


import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class TPAAcceptCommand extends CommandBase{

	@Override
	public String getCommandName() {
		return "accept";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/accept playername - accept a TPA request";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayerMP player = sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null;
		if (player == null) {
			sender.addChatMessage(new TextComponentString("Only Players can use this command"));
			return;
		}
		if(args == null || args.length != 1) {
			sender.addChatMessage(new TextComponentString("This command requires exactly 1 argument"));
			sender.addChatMessage(new TextComponentString(getCommandUsage(sender)));
			return;
		}
		String teleportername = args[0];
		EntityPlayerMP teleporter = player.getServer().getPlayerList().getPlayerByUsername(teleportername);
		if (teleporter == null) {
			sender.addChatMessage(new TextComponentString("Player not found: " + teleportername));
			return;
		}

		TPACommand.RequestInfo info = TPACommand.instance.teleportRequests.get(teleporter.getUniqueID());
		if(info == null || info.isExpired() || info.destination.equals(teleporter.getUniqueID())){
			sender.addChatMessage(new TextComponentString("There is no active request for " + teleporter.getName()
			+ " to teleport to you. Did the request expire? Did they attempt to teleport to someone else instead?"));
			return;
		}
		SimpleTeleporter.teleportToPlayer(teleporter, player);
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}
}
