package br.com.mobloja

import com.raquo.laminar.api.L._
import org.scalajs.dom

object App:
	lazy val node: HtmlElement = DiscountCouponPage().build

@main def main(args: String*) =
	println("scalajs rocks!")
	lazy val container = dom.document.getElementById("app")
	render(container, App.node)
