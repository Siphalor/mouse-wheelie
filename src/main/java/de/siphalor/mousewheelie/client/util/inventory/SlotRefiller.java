package de.siphalor.mousewheelie.client.util.inventory;

import de.siphalor.mousewheelie.client.InteractionManager;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.packet.PickFromInventoryC2SPacket;
import net.minecraft.server.network.packet.UpdateSelectedSlotC2SPacket;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SlotRefiller {
	private static PlayerInventory playerInventory;
	private static ItemStack stack;

	private static final ConcurrentLinkedDeque<Rule> rules = new ConcurrentLinkedDeque<>();

	public static void set(PlayerInventory playerInventory, ItemStack stack) {
		SlotRefiller.playerInventory = playerInventory;
		SlotRefiller.stack = stack;
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean refill() {
		Iterator<Rule> iterator = rules.descendingIterator();
		while(iterator.hasNext()) {
			Rule rule = iterator.next();
			if(rule.matches(stack)) {
				int slot = rule.findMatchingStack(playerInventory, stack);
				if(slot != -1) {
					if(slot < 9) {
						playerInventory.selectedSlot = slot;
						InteractionManager.push(new InteractionManager.PacketEvent(new UpdateSelectedSlotC2SPacket(slot)));
					} else
						InteractionManager.push(new InteractionManager.PacketEvent(new PickFromInventoryC2SPacket(slot)));
					return true;
				}
			}
		}
		return false;
	}

	public interface Rule {
		boolean matches(ItemStack oldStack);
		int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack);
	}

	static {
		rules.add(new Rule() {
			@Override
			public boolean matches(ItemStack oldStack) {
				return oldStack.getItem().getItemGroup() != null;
			}

			@Override
			public int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
				ItemGroup itemGroup = oldStack.getItem().getItemGroup();
				for(int i = 0; i < playerInventory.getInvSize(); i++) {
					if(playerInventory.getInvStack(i).getItem().getItemGroup() == itemGroup) return i;
				}
				return -1;
			}
		});

		rules.add(new Rule() {
			@Override
			public boolean matches(ItemStack oldStack) {
				return oldStack.getItem().getClass() != Item.class;
			}

			@Override
			public int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
				int currentRank = 0;
				ConcurrentLinkedQueue<Class> classes = new ConcurrentLinkedQueue<>();
				Class clazz = oldStack.getItem().getClass();
				while(clazz != Item.class) {
					classes.add(clazz);
					clazz = clazz.getSuperclass();
				}
				int classesSize = classes.size();
                if(classesSize == 0)
                	return -1;

                int index = -1;
                outer:
                for(int i = 0; i < playerInventory.getInvSize(); i++) {
                    clazz = playerInventory.getInvStack(i).getItem().getClass();
                    while(clazz != Item.class) {
                        int classRank = classesSize;
						for (Iterator iterator = classes.iterator(); iterator.hasNext(); classRank--) {
							if(classRank <= 0) break;
							if(classRank <= currentRank) continue outer;
							if(clazz.equals(iterator.next())) {
								if(classRank >= classesSize) return i;
								currentRank = classRank;
								index = i;
								continue outer;
							}
						}
						clazz = clazz.getSuperclass();
					}
				}
				return index;
			}
		});

		rules.add(new Rule() {
			@Override
			public boolean matches(ItemStack oldStack) {
				return oldStack.getItem() instanceof BlockItem;
			}

			@Override
			public int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
				int currentRank = 0;
				ConcurrentLinkedQueue<Class> classes = new ConcurrentLinkedQueue<>();
				Class clazz = ((BlockItem) oldStack.getItem()).getBlock().getClass();
				while(clazz != Block.class) {
					classes.add(clazz);
					clazz = clazz.getSuperclass();
				}
				int classesSize = classes.size();
                if(classesSize == 0)
                	return -1;

                int index = -1;
                outer:
                for(int i = 0; i < playerInventory.getInvSize(); i++) {
                	if(!(playerInventory.getInvStack(i).getItem() instanceof BlockItem)) continue;
                    clazz = ((BlockItem) playerInventory.getInvStack(i).getItem()).getBlock().getClass();
                    while(clazz != Block.class) {
                        int classRank = classesSize;
						for (Iterator iterator = classes.iterator(); iterator.hasNext(); classRank--) {
							if(classRank <= 0) break;
							if(classRank <= currentRank) continue outer;
							if(clazz.equals(iterator.next())) {
								if(classRank >= classesSize) return i;
								currentRank = classRank;
								index = i;
								continue outer;
							}
						}
						clazz = clazz.getSuperclass();
					}
				}
				return index;
			}
		});

		rules.add(new Rule() {
			@Override
			public boolean matches(ItemStack oldStack) {
				return oldStack.isFood();
			}

			@Override
			public int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
				for(int i = 0; i < playerInventory.getInvSize(); i++) {
                    if(playerInventory.getInvStack(i).isFood())
                    	return i;
				}
				return -1;
			}
		});

		rules.add(new Rule() {
			@Override
			public boolean matches(ItemStack oldStack) {
				return true;
			}

			@Override
			public int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
                return playerInventory.method_7371(oldStack);
			}
		});
	}
}
