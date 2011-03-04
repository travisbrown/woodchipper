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


//object titleVar extends RequestVar[String](S.param("title").openOr(""))
//object authorVar extends RequestVar[String](S.param("author").openOr(""))

class Search {
  object titleVar extends RequestVar[String](S.param("title").openOr(""))
  object authorVar extends RequestVar[String](S.param("author").openOr(""))
  
  def render(in: NodeSeq): NodeSeq = {
    var title = titleVar.is //S.param("title") openOr ""
    var author = authorVar.is //S.param("author") openOr ""
    bind("search", in,
         "title" -> SHtml.text(title, title = _),
         "author" -> SHtml.text(author, author = _),
         "perform" -> SHtml.submit("Search", () => perform(title, author)))
  }

  def perform(title: String, author: String): Unit = {
    if (title.isEmpty && author.isEmpty) {
      S.redirectTo("/search")
    } else if (title.isEmpty && !author.isEmpty) {
      S.redirectTo("/search?author=" + author)
    } else if (!title.isEmpty && author.isEmpty) {
      S.redirectTo("/search?title=" + title)
    } else if (!title.isEmpty && !author.isEmpty) {
      S.redirectTo("/search?title=" + title + "&author=" + author)
    }
  }
}

class TextSearchPaginatorSnippet extends StatefulSnippet with StatefulSortedPaginatorSnippet[Text, MappedField[_, Text]] {
//class TextSearchPaginatorSnippet extends PaginatorSnippet[Text] {
  override def itemsPerPage = 10

  //object titleVar extends RequestVar[String](S.param("title").openOr(""))
  //object authorVar extends RequestVar[String](S.param("author").openOr(""))
  
  //private val search = Text.findAll(Like(Text.title, "%" + titleVar.is + "%"), StartAt(curPage * itemsPerPage), MaxRows(itemsPerPage))

  val titleQ = "%" + titleVar.is.toUpperCase + "%"
  val titleC = Cmp(Text.title, OprEnum.Like, Full(titleQ), Empty, Full("UPPER"))

  val authorQ = "%" + authorVar.is.toUpperCase + "%"
  val authorC = Cmp(Text.author, OprEnum.Like, Full(authorQ), Empty, Full("UPPER"))

  //override def count = Text.findAll(Like(Text.title, "%" + titleVar.is + "%")).size
  //override def page = Text.findAll(Like(Text.title, "%" + titleVar.is + "%"), StartAt(curPage * itemsPerPage), MaxRows(itemsPerPage))
  override def count = Text.findAll(titleC, authorC).size
  override def page = Text.findAll(titleC, authorC, StartAt(curPage * itemsPerPage), MaxRows(itemsPerPage))

  def dispatch = {
    case "renderPage" => renderPage
    case "paginate" => paginate
  }

  def headers = ("uid", Text.uid) :: ("title", Text.title) :: Nil

  def selectText(text: Text) {
    val current = selectedTexts.is + text
    selectedTexts(current)
  }

  def renderPage(in: NodeSeq): NodeSeq = page.flatMap(item =>
    bind("item", in, "add" -> SHtml.link("/search?add=" + item.id, () => selectText(item), scala.xml.Text("Add")),
                     "uid" -> item.uid,
                     "title" -> item.title,
                     "author" -> item.author,
                     "year" -> item.year,
                     "collection" -> item.collectionName))

  /*override def pageUrl(offset: Long) = {
    val currentTitle = titleVar.is
    S.fmapFunc(S.NFuncHolder(() => { titleVar(currentTitle); authorVar(currentAuthor) })) { (title, author) =>
      Helpers.appendParams(super.pageUrl(offset), List(author -> "_"))
    //S.fmapFunc(S.NFuncHolder(() => titleVar(currentTitle))) { name =>
    //  Helpers.appendParams(super.pageUrl(offset), List(name -> "_"))
    }
  }*/
}

object selectedTexts extends SessionVar[Set[Text]](Set[Text]())

class SelectedSnippet {
  //object selectedTexts extends SessionVar[Set[Text]](Set[Text]())

  def removeText(text: Text) {
    val current = selectedTexts.is - text
    selectedTexts(current)
  }

  def renderPage(in: NodeSeq): NodeSeq = selectedTexts.toSeq.flatMap(item =>
    bind("item", in, "remove" -> SHtml.link("/search?remove=" + item.id, () => removeText(item), scala.xml.Text("Remove")),
                     "uid" -> item.uid,
                     "title" -> item.title,
                     "author" -> item.author,
                     "year" -> item.year,
                     "collection" -> item.collectionName))
}

}
}
