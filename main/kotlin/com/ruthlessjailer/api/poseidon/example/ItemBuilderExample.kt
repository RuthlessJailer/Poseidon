package com.ruthlessjailer.api.poseidon.example

import com.ruthlessjailer.api.poseidon.item.item
import com.ruthlessjailer.api.poseidon.multiversion.XColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import java.awt.Color

/**
 * @author RuthlessJailer
 */
class ItemBuilderExample {

	val item = item {
		displayName = "&1just a chestplate"
		lore = listOf("&3a lore line", "&6another lore line")

		material = Material.LEATHER_CHESTPLATE
		color = XColor.fromColor(Color(0, 100, 100))

		enchantment(Enchantment.DURABILITY, 3)
		enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)

		unbreakable = true
		flag(ItemFlag.HIDE_UNBREAKABLE)
	}

}