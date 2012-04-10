package edu.umd.mith.woodchipper.snippet

import scala.xml.NodeSeq
import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.mapper._
import java.util.Date
import edu.umd.mith.woodchipper.model._
import Helpers._

object titleVar extends SessionVar[String](S.param("title").openOr(""))
object authorVar extends SessionVar[String](S.param("author").openOr(""))
object offsetVar extends SessionVar[Long](S.param("offset").map(_.toLong).openOr(0))

//object searchState extends SessionVar[(String, String)]

class Search {
  //object titleVar extends RequestVar[String](S.param("title").openOr(""))
  //object authorVar extends RequestVar[String](S.param("author").openOr(""))
  
  def render(in: NodeSeq): NodeSeq = {
    var title = titleVar.is //S.param("title") openOr ""
    var author = authorVar.is //S.param("author") openOr ""
    //var offset = offsetVar.is //S.param("author") openOr ""
    bind("search", in,
         "title" -> SHtml.text(title, title = _),
         "author" -> SHtml.text(author, author = _),
         "perform" -> SHtml.submit("Search", () => perform(title, author)))
  }

  def perform(title: String, author: String): Unit = {
    //if (!title.isEmpty) {
      titleVar(title)
    //}

    //if (!author.isEmpty) {
      authorVar(author)
    //}

    offsetVar(0)
    /*S.param("offset").map { offset =>
      offsetVar(offset.toInt)
      S.redirectTo("/search?offset=" + offset)
    }.openOr {
      S.redirectTo("/search")
    }*/

    /*if (title.isEmpty && author.isEmpty) {
      S.redirectTo("/search")
    } else if (title.isEmpty && !author.isEmpty) {
      S.redirectTo("/search?author=" + author)
    } else if (!title.isEmpty && author.isEmpty) {
      S.redirectTo("/search?title=" + title)
    } else if (!title.isEmpty && !author.isEmpty) {
      S.redirectTo("/search?title=" + title + "&author=" + author)
    }*/
  }
}

class VisualizeButtonSnippet {
  def render(in: NodeSeq): NodeSeq = {
    bind("vis", in,
         "perform" -> SHtml.submit("Visualize", () => S.redirectTo("/viz")))
  }

  def perform(): Unit = {
    S.redirectTo("/visualize")
  }
}

//class TextSearchPaginatorSnippet extends StatefulSnippet with StatefulSortedPaginatorSnippet[Text, MappedField[_, Text]] {
class TextSearchPaginatorSnippet extends StatefulSnippet with PaginatorSnippet[Text] { //, MappedField[_, Text]] {
//class TextSearchPaginatorSnippet extends PaginatorSnippet[Text] {
  override def itemsPerPage = 5

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
    val current = text :: selectedTexts.is.filterNot(_ == text)
    selectedTexts(current)
  }

  override def first = {
    S.param(this.offsetParam).map { offset =>
      val off = offset.toLong
      offsetVar(off)
      off
    }.openOr(offsetVar.is)
  }

  def renderPage(in: NodeSeq): NodeSeq = page.flatMap(item => {
    //bind("item", in, "add" -> SHtml.link("/add?id=" + item.id, () => selectText(item), scala.xml.Text("Add")),
    val url = "/add?id=%d".format(item.id.is)
    //val url = "/add?id=" + item.id.toString
    bind("item", in, "add" -> <a href={url}>Add</a>,
                     "uid" -> item.uid,
                     "title" -> item.title,
                     "author" -> item.author,
                     "year" -> item.year,
                     "collection" -> item.collectionName)})

  //override def allPages = 

  /*override def pageUrl(offset: Long) = {
    val currentTitle = titleVar.is
    S.fmapFunc(S.NFuncHolder(() => { titleVar(currentTitle); authorVar(currentAuthor) })) { (title, author) =>
      Helpers.appendParams(super.pageUrl(offset), List(author -> "_"))
    //S.fmapFunc(S.NFuncHolder(() => titleVar(currentTitle))) { name =>
    //  Helpers.appendParams(super.pageUrl(offset), List(name -> "_"))
    }
  }*/

  override def pagesXml(pages : Seq[Int], sep: NodeSeq) = scala.xml.Text("...")

  /*def paginate(xhtml: NodeSeq) = {  
       bind(navPrefix, xhtml,  
         "first" -> pageXml(0, firstXml),  
         "prev" -> pageXml(first-itemsPerPage max 0, prevXml),  
         "allpages" -> {(n:NodeSeq) => pagesXml(0 until numPages, n)},
         "zoomedpages" -> {(ns: NodeSeq) => pagesXml(zoomedPages, ns)},  
         "next" -> pageXml(first+itemsPerPage min itemsPerPage*(numPages-1) max
         0, nextXml),  
         "last" -> pageXml(itemsPerPage*(numPages-1), lastXml),  
         "records" -> currentXml,  
         "recordsFrom" -> Text(recordsFrom),  
         "recordsTo" -> Text(recordsTo),  
         "recordsCount" -> Text(count.toString)  
       )  
     }*/
}

object selectedTexts extends SessionVar[List[Text]](List[Text]())

class SelectedSnippet {
  //object selectedTexts extends SessionVar[Set[Text]](Set[Text]())

  def removeText(text: Text) {
    val current = selectedTexts.is.filterNot(_ == text)
    selectedTexts(current)
  }

  def renderPage(in: NodeSeq): NodeSeq = selectedTexts.flatMap(item => {
    //bind("item", in, "remove" -> SHtml.link("/remove?id=" + item.id, () => removeText(item), scala.xml.Text("Remove")),
    val url = "/remove?id=%d".format(item.id.is)
    bind("item", in, "remove" -> <a href={url}>Remove</a>,
                     "uid" -> item.uid,
                     "title" -> item.title,
                     "author" -> item.author,
                     "year" -> item.year,
                     "collection" -> item.collectionName)})
}

