package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.ResourceServer
import _root_.net.liftweb.http.rest._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.json.JsonAST._
import _root_.net.liftweb.json.JsonDSL._
import _root_.net.liftweb.mapper._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, StandardDBVendor}
import _root_.java.sql.{Connection, DriverManager}
import _root_.edu.umd.mith.cc.model._
import net.liftweb.widgets.flot._
import edu.umd.mith.cc.util.CCURLBuilder

object WoodchipperRest extends RestHelper {
  val urlBuilder = new CCURLBuilder

  serve {
    case Req("api" :: "text" :: textId :: docSeqId :: _, "json", GetRequest) => {
      val text = Text.findAll(By(Text.id, textId.toLong))(0)
      val doc = Document.findAll(By(Document.text, text.id))(docSeqId.toInt)
      ("text" -> ("title" -> text.title.is) ~ ("author" -> text.author.is) ~ ("year" -> text.year.is)) ~
      ("document" -> ("seq" -> doc.uid.is) ~
                     ("html" -> scala.xml.Utility.escape(doc.plain.is).replaceAll("\n", "<br />")) ~
                     ("url" -> urlBuilder.buildChunkURL(text.collectionName, text.uid.is, doc.uid.is)))
    }
  }
}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = 
	new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			     Props.get("db.url") openOr 
			     "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			     Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }


    Flot.init
    //LiftRules.htmlProperties.default.set((r: Req) =>
    //  new Html5Properties(r.userAgent))

    ResourceServer.allow({
      case "flot" :: "jquery.flot.navigate.js" :: Nil => true
      case "flot" :: "jquery.flot.pie.js" :: Nil => true
    })

    // where to search snippet
    LiftRules.addToPackages("edu.umd.mith.cc")
    Schemifier.schemify(true, Schemifier.infoF _, User, Collection, Text, Document, Feature)

    // Build SiteMap
    def sitemap() = SiteMap(
      Menu.i("Home") / "index" >> User.AddUserMenusAfter, // Simple menu form
      Menu.i("Search") / "search",
      Menu.i("Viz") / "viz" >> Hidden,
      Menu.i("Remove") / "remove" >> Hidden,
      Menu.i("Add") / "add" >> Hidden,
      Menu.i("Drilldown") / "drilldown" >> Hidden
      // Menu with special Link
      /*Menu(/*Loc("Static", Link(List("static"), true, "/static/index"), 
	       "Static Content")*/)*/)

    LiftRules.setSiteMapFunc(() => User.sitemapMutator(sitemap()))

    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)

    LiftRules.loggedInTest = Full(() => User.loggedIn_?)
    LiftRules.statelessDispatchTable.append(WoodchipperRest)

    S.addAround(DB.buildLoanWrapper)
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}
