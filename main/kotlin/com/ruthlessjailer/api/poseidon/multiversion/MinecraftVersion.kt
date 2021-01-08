@file:Suppress("EnumEntryName")

package com.ruthlessjailer.api.poseidon.multiversion

import com.ruthlessjailer.api.poseidon.Chat
import lombok.AllArgsConstructor
import lombok.Getter
import org.bukkit.Bukkit

/**
 * @author RuthlessJailer
 */
@AllArgsConstructor
@Getter
enum class MinecraftVersion(val id: Int, val version: String, val type: VersionType) {

	//Modern
	v1_16(16, "1.16", VersionType.MODERN),
	v1_15(15, "1.15", VersionType.MODERN),
	v1_14(14, "1.14", VersionType.MODERN),
	v1_13(13, "1.13", VersionType.MODERN),

	//Legacy
	v1_12(12, "1.12", VersionType.LEGACY),
	v1_11(11, "1.11", VersionType.LEGACY),
	v1_10(10, "1.10", VersionType.LEGACY),
	v1_9(9, "1.9", VersionType.LEGACY),
	v1_8(8, "1.8", VersionType.LEGACY),

	//Obsolete
	v1_7(7, "1.7", VersionType.OBSOLETE),
	v1_6(6, "1.6", VersionType.OBSOLETE),
	v1_5(5, "1.5", VersionType.OBSOLETE),
	v1_4(4, "1.4", VersionType.OBSOLETE),

	//WAY too old
	v1_3_OR_OLDER(3, "1.3 or older", VersionType.OBSOLETE);

	companion object {

		val CURRENT_VERSION: MinecraftVersion
		val SERVER_VERSION: String
		val FLAT: Boolean
			get() = CURRENT_VERSION.isModern

		fun atLeast(version: MinecraftVersion): Boolean {
			return CURRENT_VERSION.isAtLeast(version)
		}

		fun atMost(version: MinecraftVersion): Boolean {
			return CURRENT_VERSION.isAtMost(version)
		}

		fun greaterThan(version: MinecraftVersion): Boolean {
			return CURRENT_VERSION.isAfter(version)
		}

		fun lessThan(version: MinecraftVersion): Boolean {
			return CURRENT_VERSION.isBefore(version)
		}

		/**
		 * @param id the major version number (e.g. `1.12.2` -> `12`)
		 *
		 * @return the parsed [MinecraftVersion]
		 */
		fun fromId(id: Int): MinecraftVersion {
			for (version in values()) {
				if (version.id == id) {
					return version
				}
			}
			throw IllegalArgumentException("Unknown version identifier $id.")
		}

		/**
		 * @param id the id, either `x.xx.x`, `x.xx`, or `xx`
		 *
		 * @return the parsed [MinecraftVersion]
		 */
		fun fromString(id: String): MinecraftVersion {
			if (id.matches(Regex("[0-9].[0-9]{1,2}(.[0-9])?"))) {
				return fromId(id.split(".")[1].toInt())
			}

			if (id.matches(Regex("[0-9]{1,2}"))) {
				return fromId(id.toInt())
			}

			throw IllegalArgumentException("Unknown version identifier $id.")
		}

		init {
			val pkg = Bukkit.getServer().javaClass.getPackage().name
			val version = pkg.substring(pkg.lastIndexOf('.') + 1)

			SERVER_VERSION = version
			CURRENT_VERSION = if (version != "craftbukkit") {
				val numeric = version.substring(1, version.indexOf('R') - 2).replace("_", ".") //v1_15_R1 -> 1_15 -> 1.15
				var dots = 0
				for (c in numeric.toCharArray()) {
					if (c == '.') {
						dots++
					}
				}

				check(version.count { it == '.' } == 1) { "Unsupported server version. Error parsing: $version -> $numeric" }
				fromId(numeric.split("\\.").toTypedArray()[1].toInt()) //15
			} else {
				v1_3_OR_OLDER
			}
			Chat.info(String.format("Detected server version %s.", CURRENT_VERSION.xname))
		}
	}

	fun isAtLeast(version: MinecraftVersion): Boolean {
		return id >= version.id
	}

	fun isAtMost(version: MinecraftVersion): Boolean {
		return id <= version.id
	}

	fun isBefore(version: MinecraftVersion): Boolean {
		return id < version.id
	}

	fun isAfter(version: MinecraftVersion): Boolean {
		return id > version.id
	}

	val isModern: Boolean
		get() = type == VersionType.MODERN
	val isLegacy: Boolean
		get() = type == VersionType.LEGACY || isObsolete
	val isObsolete: Boolean
		get() = type == VersionType.OBSOLETE
	val xname: String
		get() = "$name.x"
	val previous: MinecraftVersion
		get() = fromId(id - 1)
	val next: MinecraftVersion
		get() = fromId(id + 1)


	enum class VersionType {
		MODERN,
		LEGACY,
		OBSOLETE;
	}

	class UnsupportedServerVersionException @JvmOverloads constructor(message: String = "", cause: Throwable? = null) :
			RuntimeException("Unsupported server version (" + CURRENT_VERSION!!.xname + ") encountered. " + message, cause) {
		constructor(cause: Throwable?) : this("", cause) {}

		companion object {
			private const val serialVersionUID = -4100099225681420337L
		}
	}
}