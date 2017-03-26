package com.winthier.util.tpa;

import lombok.AllArgsConstructor;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;

import java.util.HashMap;
import java.util.UUID;

public class TPACommand extends CommandBase {
	static TPACommand instance;
	HashMap<UUID, RequestInfo> teleportRequests = new HashMap<UUID, RequestInfo>();

	public TPACommand(){
		instance = this;
	}

	@Override
	public String getCommandName() {
		return "tpa";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/tpa player";
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
		EntityPlayerMP destination = player.getServer().getPlayerList().getPlayerByUsername(args[0]);
		if (destination == null) {
			sender.addChatMessage(new TextComponentString("Player not found: " + args[0]));
			return;
		}

		teleportRequests.put(player.getUniqueID(), new RequestInfo(player.getUniqueID(), destination.getUniqueID()));
		player.addChatMessage(new TextComponentString("You sent a TP request to " + destination.getName()));
		destination.addChatMessage(new TextComponentString(player.getName() + " wants to TP to you. "));
		String acceptString = "tellraw " + destination.getName() +
				" [\"\",{\"text\":\"[Click here]\",\"color\":\"aqua\"," +
					"\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/accept " + player.getName() + "\"}," +
					"\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"/accept " + player.getName() + "\"}]}}}," +
				"{\"text\":\" to accept.\",\"color\":\"white\"}]";
		destination.getServer().commandManager.executeCommand(player.getServer(), acceptString);
		destination.getServerWorld().playSound(null, destination.posX, destination.posY, destination.posZ,
				SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 1);
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@AllArgsConstructor
	class RequestInfo{
		public UUID teleporter;
		public UUID destination;
		public final long requestTime = System.currentTimeMillis();

		public boolean isExpired(){
			return System.currentTimeMillis() - requestTime > 60 * 1000;
		}
	}
}
