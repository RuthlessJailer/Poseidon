package com.ruthlessjailer.api.poseidon.io

import com.ruthlessjailer.api.poseidon.PluginBase
import com.ruthlessjailer.api.theseus.io.IFile
import com.ruthlessjailer.api.theseus.io.TextFile
import org.bukkit.configuration.file.YamlConfiguration
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

/**
 * @author RuthlessJailer
 */
abstract class YAMLFile(path: String, content: String = "") : TextFile(PluginBase.instance.dataFolder.path + File.separator + removeStartingSeparatorChar(path), content) {

	lateinit var configuration: YamlConfiguration

	override fun save(): IFile {
		if (::configuration.isInitialized) {
			contents = configuration.saveToString()
		}
		return super.save()
	}

	override fun load(): IFile {
		val iFile = super.load()
		if (::configuration.isInitialized) {
			configuration.loadFromString(contents)
		}
		return iFile
	}

	override fun reload(): YAMLFile {
		configuration.loadFromString(load().contents)
		contents = configuration.saveToString()
		save()
		return getNewInstance(read()) as YAMLFile
	}

	fun registerDefaults() {
		configuration = YamlConfiguration.loadConfiguration(InputStreamReader(ByteArrayInputStream(contents.toByteArray(charset))))
		configuration.loadFromString(contents)
		configuration.addDefaults(
				YamlConfiguration.loadConfiguration(
						InputStreamReader(
								getResourceAsStream(path)
								?: getResourceAsStream(file.name)
								?://it's not a resource; we can't do anything
								throw UnsupportedOperationException("No resource found for ${removeStartingSeparatorChar(path)} or ${file.name}."))))
		configuration.options().copyDefaults(true).copyHeader(true).indent(2)
	}

	override fun getResourceAsStream(path: String): InputStream? = PluginBase.instance.getResource(removeStartingSeparatorChar(path))

}