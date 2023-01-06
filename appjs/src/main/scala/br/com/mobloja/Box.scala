package br.com.mobloja

import com.raquo.laminar.api.L.*
import org.scalajs.dom

class Box(title:String, child: HtmlElement):
	import  Box.*
	def build: HtmlElement = createNode(title, child)

object Box:
	private def createNode(title:String, child: HtmlElement): HtmlElement =
		div(
			cls("row"),
			div(
				cls(List("col-md-12", "col-xs-12")),
				div(
					cls(List("ibox", "float-e-margins")),
					div(
						cls("ibox-title"),
						h5(title)
					),
					div(
						cls("ibox-content"),
						child
					)
				)
			)
		)

