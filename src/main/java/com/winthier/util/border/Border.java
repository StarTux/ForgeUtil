package com.winthier.util.border;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Border {
	@Getter
	@Setter
	int dimID, radius, negRadius;

	//no args constructor for deserialization
	private Border(){

	}

	public Border(int dimID) throws IllegalArgumentException{
		this(dimID, 5000);
	}

	public Border(int dimID, int radius) throws IllegalArgumentException{
		this(dimID, radius, radius * -1);
	}

	public Border(int dimID, int radius, int negRadius){
		if(FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimID) == null){
			throw new IllegalArgumentException("Dimension " + dimID + " has no world provider!");
		}
		this.dimID = dimID;
		this.radius = radius;
		this.negRadius = negRadius;
	}

	//returns false if player dimension is not the border dimension. The player has to be in the correct dimension
	// to be considered outside the border.
	public boolean isOutside(EntityPlayerMP player){
		return isOutside(player.dimension, player.posX, player.posZ);
	}

	//returns false if player dimension is not the border dimension. The location has to be in the correct dimension
	// to be considered outside the border.
	public boolean isOutside(int dimension, double x, double z){
		if(dimension != dimID) return false;
		return x > radius || x < negRadius || z > radius || z < negRadius;
	}

	//TODO eh, maybe
	public static void killPlayer(EntityPlayerMP player){

	}
}
