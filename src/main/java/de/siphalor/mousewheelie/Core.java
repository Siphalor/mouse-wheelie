package de.siphalor.mousewheelie;

import net.minecraft.client.MinecraftClient;
import net.minecraft.container.SlotActionType;
import net.minecraft.network.Packet;

import java.util.ArrayDeque;

public class Core {
	public static final String MODID = "mousewheelie";
	public static int scrollFactor = -1;

	public static ArrayDeque<InteractionEvent> interactionEventQueue = new ArrayDeque<>();
	public static boolean sending = false;

	public static void push(InteractionEvent interactionEvent) {
		interactionEventQueue.add(interactionEvent);
		if(!sending)
			triggerSend();
	}

	public static void pushClickEvent(int containerSyncId, int slotId, int buttonId, SlotActionType slotAction) {
		ClickEvent clickEvent = new ClickEvent(containerSyncId, slotId, buttonId, slotAction);
        push(clickEvent);
	}

	public static void triggerSend() {
		if(interactionEventQueue.size() > 0) {
			while(interactionEventQueue.pop().send()) {
				if(interactionEventQueue.isEmpty()) {
					sending = false;
					break;
				}
			}
		} else
			sending = false;
	}

	public static void stopSending() {
		sending = false;
		interactionEventQueue.clear();
	}

	public interface InteractionEvent {
		/**
		 * Sends the interaction to the server
		 * @return a boolean determining whether to continue sending packets
		 */
		boolean send();
	}

	public static class ClickEvent implements InteractionEvent {
		private int containerSyncId;
		private int slotId;
		private int buttonId;
		private SlotActionType slotAction;

		public ClickEvent(int containerSyncId, int slotId, int buttonId, SlotActionType slotAction) {
			this.containerSyncId = containerSyncId;
			this.slotId = slotId;
			this.buttonId = buttonId;
			this.slotAction = slotAction;
		}

		@Override
		public boolean send() {
			sending = true;
			MinecraftClient.getInstance().interactionManager.method_2906(containerSyncId, slotId, buttonId, slotAction, MinecraftClient.getInstance().player);
			return false;
		}
	}

	public static class PacketEvent implements InteractionEvent {
		private Packet packet;

		public PacketEvent(Packet packet) {
			this.packet = packet;
		}

		@Override
		public boolean send() {
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
			return true;
		}
	}
}
