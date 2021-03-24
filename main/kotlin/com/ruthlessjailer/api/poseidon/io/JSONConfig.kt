package com.ruthlessjailer.api.poseidon.io

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ruthlessjailer.api.poseidon.Chat
import com.ruthlessjailer.api.poseidon.PluginBase
import com.ruthlessjailer.api.theseus.ReflectUtil
import com.ruthlessjailer.api.theseus.io.JSONFile
import java.io.File
import java.io.InputStream

/**
 * @author RuthlessJailer
 */
abstract class JSONConfig(path: String, content: String = "", GSON: Gson = GsonBuilder().create()) :
		JSONFile(PluginBase.instance.dataFolder.path + File.separator + removeStartingSeparatorChar(path), content = content, GSON = GSON) {

	init {
		Chat.debug("Config", "Loading config class ${ReflectUtil.getPath(javaClass)}.")
	}

	override fun getResourceAsStream(path: String): InputStream? {
		val name = PluginBase.getCurrentName()
		val resourcePath = removeStartingSeparatorChar(
				path.substring(
						path.indexOf(name) + name.length))
		return PluginBase.instance.getResource(resourcePath)//plugins/PluginName/file.json
	}
}