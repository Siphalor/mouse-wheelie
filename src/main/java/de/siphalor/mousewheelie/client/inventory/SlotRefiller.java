package de.siphalor.mousewheelie.client.inventory;

import de.siphalor.mousewheelie.client.Config;
import de.siphalor.mousewheelie.client.inventory.SlotRefiller.ConfigRule;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.tweed.config.entry.BooleanEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.collection.DefaultedList;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
@SuppressWarnings("unused")
public class SlotRefiller {
	private static PlayerInventory playerInventory;
	private static ItemStack stack;

	private static final Rule BLOCK_RULE;
	private static final Rule ITEMGROUP_RULE;
	private static final Rule ITEM_HIERARCHY_RULE;
	private static final Rule BLOCK_HIERARCHY_RULE;
	private static final Rule FOOD_RULE;
	private static final Rule EQUAL_STACK_RULE;

	private static final ConcurrentLinkedDeque<Rule> rules = new ConcurrentLinkedDeque<>();

	public static void set(PlayerInventory playerInventory, ItemStack stack) {
		SlotRefiller.playerInventory = playerInventory;
		SlotRefiller.stack = stack;
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean refill() {
		Iterator<Rule> iterator = rules.descendingIterator();
		while (iterator.hasNext()) {
			Rule rule = iterator.next();
			if (rule.matches(stack)) {
				int slot = rule.findMatchingStack(playerInventory, stack);
				if (slot != -1) {
					if (slot < 9) {
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

		static int iterateInventory(PlayerInventory playerInventory, Function<ItemStack, Boolean> consumer) {
			for (int i = 0; i < playerInventory.main.size(); i++) {
				if (consumer.apply(playerInventory.main.get(i)))
					return i;
			}
			return -1;
		}
	}

	abstract static class ConfigRule implements Rule {
		BooleanEntry booleanEntry;

		ConfigRule(String name, boolean enabled, String comment) {
			booleanEntry = Config.refillRules.register(name, new BooleanEntry(enabled)).setComment(comment);
			rules.add(this);
		}

		@Override
		public final boolean matches(ItemStack oldStack) {
			if (booleanEntry.value)
				return matchesEnabled(oldStack);
			return false;
		}

		abstract boolean matchesEnabled(ItemStack oldStack);
	}

	public static void initialize() {
	}

	static {
		BLOCK_RULE = new ConfigRule("any-block", false, "Tries to find any block items") {
			@Override
			boolean matchesEnabled(ItemStack oldStack) {
				return oldStack.getItem() instanceof BlockItem;
			}

			@Override
			public int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
				return Rule.iterateInventory(playerInventory, itemStack -> itemStack.getItem() instanceof BlockItem);
			}
		};

		ITEMGROUP_RULE = new ConfigRule("itemgroup", false, "Find items of the same item group") {
			@Override
			boolean matchesEnabled(ItemStack oldStack) {
				return oldStack.getItem().getGroup() != null;
			}

			@Override
			public int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
				ItemGroup itemGroup = oldStack.getItem().getGroup();
				return Rule.iterateInventory(playerInventory, (itemStack) -> itemStack.getItem().getGroup() == itemGroup);
			}
		};

		ITEM_HIERARCHY_RULE = new ConfigRule("item-hierarchy", false, "Try to find similar items through the item type hierarchy") {
			@Override
			boolean matchesEnabled(ItemStack oldStack) {
				return oldStack.getItem().getClass() != Item.class && !(oldStack.getItem() instanceof BlockItem);
			}

			@Override
			public int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
				int currentRank = 0;
				ConcurrentLinkedQueue<Class<?>> classes = new ConcurrentLinkedQueue<>();
				Class<?> clazz = oldStack.getItem().getClass();
				while (clazz != Item.class) {
					classes.add(clazz);
					clazz = clazz.getSuperclass();
				}
				int classesSize = classes.size();
				if (classesSize == 0)
					return -1;

				int index = -1;

				DefaultedList<ItemStack> mainInv = playerInventory.main;
				outer:
				for (int i = 0; i < mainInv.size(); i++) {
					clazz = mainInv.get(i).getItem().getClass();
					while (clazz != Item.class) {
						int classRank = classesSize;
						for (Iterator<Class<?>> iterator = classes.iterator(); iterator.hasNext(); classRank--) {
							if (classRank <= 0) break;
							if (classRank <= currentRank) continue outer;
							if (clazz.equals(iterator.next())) {
								if (classRank >= classesSize) return i;
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
		};

		BLOCK_HIERARCHY_RULE = new ConfigRule("block-hierarchy", false, "Try to find similar block items through the block type hierarchy") {
			@Override
			public boolean matchesEnabled(ItemStack oldStack) {
				return oldStack.getItem() instanceof BlockItem;
			}

			@Override
			public int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
				int currentRank = 0;
				ConcurrentLinkedQueue<Class<?>> classes = new ConcurrentLinkedQueue<>();
				Class<?> clazz = ((BlockItem) oldStack.getItem()).getBlock().getClass();
				while (clazz != Block.class) {
					classes.add(clazz);
					clazz = clazz.getSuperclass();
				}
				int classesSize = classes.size();
				if (classesSize == 0)
					return -1;

				int index = -1;
				DefaultedList<ItemStack> mainInv = playerInventory.main;

				outer:
				for (int i = 0; i < mainInv.size(); i++) {
					if (!(mainInv.get(i).getItem() instanceof BlockItem)) continue;
					clazz = ((BlockItem) mainInv.get(i).getItem()).getBlock().getClass();
					while (clazz != Block.class) {
						int classRank = classesSize;
						for (Iterator<Class<?>> iterator = classes.iterator(); iterator.hasNext(); classRank--) {
							if (classRank <= 0) break;
							if (classRank <= currentRank) continue outer;
							if (clazz.equals(iterator.next())) {
								if (classRank >= classesSize) return i;
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
		};

		FOOD_RULE = new ConfigRule("food", false, "Try to find other food items") {
			@Override
			boolean matchesEnabled(ItemStack oldStack) {
				return oldStack.isFood();
			}

			@Override
			public int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
				return Rule.iterateInventory(playerInventory, ItemStack::isFood);
			}
		};

		EQUAL_STACK_RULE = new ConfigRule("equal-stacks", true, "Try to find equal stacks") {
			@Override
			boolean matchesEnabled(ItemStack oldStack) {
				return true;
			}

			@Override
			public int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
				return playerInventory.method_7371(oldStack);
			}
		};
	}
}
