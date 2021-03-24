package com.ruthlessjailer.api.poseidon.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ruthlessjailer.api.poseidon.Chat;
import com.ruthlessjailer.api.poseidon.multiversion.XColor;
import com.ruthlessjailer.api.theseus.Checks;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Colorable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class was mostly generated via a builder class generation IDE plugin which creates setters from fields in the class.
 * Most of the fields were copied from {@link ItemMeta} and {@link ItemStack} implementations.
 *
 * @author RuthlessJailer
 * @see XColor
 * @see ItemStack
 * @see ItemMeta
 */
public final class ItemBuilder {
	private Material                               material;
	private ItemStack                              item;
	private String                                 displayName;
	private String                                 localizedName;
	private List<String>                           lore               = new ArrayList<>();
	private List<ItemFlag>                         flags              = new ArrayList<>();
	private Map<Enchantment, Integer>              enchantments       = new HashMap<>();
	private Multimap<Attribute, AttributeModifier> attributeModifiers = HashMultimap.create();
	private UUID                                   skullOwner         = UUID.randomUUID();
	private XColor                                 color;
	private Integer                                customModelData;
	private boolean                                unbreakable        = false;
	private int                                    repairCost         = 0;
	private int                                    damage             = 0;
	private int                                    amount             = 1;


	private ItemBuilder(final Material material) {
		this.item     = null;
		this.material = material;
	}

	private ItemBuilder(final ItemStack item) {
		this.item     = item;
		this.material = null;
	}

	/**
	 * @deprecated do not use for any reason
	 */
	@Deprecated
	static ItemBuilder from() { return new ItemBuilder((Material) null); }

	public static ItemBuilder from(final Material material) { return new ItemBuilder(material); }

	public static ItemBuilder from(final ItemStack item)    { return new ItemBuilder(item).meta(item.getItemMeta()); }

	/**
	 * Updates all available values to values from the {@code meta}, including the current {@code item}'s (if both are non-null) via {@link ItemStack#setItemMeta(ItemMeta)}.
	 *
	 * @param meta the {@link ItemMeta} to update from
	 *
	 * @return this
	 */
	public ItemBuilder meta(final ItemMeta meta) {
		if (meta == null) { return this; }

		displayName(meta.getDisplayName());
		localizedName(meta.getLocalizedName());
		lore(meta.getLore());
		flags(new ArrayList<>(meta.getItemFlags()));
		enchantments(meta.getEnchants());
		attributeModifiers(meta.getAttributeModifiers());
		customModelData(meta.getCustomModelData());
		unbreakable(meta.isUnbreakable());

		if (meta instanceof Repairable && ((Repairable) meta).hasRepairCost()) {
			repairCost(((Repairable) meta).getRepairCost());
		}
		if (meta instanceof Colorable && ((Colorable) meta).getColor() != null) {
			color(XColor.fromDyeColor(((Colorable) meta).getColor()));
		}
		if (meta instanceof LeatherArmorMeta) {
			color(XColor.fromBukkitColor(((LeatherArmorMeta) meta).getColor()));
		}
		if (meta instanceof SkullMeta && ((SkullMeta) meta).getOwningPlayer() != null) {
			skullOwner(((SkullMeta) meta).getOwningPlayer().getUniqueId());
		}

		if (this.item != null) {
			this.item.setItemMeta(meta);
		}

		return this;
	}

	public ItemBuilder displayName(final String displayName) {
		this.displayName = displayName;
		return this;
	}

	public ItemBuilder localizedName(final String localizedName) {
		this.localizedName = localizedName;
		return this;
	}

	public ItemBuilder lore(final List<String> lore) {
		this.lore = lore;
		return this;
	}

	public ItemBuilder flags(final List<ItemFlag> flags) {
		this.flags = flags;
		return this;
	}

	public ItemBuilder enchantments(final Map<Enchantment, Integer> enchantments) {
		this.enchantments = enchantments;
		return this;
	}

	public ItemBuilder attributeModifiers(final Multimap<Attribute, AttributeModifier> attributeModifiers) {
		this.attributeModifiers = attributeModifiers;
		return this;
	}

	public ItemBuilder customModelData(final Integer customModelData) {
		this.customModelData = customModelData;
		return this;
	}

	public ItemBuilder unbreakable(final boolean unbreakable) {
		this.unbreakable = unbreakable;
		return this;
	}

	public ItemBuilder repairCost(final int repairCost) {
		this.repairCost = repairCost;
		return this;
	}

	public ItemBuilder color(final XColor color) {
		this.color = color;
		return this;
	}

	public ItemBuilder skullOwner(final UUID skullOwner) {
		this.skullOwner = skullOwner;
		return this;
	}

	public ItemBuilder item(@NonNull @NotNull final ItemStack item) {
		this.item = item;
		return meta(item.getItemMeta());
	}

	public ItemBuilder material(@NonNull @NotNull final Material material) {
		this.material = material;
		return this;
	}

	public ItemBuilder lore(final String... lore) {
		this.lore = Arrays.asList(lore);
		return this;
	}

	public ItemBuilder flags(final ItemFlag... flags) {
		this.flags = Arrays.asList(flags);
		return this;
	}

	public ItemBuilder flag(final ItemFlag flag) {
		this.flags.add(flag);
		return this;
	}

	public ItemBuilder enchantment(final Enchantment enchantment, final int level) {
		this.enchantments.put(enchantment, level);
		return this;
	}

	public ItemBuilder attributeModifiers(final Attribute attribute, final AttributeModifier... attributeModifier) {
		this.attributeModifiers.get(attribute).addAll(Arrays.asList(attributeModifier));
		return this;
	}

	public ItemBuilder attributeModifier(final Attribute attribute, final AttributeModifier attributeModifier) {
		this.attributeModifiers.get(attribute).add(attributeModifier);
		return this;
	}

	public ItemBuilder skullOwner(final String skullOwner) {
		this.skullOwner = Bukkit.getOfflinePlayer(skullOwner).getUniqueId();
		return this;
	}

	public ItemBuilder damage(final int damage) {
		this.damage = damage;
		return this;
	}

	public ItemBuilder amount(final int amount) {
		this.amount = amount;
		return this;
	}


	/**
	 * Constructs an {@link ItemStack} from this instance.
	 *
	 * @return an {@link ItemStack} reflecting all the values in this instance
	 *
	 * @throws NullPointerException if the {@code material} and the {@code item} are null; this will never happen under normal circumstances
	 */
	public ItemStack build() {
		Checks.verify(this.material != null || this.item != null, "Material or item must be set.", NullPointerException.class);

		final ItemStack item = this.material == null ? this.item : new ItemStack(this.material, this.amount, (short) this.damage);
		final ItemMeta  meta = item.getItemMeta();

		if (meta == null) { return item; }

		meta.setDisplayName(Chat.INSTANCE.colorize(this.displayName));
		meta.setLocalizedName(this.localizedName);
		meta.setLore(Chat.INSTANCE.colorize(this.lore));
		meta.addItemFlags(this.flags.toArray(new ItemFlag[this.flags.size()]));
		meta.setAttributeModifiers(this.attributeModifiers);
		meta.setCustomModelData(this.customModelData);
		meta.setUnbreakable(this.unbreakable);

		if (meta instanceof Repairable) {
			((Repairable) meta).setRepairCost(this.repairCost);
		}
		if (meta instanceof Colorable && this.color != null) {
			((Colorable) meta).setColor(this.color.getDyeColor());
		}
		if (meta instanceof LeatherArmorMeta && this.color != null) {
			((LeatherArmorMeta) meta).setColor(this.color.getBukkitColor());
		}
		if (meta instanceof SkullMeta && this.skullOwner != null) {
			((SkullMeta) meta).setOwningPlayer(Bukkit.getOfflinePlayer(this.skullOwner));
		}

		item.setItemMeta(meta);
		item.addUnsafeEnchantments(this.enchantments);

		return item;
	}
}