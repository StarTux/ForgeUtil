package com.winthier.util;

import net.minecraftforge.common.config.Configuration;

public class Config extends Configuration{

	private static final String CATEGORY_GENERAL = "general";

	// This values below you can access elsewhere in your mod:
	public static boolean isDebug = false;

	// Call this from preInit(). It will create our config if it doesn't
	// exist yet and read the values if it does exist.
	public static void readConfig() {
		Configuration cfg = UtilMod.config;
		try {
			cfg.load();
			initGeneralConfig(cfg);
		} catch (Exception e1) {
			System.out.println("\n\n\n\n**********************ERROR SAVING CONFIG************************\n\n\n\n");
		} finally {
			if (cfg.hasChanged()) {
				System.out.println("\n\n\n\n**********************SAVING CONFIG************************\n\n\n\n");
				cfg.save();
			}
		}
	}

	private static void initGeneralConfig(Configuration cfg) {
		cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General configuration");
		// cfg.getBoolean() will get the value in the config if it is already specified there. If not it will create the value.
		isDebug = cfg.getBoolean("debug", CATEGORY_GENERAL, isDebug, "Testing without a database");
	}
}