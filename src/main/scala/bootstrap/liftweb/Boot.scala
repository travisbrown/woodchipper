package bootstrap.liftweb

import net.liftweb.util.Props
import net.liftweb.common.Full
import net.liftweb.http.{ LiftRules, ResourceServer, S }
import net.liftweb.http.provider.HTTPRequest
import net.liftweb.sitemap.{ Menu, SiteMap }
import net.liftweb.sitemap.Loc.Hidden
import net.liftweb.mapper.{ DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, StandardDBVendor }
import java.sql.{ Connection, DriverManager }
import edu.umd.mith.cc.model._

class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = new StandardDBVendor(
        Props.get("db.driver") openOr "org.h2.Driver",
			  Props.get("db.url") openOr 
			  "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			  Props.get("db.user"), Props.get("db.password")
      )

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)
      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    ResourceServer.allow({
      case "js" :: "viz" :: "pca.js" :: Nil => true
      case "js" :: "flot" :: "jquery.flot.js" :: Nil => true
      case "js" :: "flot" :: "jquery.flot.navigate.js" :: Nil => true
      case "js" :: "flot" :: "jquery.flot.pie.js" :: Nil => true
      case "css" :: "default.css" :: Nil => true
    })

    LiftRules.addToPackages("edu.umd.mith.cc")
    Schemifier.schemify(true, Schemifier.infoF _, User, Collection, Text, Document, Feature, Topic, Word, TopicWord)

    val sitemap = SiteMap(
      Menu.i("Home") / "index" >> User.AddUserMenusAfter,
      Menu.i("Search") / "search",
      Menu.i("Viz") / "viz" >> Hidden,
      Menu.i("Remove") / "remove" >> Hidden,
      Menu.i("Add") / "add" >> Hidden,
      Menu.i("Drilldown") / "drilldown" >> Hidden
    )

    LiftRules.setSiteMapFunc(() => User.sitemapMutator(sitemap))

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(this.makeUtf8)

    LiftRules.loggedInTest = Full(() => User.loggedIn_?)
    LiftRules.statelessDispatchTable.append(edu.umd.mith.cc.services.RestService)

    S.addAround(DB.buildLoanWrapper)
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}

