@file:JsModule("css-selector-parser")

package js.css_selector_parser

external class CssSelectorParser {
  fun registerSelectorPseudos(vararg names: String)
  fun registerNestingOperators(vararg names: String)
  fun registerAttrEqualityMods(vararg names: String)
  fun enableSubstitutes()
  fun parse(selector: String): Token
  fun render(token: Token): String
}