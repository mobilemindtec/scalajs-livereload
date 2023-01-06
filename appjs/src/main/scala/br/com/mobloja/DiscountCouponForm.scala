package br.com.mobloja

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js.Date

case class ClientState(id: Int = 0,
                       name: String = "",
                       username: String = "",
                       document: String = "")


case class DiscountCouponState(name: String = "",
                               description: String = "",
                               code: String = "",
                               limitQts: Int = 0,
                               limitDate: Option[Date] = None,
                               cashDiscount: Double = 0,
                               percentDiscount: Double = 0,
                               minSale: Double = 0,
                               enabled: Boolean = false,
                               firstSaleOnly: Boolean = false,
                               client: Option[ClientState] = None,
                               showErrors: Boolean = false):
	def hasErrors: Boolean = true

	def displayError(error: DiscountCouponState => Option[String]) =
		error(this).filter(_ => showErrors)

	def notEmpty(v: String): Option[String] =
		if v.isEmpty then Some("Entre com um valor válido") else None

	def nameError: Option[String] = notEmpty(name)

	def descriptionError: Option[String] = notEmpty(description)

	def codeError: Option[String] = notEmpty(code)

val stateVar = Var(DiscountCouponState())

object Writers:
	def nameWriter = stateVar.updater[String]((state, name) => state.copy(name = name))

	def codeWriter = stateVar.updater[String]((state, code) => state.copy(code = code))

	def descriptionWriter = stateVar.updater[String]((state, description) => state.copy(description = description))


val submitter = Observer[DiscountCouponState] { state =>
	state.hasErrors match
		case true => stateVar.update(_.copy(showErrors = true))
		case _ => dom.window.alert("error")
}

object DiscountCouponForm:
	lazy val node: HtmlElement = renderForm

	def renderForm =
		div(
			form(
				cls(""),
				onSubmit
					.preventDefault
					.mapTo(stateVar.now()) --> submitter,
				renderInput("Nome: ", _.nameError)(
					input(
						cls("form-control"),
						required(true),
						placeholder("Natal 10%"),
						controlled(
							value <-- stateVar.signal.map(_.name),
							onInput.mapToValue --> Writers.nameWriter
						)
					)
				),
				div(
					cls("actions"),
					button(
						cls(List("btn", "btn-primary", "btn-sm", "pull-right", "m-t-n-xs")),
						typ("submit"),
						"Salvar"
					)
				)
			)
		)

	def renderInput(l: String, error: DiscountCouponState => Option[String])(mods: Modifier[HtmlElement]*): HtmlElement =
		val $error = stateVar.signal.map(_.displayError(error))
		div(
			cls("form-group"),
			cls.toggle("has-error") <-- $error.map(_.nonEmpty),
			label(l),
			mods,
			child.maybe <-- $error.map(_.map(err => span(cls("help-block"), err)))
		)

class DiscountCouponForm:

	import DiscountCouponForm.*

	def build = node
