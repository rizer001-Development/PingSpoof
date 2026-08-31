package com.pingspoof.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pingspoof.PingSpoofMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON config stored at {@code config/pingspoof.json}.
 *
 * <p>Fields are package-private on purpose: Gson reads/writes them directly.
 */
public final class PingSpoofConfig {
	public static final int MAX_PING_MS = 60_000;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("pingspoof.json");

	private static PingSpoofConfig instance = new PingSpoofConfig();

	boolean enabled = true;
	int pingMs = 0;

	private PingSpoofConfig() {
	}

	public static PingSpoofConfig get() {
		return instance;
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			PingSpoofConfig parsed = GSON.fromJson(reader, PingSpoofConfig.class);
			if (parsed != null) {
				parsed.validate();
				instance = parsed;
			}
		} catch (IOException | RuntimeException e) {
			PingSpoofMod.LOGGER.error("Failed to load pingspoof config, using defaults", e);
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			PingSpoofMod.LOGGER.error("Failed to save pingspoof config", e);
		}
	}

	private void validate() {
		if (pingMs < 0) {
			pingMs = 0;
		}
		if (pingMs > MAX_PING_MS) {
			pingMs = MAX_PING_MS;
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	/** The ping value in milliseconds; 0 means reply instantly (as low a ping as possible). */
	public int pingMs() {
		return pingMs;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		save();
	}

	public void setPing(int pingMs) {
		this.enabled = true;
		this.pingMs = Math.max(0, Math.min(pingMs, MAX_PING_MS));
		save();
	}

	public void toggle() {
		setEnabled(!enabled);
	}
}
