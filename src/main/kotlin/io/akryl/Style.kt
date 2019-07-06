package io.akryl

import js.HMR
import js.css_selector_parser.AttrToken
import js.css_selector_parser.CssSelectorParser
import js.css_selector_parser.RuleToken
import js.css_selector_parser.Token
import kotlin.browser.document

private fun camelToDashed(value: String): String {
  return buildString {
    for (c in value) {
      if (c in 'A'..'Z') {
        append('-')
        append(c.toLowerCase())
      } else {
        append(c)
      }
    }
  }
}

class SelectorScope(private val selectors: List<String>) {
  private val data = HashMap<String, String>()
  private val inner = ArrayList<SelectorScope>()

  var alignItems by data
  var alignSelf by data
  var background by data
  var border by data
  var borderColor by data
  var borderRadius by data
  var borderTop by data
  var bottom by data
  var boxShadow by data
  var color by data
  var content by data
  var cursor by data
  var display by data
  var fill by data
  var filter by data
  var flex by data
  var flexDirection by data
  var flexFlow by data
  var font by data
  var fontFamily by data
  var fontSize by data
  var fontStyle by data
  var fontWeight by data
  var gridColumn by data
  var gridRow by data
  var gridTemplateColumns by data
  var gridTemplateRows by data
  var height by data
  var justifyContent by data
  var left by data
  var lineHeight by data
  var margin by data
  var marginBottom by data
  var marginLeft by data
  var marginRight by data
  var marginTop by data
  var maxHeight by data
  var maxWidth by data
  var minHeight by data
  var minWidht by data
  var objectFit by data
  var objectPosition by data
  var overflow by data
  var overflowX by data
  var overflowY by data
  var outline by data
  var padding by data
  var paddingTop by data
  var position by data
  var right by data
  var stroke by data
  var strokeWidth by data
  var textAlign by data
  var textDecoration by data
  var textOverflow by data
  var top by data
  var transition by data
  var visibility by data
  var width by data
  var willChange by data
  var whiteSpace by data
  var wordBreak by data
  var userSelect by data
  var zIndex by data

  operator fun String.invoke(block: SelectorScope.() -> Unit) {
    inner.add(SelectorScope(selectors + this).apply(block))
  }

  internal fun build(prefix: String?, builder: StringBuilder) {
    for (selector in selectors) {
      val prefixedSelector = addPrefixToSelector(selector, prefix)
      builder.append(prefixedSelector)
      builder.append(' ')
    }
    builder.append("{")

    for ((k, v) in data) {
      builder.append(camelToDashed(k))
      builder.append(":")
      builder.append(v)
      builder.append(";")
    }

    builder.append('}')

    inner.forEach { it.build(prefix, builder) }
  }
}

class StyleScope {
  private val selectors = ArrayList<SelectorScope>()

  operator fun String.invoke(block: SelectorScope.() -> Unit) {
    selectors.add(SelectorScope(listOf(this)).apply(block))
  }

  internal fun build(prefix: String?): String {
    val sb = StringBuilder()
    selectors.forEach { it.build(prefix, sb) }
    return sb.toString()
  }
}

private class StyleImpl(
  val scoped: Boolean,
  val scope: StyleScope
) : Style {
  private var built = false
  private var _prefix: String? = null

  override val prefix by lazy {
    build()
    _prefix
  }

  override fun build() {
    if (!built) {
      built = true

      _prefix = if (scoped) {
        (scope.hashCode().toLong() and 0xFFFFFFFFL).toString(16) // todo random prefix
      } else {
        null
      }

      val text = scope.build(_prefix)

      val css = document.createElement("style")
      css.setAttribute("type", "text/css")
      css.innerHTML = text
      document.head!!.appendChild(css)

      HMR.dispose {
        document.head!!.removeChild(css)
      }
    }
  }
}

fun css(scoped: Boolean = true, block: StyleScope.() -> Unit): Style {
  return StyleImpl(scoped, StyleScope().apply(block))
}

val Int.vw get() = "${this}vw"
val Int.vh get() = "${this}vh"
val Int.px get() = "${this}px"
val Double.em get() = "${this}em"



private val cssSelectorParser by lazy {
  CssSelectorParser().apply {
    registerSelectorPseudos("not")
    registerNestingOperators(">", "+", "~")
    registerAttrEqualityMods("^", "$", "*", "~")
    enableSubstitutes()
  }
}

private fun addPrefixToSelector(selector: String, prefix: String?): String {
  if (prefix == null) return selector

  val ast = cssSelectorParser.parse(selector)

  val styleToken = AttrToken(
    name = STYLE_ATTRIBUTE_NAME,
    operator = "=",
    valueType = "string",
    value = prefix
  )

  visitRules(ast) { token ->
    if (token.classNames != null || token.tagName != null) {
      token.attrs = (token.attrs ?: emptyArray()) + styleToken
    }
  }

  return cssSelectorParser.render(ast)
}

private fun visitRules(token: Token, visitor: (token: RuleToken) -> Unit) {
  token.rule?.let { rule ->
    visitRules(rule, visitor)
  }

  token.selectors?.forEach { visitRules(it, visitor) }

  token.pseudos?.forEach { pseudo ->
    if (pseudo.valueType == "selector") {
      visitRules(pseudo.value.unsafeCast<Token>(), visitor)
    }
  }

  if (token.type == "rule") {
    visitor(token.unsafeCast<RuleToken>())
  }
}
