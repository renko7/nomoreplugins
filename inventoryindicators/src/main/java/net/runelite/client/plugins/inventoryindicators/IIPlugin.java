/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.inventoryindicators;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import java.util.Arrays;
import java.util.regex.Pattern;

@Extension
@PluginDescriptor(
	name = "Inventory Indicators",
	description = "Display indicators based on various inventory related states.",
	tags = {"ahk", "inventory", "nomore"},
	type = PluginType.UTILITY
)
@Slf4j
public class IIPlugin extends Plugin {

	@Inject
	private Client client;

	@Inject
	private IIConfig config;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private InventoryOverlay inventoryOverlay;

	@Inject
	private SceneOverlay sceneOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Provides
	IIConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(IIConfig.class);
	}

	@Override
	protected void startUp() {
		overlayManager.add(sceneOverlay);
		overlayManager.add(inventoryOverlay);
	}

	@Override
	protected void shutDown() {
		overlayManager.remove(sceneOverlay);
		overlayManager.remove(inventoryOverlay);
	}

	@Getter(AccessLevel.PACKAGE)
	boolean inventoryFull = false;
	boolean inventoryContains = false;

	@Subscribe
	private void on(ItemContainerChanged event)
	{
		Item[] items = client.getItemContainer(InventoryID.INVENTORY).getItems();
		if (config.displayFull())
		{
			int i = 0;
			for (Item item : items) {
				if (item == null || item.getId() == -1)
				{
					continue;
				}
				i++;
			}
			inventoryFull = i == 28;
		}
	}

	@Subscribe
	private void on(GameTick event)
	{

		if (config.displayContain())
		{
			Item[] items = client.getItemContainer(InventoryID.INVENTORY).getItems();
			String[] configNames = config.containName().split(Pattern.quote("."));
			int slot = 1;
			for (Item item : items)
			{
				if (item == null || item.getId() == -1)
				{
					continue;
				}
				String itemName = itemManager.getItemDefinition(item.getId()).getName().toLowerCase();
				if (configNames == null)
				{
					inventoryContains = false;
					System.out.println("The inventory does not contain: " + configNames);
					continue;
				}
				for (String configName : configNames)
				{
					configName = configName.replaceAll("\\s+","");
					itemName = itemName.replaceAll("\\s+","");
					if (configName.equals(""))
					{
						inventoryContains = false;
						System.out.println("The inventory does not contain: " + configName);
						continue;
					}
					if (itemName.contains(configName.toLowerCase()))
					{
						inventoryContains = true;
						System.out.println("The inventory contains: " + configName);
						return;
					}
				}
				slot++;
			}
			System.out.println("The inventory does not contain: " + Arrays.toString(configNames));
			inventoryContains = false;
		}
	}
}