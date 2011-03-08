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

//case class Doc(title: String, author: String, html: String)

class WoodchipperSerie extends FlotSerie {
  override val points = Full(new FlotPointsOptions {
    override val radius = Full(3)
    override val show = Full(true)
  })
  override val lines = Full(new FlotLinesOptions { override val show = Full(false) })
  override val shadowSize = Full(3)
}

class Visualization {
  //val docs = scala.collection.mutable.Map[(Int, Int), Doc]()
  //val titles = new scala.collection.mutable.ArrayBuffer[String]
  //val authors = new scala.collection.mutable.ArrayBuffer[String]
  //val htmls = new scala.collection.mutable.ArrayBuffer[scala.collection.mutable.ArrayBuffer[String]]
  def draw(xhtml: NodeSeq) = {

    val colors = List("#7FFF00", "#FF6347", "#7FFFD4", "#DDA0DD", "#B0C4DE", "#FFE4C4", "#B22222")

    val reducer = new PCAReducer
    //val matrix = scala.collection.mutable.ArrayBuffer[Array[Double]]()
    //val colass = scala.collection.mutable.ArrayBuffer[String]()

    //val docs = scala.collection.mutable.Map[(Int, Int), Doc]() 


    val sel = selectedTexts.is.reverse.map { (text: Text) => (text, Document.findAll(By(Document.text, text.id))) }

    //val matrix = selectedTexts.is.reverse.map { text =>
      //titles += text.title.is
      //authors += text.author.is
      //htmls += new scala.collection.mutable.ArrayBuffer[String]
    //  .map { document =>
    //    matrix += document.features
      //  htmls(htmls.size - 1) += document.html.is

        //colass += color
    //  }
    //}

    val matrix = sel.flatMap { _._2.map { _.features } }

    val reduced = reducer.reduce(matrix.toArray, 2).data
    //reduced.foreach { _.foreach { println(_) }}
    var i = 0
    //var ti = 0
    val series = sel.zip(colors).map { case ((text, docs), col) =>
      //val tj = ti
      //ti += 1
      val vals = docs.map { doc => //Document.findAll(By(Document.text, text.id)).map { doc =>
        val j = i
        i += 1
        //docs((tj, j)) = Doc(text.title.is, text.author.is, doc.html.is)
        (reduced(j)(0), reduced(j)(1))
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
    //implicit val formats = DefaultFormats

    /*val points = docs.map {
      case Doc(title, author, html) => "[
    }*/
    //val json = "var titles = " + pretty(render(JArray(titles.toList.map(JString(_))))) + "; " +
    //           "var authors = " + pretty(render(JArray(authors.toList.map(JString(_))))) + "; " +
    //           "var htmls = " + pretty(render(JArray(htmls.toList.map((s: scala.collection.mutable.ArrayBuffer[String]) => JArray(s.toList.map(JString(_))))))) + "; "
                
     

    //val json = "var titles  = [" +  titles.map("\"" + _ + "\"").mkString(",") + "];  " +
    //           "var authors = [" + authors.map("\"" + _ + "\"").mkString(",") + "];  " +
    //           "var htmls = [" + authors.map("\"" + _ + "\"").mkString(",") + "];  " +
      

    Script(JsRaw(
      """jQuery("#vizmap").bind("plotclick", function (event, pos, item) {
           if (item) {
             //alert(htmls[item.seriesIndex][item.dataIndex]);

             jQuery('#drilldown-title').text(titles[item.seriesIndex])
             jQuery('#drilldown-author').text(authors[item.seriesIndex])
             jQuery('#drilldown').html(htmls[item.seriesIndex][item.dataIndex])
             plot_vizmap.highlight(item.series, item.datapoint);
           }

         });

        /*jQuery("#vizmap").bind("plothover", function (event, pos, item) {
          jQuery("#x").text(pos.x.toFixed(2));
          jQuery("#y").text(pos.y.toFixed(2));
        });*/"""))
  }
}

}
}
