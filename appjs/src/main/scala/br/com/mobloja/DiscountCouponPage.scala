package br.com.mobloja

import com.raquo.laminar.api.L.*
import org.scalajs.dom

object DiscountCouponPage:

	lazy val node: HtmlElement = Box("Cupom de desconto", renderForm).build
	
	def renderForm =
		DiscountCouponForm().build

class DiscountCouponPage:
	import DiscountCouponPage.*
	def build = node


