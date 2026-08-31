package com.pingspoof.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.Command;
import com.pingspoof.client.config.PingSpoofConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class PingSpoofClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PingSpoofConfig.load();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			var ps = literal("ps")
					.executes(ctx -> sendStatus(ctx.getSource()))
					.then(argument("ms", IntegerArgumentType.integer(0, PingSpoofConfig.MAX_PING_MS))
							.executes(ctx -> {
								int ms = IntegerArgumentType.getInteger(ctx, "ms");
								PingSpoofConfig.get().setPing(ms);
								String extra = ms == 0 ? " (instant reply, ping as low as possible)" : "";
								return feedback(ctx.getSource(), "Ping spoof: ON, ping set to " + ms + " ms" + extra);
							}))
					.then(literal("on").executes(ctx -> {
						PingSpoofConfig.get().setEnabled(true);
						return feedback(ctx.getSource(), "Ping spoof: ON (ping: " + PingSpoofConfig.get().pingMs() + " ms)");
					}))
					.then(literal("off").executes(ctx -> {
						PingSpoofConfig.get().setEnabled(false);
						return feedback(ctx.getSource(), "Ping spoof: OFF");
					}));

			dispatcher.register(ps);
			// Alias so the old /pingspoof command keeps working.
			dispatcher.register(literal("pingspoof").redirect(ps.build()));
		});
	}

	private static int sendStatus(FabricClientCommandSource source) {
		PingSpoofConfig config = PingSpoofConfig.get();
		if (config.isEnabled()) {
			return feedback(source, "Ping spoof: ON (ping: " + config.pingMs() + " ms)");
		}
		return feedback(source, "Ping spoof: OFF");
	}

	private static int feedback(FabricClientCommandSource source, String message) {
		source.sendFeedback(Component.literal(message));
		return Command.SINGLE_SUCCESS;
	}
}
