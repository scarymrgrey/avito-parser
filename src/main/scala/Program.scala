import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.TextNode
import org.jsoup.HttpStatusException

import scala.collection.mutable
import scala.io.Source
object JsoupBrowser {
  def apply(agent: String): Browser = new JsoupBrowser(userAgent = agent)
}
object Program extends App {
  val browser = JsoupBrowser("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36")
  browser.clearCookies();
  var lastPage = false
  var page = 0
  var allItems = new mutable.ListBuffer[String]
  while (!lastPage) {
    try {
      //val doc = browser.get(s"https://www.avito.ru/moskva/avtomobili/s_probegom/volkswagen/touareg?view=list&radius=0&p=$page")
      val doc = browser.parseFile("C:\\Users\\polunin\\Pictures\\VW.html")
      val items = doc >> elementList(".item.item_list.js-catalog-item-enum.item_car a.description-title-link")
      allItems ++= items.map(r => r.attr("href")).distinct
      lastPage = true
      page += 1
    } catch {
      case ex: HttpStatusException => {
        lastPage = true
        println(ex)
      }
    }
  }
  val cnt = allItems.count(_ => true)
  val toRemove = ": ".toSet
  val res = allItems.map(r => {
    val car2 = Source.fromURL(r).mkString
    val car = browser.parseString(car2)
    val items = car >> elementList("li.item-params-list-item")
    items.map(z => {
      val span = z >> elementList(".item-params-label")
      val key = span.head.innerHtml
      val value = z.childNodes.last.asInstanceOf[TextNode].content
      (key.filterNot(toRemove), value.filterNot(toRemove))
    }).filter({case (key,value) => !(key contains "VIN")}).toMap
  })
  println(cnt)
}
