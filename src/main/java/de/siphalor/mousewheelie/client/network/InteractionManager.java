package de.siphalor.mousewheelie.client.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.container.SlotActionType;
import net.minecraft.network.Packet;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class InteractionManager {
	public static Queue<InteractionEvent> interactionEventQueue = new ConcurrentLinkedQueue<>();

	private static int awaitedTriggers = 0;

	public static void push(InteractionEvent interactionEvent) {
		interactionEventQueue.add(interactionEvent);
		if (awaitedTriggers <= 0)
			triggerSend();
	}

	public static void pushClickEvent(int containerSyncId, int slotId, int buttonId, SlotActionType slotAction) {
		ClickEvent clickEvent = new ClickEvent(containerSyncId, slotId, buttonId, slotAction);
		push(clickEvent);
	}

	public static void triggerSend() {
		if (--awaitedTriggers <= 0 && interactionEventQueue.size() > 0) {
			while ((awaitedTriggers = interactionEventQueue.remove().send()) == 0) {
				if (interactionEventQueue.isEmpty()) {
					break;
				}
			}
		}
	}

	public static void clear() {
		awaitedTriggers = 0;
		interactionEventQueue.clear();
	}

	public interface InteractionEvent {
		/**
		 * Sends the interaction to the server
		 *
		 * @return the number of inventory packets to wait for
		 */
		int send();
	}

	public static class ClickEvent implements InteractionEvent {
		private final int awaitedTriggers;
		private int containerSyncId;
		private int slotId;
		private int buttonId;
		private SlotActionType slotAction;

		public ClickEvent(int containerSyncId, int slotId, int buttonId, SlotActionType slotAction) {
			this(1, containerSyncId, slotId, buttonId, slotAction);
		}

		public ClickEvent(int awaitedTriggers, int containerSyncId, int slotId, int buttonId, SlotActionType slotAction) {
			this.awaitedTriggers = awaitedTriggers;
			this.containerSyncId = containerSyncId;
			this.slotId = slotId;
			this.buttonId = buttonId;
			this.slotAction = slotAction;
		}

		@Override
		public int send() {
			//noinspection ConstantConditions
			MinecraftClient.getInstance().interactionManager.clickSlot(containerSyncId, slotId, buttonId, slotAction, MinecraftClient.getInstance().player);
			return awaitedTriggers;
		}
	}

	public static class CallbackEvent implements InteractionEvent {
		private final Supplier<Integer> callback;

		public CallbackEvent(Supplier<Integer> callback) {
			this.callback = callback;
		}

		@Override
		public int send() {
			return callback.get();
		}
	}

	public static class PacketEvent implements InteractionEvent {
		private Packet<?> packet;

		public PacketEvent(Packet<?> packet) {
			this.packet = packet;
		}

		@Override
		public int send() {
			//noinspection ConstantConditions
			MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
			return 1;
		}
	}
}
