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
      if (e.attr("href").contains("item?")) e.attr("href", "http://localhost:9000/" + e.attr("href"))
      else e.attr("href", "https://news.ycombinator.com/" + e.attr("href"))
    }

    doc.select("head").append("<link type='text/css' href='https://raw.github.com/Primigenus/Cleaner-Hacker-News/master/cleaner-hn.css' rel='stylesheet'>")

    doc.toString()
  }
  
  def updateHtml2(body: String): String = {
    val doc = Jsoup.parse(body);

    doc.select("head").append("<link type='text/css' href='https://raw.github.com/Primigenus/Cleaner-Hacker-News/master/cleaner-hn.css' rel='stylesheet'>")

    doc.toString()
  }  

  def item(id: Option[Long]) = Action {
    id match {
      case Some(x) => {
        val myFunction: Future[Response] = WS.url("https://news.ycombinator.com/item?id=" + x).get()

        Async {
          myFunction.map {
            resp =>
              {
                Ok(updateHtml2(resp.body)).as(HTML)
              }
          }
        }
      }
      case None => Ok("page not found...")
    }
  }

  def index = Action {
    val myFunction: Future[Response] = WS.url("https://news.ycombinator.com/").get()

    Async {
      myFunction.map {
        resp =>
          {
            Ok(updateHtml(resp.body)).as(HTML)
          }
      }
    }
  }

}