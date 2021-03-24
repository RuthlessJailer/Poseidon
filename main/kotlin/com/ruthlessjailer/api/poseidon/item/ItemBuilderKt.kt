package com.ruthlessjailer.api.poseidon.item

import com.google.common.collect.Multimap
import com.ruthlessjailer.api.poseidon.multiversion.XColor
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.*

inline fun item(build: ItemBuilderKt.() -> Unit): ItemStack = ItemBuilderKt().apply(build).builder.build()

/**
 * @author RuthlessJailer
 */
class ItemBuilderKt {
	var builder: ItemBuilder = ItemBuilder.from()

	var material: Material
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.material(value)
		}

	var item: ItemStack
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.item(value)
		}

	var meta: ItemMeta
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.meta(value)
		}

	var displayName: String
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.displayName(value)
		}

	var localizedName: String
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.localizedName(value)
		}

	var lore: List<String>
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.lore(value)
		}

	var flags: List<ItemFlag>
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.flags(value)
		}

	fun flag(vararg flags: ItemFlag) {
		builder.flags(*flags)
	}

	var enchantments: Map<Enchantment, Int>
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.enchantments(value)
		}

	fun enchantment(enchantment: Enchantment, level: Int) {
		builder.enchantment(enchantment, level)
	}

	var attributeModifiers: Multimap<Attribute, AttributeModifier>
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.attributeModifiers(value)
		}

	fun attributeModifier(attribute: Attribute, vararg modifier: AttributeModifier) {
		builder.attributeModifiers(attribute, *modifier)
	}

	var skullOwner: UUID
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.skullOwner(value)
		}

	fun skullOwner(value: String) {
		builder.skullOwner(value)
	}

	var color: XColor
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.color(value)
		}

	var customModelData: Int
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.customModelData(value)
		}

	var unbreakable: Boolean
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.unbreakable(value)
		}

	var repairCost: Int
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.repairCost(value)
		}

	var damage: Int
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.damage(value)
		}

	var amount: Int
		get() = throw UnsupportedOperationException("get")
		set(value) {
			builder.amount(value)
		}

}