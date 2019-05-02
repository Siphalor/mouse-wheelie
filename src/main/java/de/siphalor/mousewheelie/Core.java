package de.siphalor.mousewheelie;

import net.minecraft.client.MinecraftClient;
import net.minecraft.container.SlotActionType;

import java.util.ArrayDeque;

public class Core {
	public static final String MODID = "mousewheelie";
	public static int scrollFactor = -1;

	public static ArrayDeque<ClickEvent> clickQueue = new ArrayDeque<>();
	public static boolean sending = false;


	public static void pushClickEvent(int containerSyncId, int slotId, int buttonId, SlotActionType slotAction) {
		ClickEvent clickEvent = new ClickEvent(containerSyncId, slotId, buttonId, slotAction);
		if(!sending)
			clickEvent.send();
        else
        	clickQueue.push(clickEvent);
	}

	public static void stopSending() {
		sending = false;
		clickQueue.clear();
	}

	public static class ClickEvent {
		private int containerSyncId;
		private int slotId;
		private int buttonId;
		private SlotActionType slotAction;

		ClickEvent(int containerSyncId, int slotId, int buttonId, SlotActionType slotAction) {
			this.containerSyncId = containerSyncId;
			this.slotId = slotId;
			this.buttonId = buttonId;
			this.slotAction = slotAction;
		}

		public void send() {
			sending = true;
			MinecraftClient.getInstance().interactionManager.method_2906(containerSyncId, slotId, buttonId, slotAction, MinecraftClient.getInstance().player);
		}
	}
}
