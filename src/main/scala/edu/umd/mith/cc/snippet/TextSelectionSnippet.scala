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

//object selectedTexts extends SessionVar[List[Text]](List[Text]())

class TextSelectionSnippet {

  private def redirect = {
    "/search?offset=%d".format(offsetVar.is)
    //?title=%s&author=%s".format(S.param("title").openOr(""), S.param("author").openOr(""))
  }

  def remove(in: NodeSeq): NodeSeq = {
    val id = S.param("id").openOr("0").toInt
    val current = selectedTexts.is.filterNot(_.id == id)
    selectedTexts(current)
    S.redirectTo(this.redirect)
    new scala.xml.Text("remove")
  }

  def add(in: NodeSeq): NodeSeq = {
    val id = S.param("id").openOr("0").toInt
    val text = Text.findAll(By(Text.id, id))(0)
    val current = text :: selectedTexts.is.filterNot(_.id == id)
    selectedTexts(current)
    S.redirectTo(this.redirect)
    new scala.xml.Text("add")
  }
}

}
}
