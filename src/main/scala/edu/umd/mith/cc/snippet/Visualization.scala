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
import edu.umd.mith.cc.analysis._
import Helpers._
import net.liftweb.http.js.JsCmds._
import net.liftweb.widgets.flot._
import js._
import JsCmds._
import JE._
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json.Printer.pretty 

class WoodchipperSerie extends FlotSerie {
  override val points = Full(new FlotPointsOptions {
    override val radius = Full(4)
    override val show = Full(true)
  })
  override val lines = Full(new FlotLinesOptions { override val show = Full(false) })
  override val shadowSize = Full(8)
}

class Visualization {
  var textIds: List[Long] = List[Long]()
  var reduced: Option[PCAReduction] = None 

  def draw(xhtml: NodeSeq) = {

    val colors = List("#7FFF00", "#FF6347", "#7FFFD4", "#DDA0DD", "#B0C4DE", "#FFE4C4", "#B22222")

    val reducer = new PCAReducer

    S.param("texts").foreach { textsParam =>
      val textIds = textsParam.split(",").map(_.trim.toLong)
      selectedTexts(Text.findAll(ByList(Text.id, textIds)))
    }

    val sel = selectedTexts.is.map { (text: Text) => (text, Document.findAll(By(Document.text, text.id))) }
    textIds = selectedTexts.is.map { text => text.id.is }

    val matrix = sel.flatMap { _._2.map { _.features } }
    reduced = Some(reducer.reduce(matrix.toArray, 2))
    
    var i = 0
    val series = sel.zip(colors).map { case ((text, docs), col) =>
      val vals = docs.map { doc => //Document.findAll(By(Document.text, text.id)).map { doc =>
        val j = i
        i += 1
        (reduced.get.data(j)(0), reduced.get.data(j)(1))
      }

      new WoodchipperSerie() {
        override val data = vals
        override val color = Full(Left(col))
        override val label = Full(text.title.is.substring(0, Math.min(60, text.title.is.length)) + "...")
      }
    }

    val options = new FlotOptions {
      override val grid = Full(new FlotGridOptions {
        override val hoverable = Full(true)
        override val clickable = Full(true)
      })

      override val xaxis = Full(new FlotAxisOptions {
        override val min = Full(-0.4)
        override val max = Full(0.4)
      })

      override val yaxis = Full(new FlotAxisOptions {
        override val min = Full(-0.4)
        override val max = Full(0.4)
      })
    }

    Flot.render("vizmap", series, options, Flot.script(xhtml))
  }

  def clicker(xhtml: NodeSeq) = {
    Script(JsRaw("var eigenvalues = " + pretty(render(JArray(reduced.get.loadings.map(JDouble(_)).toList))) + ";\nvar textIds = " + pretty(render(JArray(textIds.map(JInt(_))))) + ";\n"))
  }
}

}
}
