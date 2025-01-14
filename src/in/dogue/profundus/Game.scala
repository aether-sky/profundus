package in.dogue.profundus

import com.deweyvm.gleany.{Glean, GleanyGame, GleanyInitializer}
import java.util.concurrent.{Executors, Callable, TimeUnit}
import in.dogue.profundus.input.Controls
import scala.util.Random
import in.dogue.antiqua.Antiqua
import Antiqua._
import in.dogue.profundus.utils.PerfTrack

object Game {
  var t = 0
  val debug = false
  val fixedSeed  = false && debug
  val flyMode    = false && debug
  val invMode    = true && debug
  val hasDrill   = true && debug
  var lightsOff  = true && debug
  val version = "Version 1.0.0"
  val updatePerf = new PerfTrack("World Update")
  val globPerf = new PerfTrack("Everything")
  val drawPerf = new PerfTrack("Drawing")
  def getSeed = {
    val seed = fixedSeed.select(new Random().nextInt(), 1847809037)
    println("SEED: " + seed)
    seed
  }
}
class Game(initializer: GleanyInitializer) extends GleanyGame(initializer) {
  private lazy val engine = new Engine()
  override def update(): Unit = {
    Controls.update()
    engine.update()
    Game.t += 1
  }

  override def draw(): Unit =  {
    engine.draw()
  }

  override def resize(width: Int, height: Int): Unit =  {
    Glean.y.settings.setWindowSize(width, height)
  }

  override def dispose(): Unit =  {
    val executor = Executors.newSingleThreadExecutor()
    executor.invokeAll(java.util.Arrays.asList(new Callable[Unit] {
      override def call(): Unit = ()
    }), 2, TimeUnit.SECONDS)
    executor.shutdown()
  }
}
