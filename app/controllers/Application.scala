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

  val appRoot = "jmaat.me"
    
  val style = """
    <link id='style' type='text/css' href='' rel='stylesheet'>
  """
    
  val styles = """
  	<td style='text-align:right;padding-right:4px;'>
  		<span class='pagetop'>
  			<select id='styles' onchange='changeStyle()'>
  				<option value='hn/assets/stylesheets/cleaner-hn.css' selected='selected'>Cleaner-Hacker-News</option>
  				<option value='hn/assets/stylesheets/comfy-helvetica.css'>Comfy-Helvetica</option><!--https://comfy-helvetica.jottit.com/user-css -->
  				<option value='hn/assets/stylesheets/georgify.css'>Georgify</option><!--https://userstyles.org/styles/46180/georgify-for-hacker-news -->
  				<option value='hn/assets/stylesheets/solarized.css'>Solarized</option><!--https://userstyles.org/styles/76042/hacker-news-neue -->
  				<option value='hn/assets/stylesheets/neue.css'>Neue</option><!--https://userstyles.org/styles/76042/hacker-news-neue -->
  			</select>
  		</span>
  	</td> 
  """
    
  val styleSelector = """
  	<script>
    	var initStyle = (localStorage.getItem('style') !== null) ? localStorage.getItem('style') : 'hn/assets/stylesheets/cleaner-hn.css';
    	changeStyle(initStyle);
    
    	function changeStyle(newStyle){
    		var style;
    
    		if(typeof newStyle !== 'undefined') {
    			style = newStyle;
  			} else{
  				var e = document.getElementById('styles');
  				style = e.options[e.selectedIndex].value;
  			}
    		
    		document.getElementById('style').href = "http://""" + appRoot + """/" + style;
    		localStorage.setItem('style', style);
  		}	
  	</script>
  """
    		
  val resetOptions = """
    <script>
    	document.getElementById('styles').value = localStorage.getItem('style');
    </script>
  """
    
  def updateHtml(body: String): String = {
    val doc = Jsoup.parse(body);

    doc.select("link").map(e => e.attr("href", "https://news.ycombinator.com/" + e.attr("href")))
    doc.select("img").map(e => e.attr("src", "https://news.ycombinator.com/" + e.attr("src")))

    for {
      e <- doc.select("a")
      if (!(e.attr("href").contains("http://") || e.attr("href").contains("https://")))
    } yield {
      if (e.attr("href").contains("?")) e.attr("href", "http://" + appRoot + "/hn/" + e.attr("href"))
      else e.attr("href", "http://" + appRoot + "/hn/" + e.attr("href"))
    }
    
    doc.select("head").append(style)
    doc.select("head").append(styleSelector)
    doc.select(".pagetop").last().parent().remove()
    doc.select(".pagetop").last().parent().after(styles)
    doc.select("body").append(resetOptions)

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
    val myFunction: Future[Response] = WS.url("https://news.ycombinator.com/" + name).get()

    Async {
      myFunction.map(resp => Ok(updateHtml(resp.body)).as(HTML))
    }
  }
  
}