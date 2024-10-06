package sky.aether

import com.deweyvm.gleany.saving.{ControlName, ControlNameCollection}



class TestGameControls(descriptor: String) extends ControlName {
  override def name: String = descriptor
}

object TestGameControls extends ControlNameCollection[TestGameControls] {
  def fromString(string: String): Option[TestGameControls] = None
  def makeJoypadDefault: Map[String,String] = Map()
  def makeKeyboardDefault: Map[String,java.lang.Float] = Map()
  def values: Seq[TestGameControls] = Seq()
}

