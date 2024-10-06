package sky.aether

import com.deweyvm.gleany.{GleanyConfig, GleanyGame, GleanyInitializer}
import com.deweyvm.gleany.data.Point2i
import com.deweyvm.gleany.files.PathResolver
import com.deweyvm.gleany.saving.{SettingDefaults, Settings}


object Main {
  def main(args: Array[String]): Unit = {
    val settings = new Settings(TestGameControls, new SettingDefaults() {
      val SfxVolume: Float = 0.2f
      val MusicVolume: Float = 0.2f
      val WindowSize: Point2i = Point2i(500, 500)
      val DisplayMode: Int = 0
    }, false)
    val iconPaths = Seq()
    val initializer = GleanyInitializer(new PathResolver("font", "texture", "sfx", "music", "data", "shaders", "maps"),
      settings)
    val config = GleanyConfig(settings, "Scala3 Antiqua Test Game", iconPaths)

    GleanyGame.runGame(config, new Game(initializer))
  }
}