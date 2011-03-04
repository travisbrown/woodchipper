package edu.umd.mith.cc {
package snippet {

import _root_.scala.xml.NodeSeq
import _root_.net.liftweb.http._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.mapper._
import _root_.java.util.Date
import edu.umd.mith.cc.lib._
import edu.umd.mith.cc.model._
import Helpers._
import net.liftweb.http.js.JsCmds._
import net.liftweb.widgets.flot._

class Visualization {
  def draw(xhtml: NodeSeq) = {
    val data_values: List[(Double, Double)] = for (i <- List.range (-100, 100))
      yield (i / 100.0, Math.sin(i / 100.0)
    )

    val data_to_plot = new FlotSerie() {
      override val data = data_values
      override val points = Full(new FlotPointsOptions {
        override val radius = Full(3)
        override val show = Full(true)
      })
      override val lines = Full(new FlotLinesOptions { override val show = Full(false) })
      override val color = Full(Left("#7fff00"))
      override val shadowSize = Full(3)

    } 

    val options = new FlotOptions {
      override val xaxis = Full(new FlotAxisOptions {
        override val min = Full(-1.0)
        override val max = Full(1.0)
      })

      override val yaxis = Full(new FlotAxisOptions {
        override val min = Full(-1.0)
        override val max = Full(1.0)
      })
    }

    Flot.render("vizmap", List(data_to_plot), options, Flot.script(xhtml))
  }
}

}
}
