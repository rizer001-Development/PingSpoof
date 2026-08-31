package com.pingspoof.client.mixin;

import com.pingspoof.client.config.PingSpoofConfig;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The server measures a player's ping by timing how long it takes the client
 * to answer {@link ClientboundKeepAlivePacket}s. This mixin intercepts the
 * client-side handler and takes over the reply, delaying it by the configured
 * ping value in milliseconds (0 means reply immediately, keeping the ping as
 * low as possible).
 *
 * <p>When the spoof is disabled the vanilla handler is left untouched.
 */
@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientPacketListenerMixin {
	private static final ScheduledExecutorService SPOOF_SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread thread = new Thread(r, "pingspoof-keepalive");
		thread.setDaemon(true);
		return thread;
	});

	@Shadow
	@Final
	private Connection connection;

	@Inject(method = "handleKeepAlive", at = @At("HEAD"), cancellable = true)
	private void pingspoof$onKeepAlive(ClientboundKeepAlivePacket packet, CallbackInfo ci) {
		PingSpoofConfig config = PingSpoofConfig.get();
		if (!config.isEnabled()) {
			return;
		}

		ci.cancel();
		long keepAliveId = packet.getId();
		int delayMs = config.pingMs();

		if (delayMs <= 0) {
			connection.send(new ServerboundKeepAlivePacket(keepAliveId));
		} else {
			SPOOF_SCHEDULER.schedule(() -> {
				// Guard against sending on a closed (old) connection after a disconnect/reconnect.
				if (connection.isConnected()) {
					connection.send(new ServerboundKeepAlivePacket(keepAliveId));
				}
			}, delayMs, TimeUnit.MILLISECONDS);
		}
	}
}
