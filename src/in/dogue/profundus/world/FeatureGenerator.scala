package in.dogue.profundus.world

import scala.util.Random
import in.dogue.antiqua.data.Direction
import in.dogue.antiqua.Antiqua._
import com.deweyvm.gleany.data.Recti
import in.dogue.profundus.world.features._
import scala.collection.mutable.ArrayBuffer
import in.dogue.profundus.world.features.Mineshaft
import in.dogue.profundus.world.features.SpikePit
import in.dogue.profundus.world.features.Campsite
import in.dogue.profundus.world.features.Cavern
import in.dogue.antiqua.geometry.Circle
import in.dogue.profundus.world.dungeon.DungeonCell

object FeatureGenerator {

  private def makePits(num:Int, y:Int, cols:Int, rows:Int)(ts:TerrainScheme, r:Random) = {
    val width = 13
    val height = 10
    (0 until num) map { case i =>
      val xx = r.nextInt(cols - width)
      val yy = r.nextInt(rows - height)
      SpikePit(xx, yy, width, height).toFeature(y, cols, rows)
    }

  }

  private def makeShafts(num:Int, y:Int, cols:Int, rows:Int)(ts:TerrainScheme, r:Random) = {
    val width = 9
    val height = 32
    (0 until num) map { case i =>
      val xx = r.nextInt(cols - width)
      val yy = r.nextInt(rows - height)
      Mineshaft(xx, yy, width, height).toFeature(y, cols, rows)
    }
  }

  private def makeCampsites(num:Int, y:Int, cols:Int, rows:Int)(ts:TerrainScheme, r:Random) = {
    val radius = 7
    (0 until num) map { case i =>
      val xx = r.nextInt(cols - (radius*2))
      val yy = r.nextInt(rows - (radius*2))
      Campsite((xx, yy), radius).toFeature(y, cols, rows)
    }
  }

  private def makeCaverns(num:Int, y:Int, cols:Int, rows:Int)(ts:TerrainScheme, r:Random) = {
    val radius = 7
    (0 until num) map { case i =>
      val xx = r.nextInt(cols - (radius*2))
      val yy = r.nextInt(rows - (radius*2))
      Cavern((xx, yy), radius).toFeature(y, cols, rows)
    }
  }

  private def makeSpikeWaves(num:Int, y:Int, cols:Int, rows:Int)(ts:TerrainScheme, r:Random) = {
    val height = 10
    val width = 55
    (0 until num) map { case i =>
      val xx = r.nextInt(cols - width)
      val yy = r.nextInt(rows - height)

      def wave(t:Int) = {
        yy + ((height/2) * math.sin(t/10f)).toInt
      }
      SpikeWave((xx, yy), width, height, wave).toFeature(y, cols, rows)
    }
  }

  def simple(cols:Int, rows:Int, y:Int, ts:TerrainScheme, r:Random, u:Unit) = {
    val yPos = y*rows
    val spikeWaves = makeSpikeWaves(1, yPos, cols, rows)(ts, r)
    val pits = makePits(3, yPos, cols, rows)(ts, r)
    val shafts = makeShafts(2, yPos, cols, rows)(ts, r)
    val camps = makeCampsites(1, yPos, cols, rows)(ts, r)
    val cavern = makeCaverns(1, yPos, cols, rows)(ts, r)
    val shop = mkShop(cols, rows, y, ts, r, u)
    val all = Vector(spikeWaves, cavern, pits, shafts, camps, shop)
    val (a, b, c) = ts.color.ways3(all)
    if (y % Stratum.size == 0) {
      val dCols = cols/DungeonCell.cellSize
      val dRows = math.max((y + Stratum.size)/Stratum.size, 2)
      Seq(new DungeonFeature(1,yPos,dCols, dRows, cols, rows, r).toFeature(cols, rows))
    } else {
      a ++ b ++ c
    }

  }

  val dummy = FeatureGenerator[Unit](simple)


  private def mkSky(cols:Int, rows:Int, y:Int, ts:TerrainScheme, r:Random, args:Unit) = {
    Seq(CaveMouth.skyFeature(cols, rows))
  }
  val sky = FeatureGenerator(mkSky)

  //(Vector[Seq[Cell]], Circle)
  private def mkSurface(cols:Int, rows:Int, y:Int, ts:TerrainScheme, r:Random, args:(Direction, Vector[Seq[Cell]], Circle)) = {
    Seq(Feature.create(true, Recti(0, 0, cols, rows), CaveMouth.createMouth(args._1, args._2, args._3)))
  }

  val surface = FeatureGenerator(mkSurface)


  private def mkShop(cols:Int, rows:Int, y:Int, ts:TerrainScheme, r:Random, args:Unit) = {
    (0 until 1).map { case i =>
      val xx = r.nextInt(cols - 16)
      val yy = r.nextInt(rows - 14)
      new Shop(xx, yy).toFeature(y*rows, cols, rows)
    }

  }


  private def mkLair(cols:Int, rows:Int, y:Int, ts:TerrainScheme, r:Random, args:Unit) = {
    Seq(new Lair().toFeature(cols, rows))
  }

  val lair = FeatureGenerator(mkLair)

  private def mkAbyss(cols:Int, rows:Int, y:Int, ts:TerrainScheme, r:Random, args:Unit) = {
    Seq(new Abyss().toFeature(cols, rows))
  }

  val abyss = FeatureGenerator(mkAbyss)
}

case class FeatureGenerator[T](private val f:(Int, Int, Int, TerrainScheme, Random, T) => Seq[Feature]) {
  def assemble(force:Seq[Feature], cols:Int, rows:Int, y:Int, ts:TerrainScheme, r:Random, t:T) = {
    val screenRect = Recti(2, 2 + y*rows, cols-4, rows-4)
    val feats = f(cols, rows, y, ts, r, t).filter { f => f.fsOverride || screenRect.containsRect(f.rect) }
    val result = ArrayBuffer[Feature](force:_*)
    for (feat <- feats) {
      var foundCollision = false
      for (r <- result) {
        if (feat.rect.area > 0 && r.intersects(feat)) {
          foundCollision = true
        }
      }
      if (!foundCollision) {
        result += feat
      }
      ()
    }
    result.toSeq
  }
}
