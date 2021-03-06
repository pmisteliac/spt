package org.metaborg.spt.testrunner

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.extensions.PluginId
import org.metaborg.spt.testrunner.intellij.BuildConfig
import java.io.File

/**
 * About this plugin.
 */
object SptPlugin {

  /**
   * Gets the ID of the plugin.
   *
   * @return The ID of the plugin.
   */
  // Defined in plugin.xml
  val id = PluginId.getId("org.metaborg.spt.testrunner.intellij")

  /**
   * Gets the plugin object.
   *
   * @return The plugin object.
   */
  val plugin = PluginManager.getPlugin(this.id)!!

  /**
   * Gets the path to the plugin's home folder.
   *
   * @return The home folder of the plugin.
   */
  val path = this.plugin.path!!

  /**
   * Gets the path to the plugin's library folder.
   *
   * @return The lib/ folder of the plugin.
   */
  val libPath = File(this.path, "lib")

  /**
   * Gets the version of the plugin.
   *
   * @return The version of the plugin.
   */
  // Defined in gradle.properties
  val version = this.plugin.version!!

  /**
   * Gets the Metaborg version, defined by the build script.
   *
   * @return The Metaborg version.
   */
  // Defined in gradle.properties
  val metaborgVersion = BuildConfig.METABORG_VERSION
}