package controllers

import play.api._
import play.api.mvc._
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import scala.concurrent.Future
import play.api.libs.ws.WS
import play.api.libs.ws.Response
import play.api.libs.concurrent.Execution.Implicits._
import scala.collection.JavaConversions._

object Application extends Controller {

  def updateHtml(body: String): String = {
    val doc = Jsoup.parse(body);

    doc.select("link").map(e => e.attr("href", "https://news.ycombinator.com/" + e.attr("href")))
    doc.select("img").map(e => e.attr("src", "https://news.ycombinator.com/" + e.attr("src")))

    for {
      e <- doc.select("a")
      if (!(e.attr("href").contains("http://") || e.attr("href").contains("https://")))
    } yield {
      if (e.attr("href").contains("?")) e.attr("href", "http://localhost:9000/" + e.attr("href"))
      else e.attr("href", "http://localhost:9000/" + e.attr("href"))
    }

    doc.select("head").append("<link type='text/css' href='https://raw.github.com/Primigenus/Cleaner-Hacker-News/master/cleaner-hn.css' rel='stylesheet'>")
    doc.select(".pagetop").last().parent().after("<td style='text-align:right;padding-right:4px;'><span class='pagetop'><select><option value='volvo'>Volvo</option><option value='saab'>Saab</option><option value='opel'>Opel</option><option value='audi'>Audi</option></select></span></td>");
    
    doc.toString()
  }

  def lookup(route: String, id: Option[String]) = Action {
    id match {
      case Some(x) => {
        val myFunction: Future[Response] = WS.url("https://news.ycombinator.com/" + route + "?id=" + x).get()

        Async {
          myFunction.map(resp => Ok(updateHtml(resp.body)).as(HTML));
        }
      }
      case None => Ok("page not found...")
    }
  }

  def index(name: String) = Action {
    println("name: " + name)
    val myFunction: Future[Response] = WS.url("https://news.ycombinator.com/" + name).get()

    Async {
      myFunction.map(resp => Ok(updateHtml(resp.body)).as(HTML))
    }
  }

}