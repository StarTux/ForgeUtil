package com.winthier.util.tpa;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

//A teleporter that handles X-dimensional teleporting properly. Modified slightly from
//http://wiki.mcjty.eu/modding/index.php/Commands-1.9
public class SimpleTeleporter extends Teleporter {

	private final WorldServer worldServer;
	private double x;
	private double y;
	private double z;

	public SimpleTeleporter(WorldServer worldInstance, double x, double y, double z) {
		super(worldInstance);
		worldServer = worldInstance;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void placeInPortal(Entity entity, float rotationYaw){
		// This method normally attempts to put a player inside a nearby nether portal or create one if one wasn't found
		// Let's not do that.

		this.worldServer.getBlockState(new BlockPos((int) this.x, (int) this.y, (int) this.z));
		entity.setPosition(this.x, this.y, this.z);
		entity.motionX = 0.0f;
		entity.motionY = 0.0f;
		entity.motionZ = 0.0f;
	}


	public static void teleportToDimension(EntityPlayerMP player, int dimension, double x, double y, double z) {
		int oldDimension = player.worldObj.provider.getDimension();
		EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
		MinecraftServer server = ((EntityPlayerMP) player).worldObj.getMinecraftServer();
		WorldServer worldServer = server.worldServerForDimension(dimension);
		player.addExperienceLevel(0);

		if (worldServer == null || worldServer.getMinecraftServer() == null){ //Dimension doesn't exist
			throw new IllegalArgumentException("Dimension: " + dimension + " doesn't exist!");
		}

		worldServer.getMinecraftServer().getPlayerList().transferPlayerToDimension(entityPlayerMP, dimension, new SimpleTeleporter(worldServer, x, y, z));
		player.setPositionAndUpdate(x, y, z);
		if (oldDimension == 1) {
			// For some reason teleporting out of the end does weird things.
			player.setPositionAndUpdate(x, y, z);
			worldServer.spawnEntityInWorld(player);
			worldServer.updateEntityWithOptionalForce(player, false);
		}
	}

	public static void teleportToPlayer(EntityPlayerMP teleporter, EntityPlayerMP destination){
		teleportToDimension(teleporter, destination.dimension, destination.posX, destination.posY, destination.posZ);
	}
}
