package de.siphalor.mousewheelie.client.util;

import de.siphalor.mousewheelie.MWConfig;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CreativeSearchOrder {
	private static FeatureSet lastFeatureSet = null;
	private static final Object2IntMap<StackMatcher> stackToSearchPositionLookup = new Object2IntOpenHashMap<>();
	static {
		stackToSearchPositionLookup.defaultReturnValue(Integer.MAX_VALUE);
	}
	private static final ReadWriteLock stackToSearchPositionLookupLock = new ReentrantReadWriteLock();

	public static Lock getReadLock() {
		return stackToSearchPositionLookupLock.readLock();
	}

	public static int getStackSearchPosition(ItemStack stack) {
		int pos = stackToSearchPositionLookup.getInt(StackMatcher.of(stack));
		if (pos == Integer.MAX_VALUE) {
			pos = stackToSearchPositionLookup.getInt(StackMatcher.ignoreNbt(stack));
		}
		return pos;
	}

	// Called on config change and when the feature set changes (on world join)
	public static void refreshItemSearchPositionLookup() {
		if (MWConfig.sort.optimizeCreativeSearchSort) {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world == null) {
				return;
			}
			FeatureSet enabledFeatures = client.world.getEnabledFeatures();

			if (stackToSearchPositionLookup.isEmpty() || !Objects.equals(enabledFeatures, lastFeatureSet)) {
				ItemGroups.updateDisplayParameters(enabledFeatures, true);
				Collection<ItemStack> displayStacks = new ArrayList<>(ItemGroups.SEARCH.getDisplayStacks());
				new Thread(() -> {
					Lock lock = stackToSearchPositionLookupLock.writeLock();
					lock.lock();
					stackToSearchPositionLookup.clear();
					if (displayStacks.isEmpty()) {
						lock.unlock();
						return;
					}

					int i = 0;
					for (ItemStack stack : displayStacks) {
						StackMatcher plainMatcher = StackMatcher.ignoreNbt(stack);
						if (!stack.hasNbt() || !stackToSearchPositionLookup.containsKey(plainMatcher)) {
							stackToSearchPositionLookup.put(plainMatcher, i);
							i++;
						}
						stackToSearchPositionLookup.put(StackMatcher.of(stack), i);
						i++;
					}
					lock.unlock();
				}, "Mouse Wheelie: creative search stack position lookup builder").start();
			}

		} else {
			Lock lock = stackToSearchPositionLookupLock.writeLock();
			lock.lock();
			stackToSearchPositionLookup.clear();
			lock.unlock();
		}
	}
}
