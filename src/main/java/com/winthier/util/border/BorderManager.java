package com.winthier.util.border;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.winthier.util.UtilMod;
import com.winthier.util.tpa.SimpleTeleporter;
import lombok.AllArgsConstructor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class BorderManager {
	final String fileName = "borders.json";
	static BorderManager instance;
	HashMap<Integer, Border> worldborders = new HashMap<Integer, Border>();
	Map<UUID, Location> lastTickLocations = new HashMap<UUID, Location>();
	Gson gson = new Gson();

	public BorderManager(){
		instance = this;
		loadBorders();
	}

	@SubscribeEvent
	public void onPlayerUpdate(TickEvent.PlayerTickEvent event){
		if(!event.phase.equals(TickEvent.Phase.END)) return;
		EntityPlayerMP player = event.player instanceof  EntityPlayerMP ? (EntityPlayerMP) event.player : null;
		if(player == null) return;
		Location lastLocation = getLastLocation(player);
		lastTickLocations.put(player.getUniqueID(), new Location(player.dimension, player.posX, player.posY, player.posZ)); //update for next tick, don't use again this tick
		if(getBorder(player.dimension).isOutside(player)) {
			UtilMod.debugMessage("Player is outside the border!");
			if (player.dimension != lastLocation.dimension) {
				SimpleTeleporter.teleportToDimension(player, lastLocation.dimension, lastLocation.x, lastLocation.y, lastLocation.z);
			} else {
				player.setPositionAndUpdate(lastLocation.x, lastLocation.y, lastLocation.z);
			}
		}
		lastTickLocations.put(player.getUniqueID(), new Location(player.dimension, player.posX, player.posY, player.posZ));
	}

	public Location getLastLocation(EntityPlayerMP player){
		if(lastTickLocations.get(player.getUniqueID()) == null){
			lastTickLocations.put(player.getUniqueID(), new Location(player.dimension, player.posX, player.posY, player.posZ));
		}
		return lastTickLocations.get(player.getUniqueID());
	}

	//returns null if there is no dimension
	public Border getBorder(int dimID){
		if(worldborders.get(dimID) == null){
			addBorder(dimID, 5000); // attempt to create default border, if the dimension exists
		}
		return worldborders.get(dimID);
	}

	public void addBorder(int dimID, int radius) throws IllegalArgumentException{
		try {
			worldborders.put(dimID, new Border(dimID, radius));
			saveBorders();
		} catch (IllegalArgumentException e){ //there is no world provider for dimID, dimension doesn't exist yet
			throw e;
		}
	}

	public void loadBorders() {
		File file = new File(UtilMod.configDirectory.getPath() + File.separator + fileName);
		if(!file.exists()) try {file.createNewFile();} catch (IOException e) {e.printStackTrace();}
		InputStream is = null;
		BufferedReader buf = null;
		StringBuilder sb = new StringBuilder();
		try {
			is = new FileInputStream(UtilMod.configDirectory.getPath() + File.separator + fileName);
			buf = new BufferedReader(new InputStreamReader(is));
			String line = buf.readLine();
			while(line != null){
				sb.append(line).append("\n");
				line = buf.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			try {
				if(is != null) is.close();
				if(buf != null) buf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		String raw = sb.toString();
		if(raw == null || raw.isEmpty()) return;
		Type collectionType = new TypeToken<HashMap<Integer, Border>>(){}.getType();
		UtilMod.debugMessage(raw);
		worldborders = new HashMap<Integer, Border>();
		HashMap<Object, Object> load = gson.fromJson(raw, collectionType);
		for(Object o : load.keySet()){
			if(o instanceof Integer  && load.get(o) instanceof Border){
				worldborders.put((Integer)o, (Border)load.get(o));
			}
		}
	}

	public void saveBorders(){
		String bordersString = gson.toJson(worldborders);
		FileWriter writer = null;
		try{
			writer = new FileWriter(new File(UtilMod.configDirectory, fileName));
			writer.write(bordersString);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {if(writer != null) writer.close();} catch (IOException e) {}
		}
	}

	public BorderManager getInstance(){
		return this;
	}

	@AllArgsConstructor
	class Location {
		int dimension;
		double x;
		double y;
		double z;
	}
}
