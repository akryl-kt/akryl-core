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

  var alignContent by data
  var alignItems by data
  var alignSelf by data
  var all by data
  var animation by data
  var animationDelay by data
  var animationDirection by data
  var animationDuration by data
  var animationFillMode by data
  var animationIterationCount by data
  var animationName by data
  var animationPlayState by data
  var animationTimingFunction by data
  var backfaceVisibility by data
  var background by data
  var backgroundAttachment by data
  var backgroundBlendMode by data
  var backgroundClip by data
  var backgroundColor by data
  var backgroundImage by data
  var backgroundOrigin by data
  var backgroundPosition by data
  var backgroundRepeat by data
  var backgroundSize by data
  var border by data
  var borderBottom by data
  var borderBottomColor by data
  var borderBottomLeftRadius by data
  var borderBottomRightRadius by data
  var borderBottomStyle by data
  var borderBottomWidth by data
  var borderCollapse by data
  var borderColor by data
  var borderImage by data
  var borderImageOutset by data
  var borderImageRepeat by data
  var borderImageSlice by data
  var borderImageSource by data
  var borderImageWidth by data
  var borderLeft by data
  var borderLeftColor by data
  var borderLeftStyle by data
  var borderLeftWidth by data
  var borderRadius by data
  var borderRight by data
  var borderRightColor by data
  var borderRightStyle by data
  var borderRightWidth by data
  var borderSpacing by data
  var borderStyle by data
  var borderTop by data
  var borderTopColor by data
  var borderTopLeftRadius by data
  var borderTopRightRadius by data
  var borderTopStyle by data
  var borderTopWidth by data
  var borderWidth by data
  var bottom by data
  var boxDecorationBreak by data
  var boxShadow by data
  var boxSizing by data
  var captionSide by data
  var caretColor by data
  var clear by data
  var clip by data
  var color by data
  var columnCount by data
  var columnFill by data
  var columnGap by data
  var columnRule by data
  var columnRuleColor by data
  var columnRuleStyle by data
  var columnRuleWidth by data
  var columnSpan by data
  var columnWidth by data
  var columns by data
  var content by data
  var counterIncrement by data
  var counterReset by data
  var cursor by data
  var direction by data
  var display by data
  var emptyCells by data
  var filter by data
  var flex by data
  var flexBasis by data
  var flexDirection by data
  var flexFlow by data
  var flexGrow by data
  var flexShrink by data
  var flexWrap by data
  var float by data
  var font by data
  var fontFamily by data
  var fontKerning by data
  var fontSize by data
  var fontSizeAdjust by data
  var fontStretch by data
  var fontStyle by data
  var fontVariant by data
  var fontWeight by data
  var grid by data
  var gridArea by data
  var gridAutoColumns by data
  var gridAutoFlow by data
  var gridAutoRows by data
  var gridColumn by data
  var gridColumnEnd by data
  var gridColumnGap by data
  var gridColumnStart by data
  var gridGap by data
  var gridRow by data
  var gridRowEnd by data
  var gridRowGap by data
  var gridRowStart by data
  var gridTemplate by data
  var gridTemplateAreas by data
  var gridTemplateColumns by data
  var gridTemplateRows by data
  var hangingPunctuation by data
  var height by data
  var hyphens by data
  var isolation by data
  var justifyContent by data
  var left by data
  var letterSpacing by data
  var lineHeight by data
  var listStyle by data
  var listStyleImage by data
  var listStylePosition by data
  var listStyleType by data
  var margin by data
  var marginBottom by data
  var marginLeft by data
  var marginRight by data
  var marginTop by data
  var maxHeight by data
  var maxWidth by data
  var minHeight by data
  var minWidth by data
  var mixBlendMode by data
  var objectFit by data
  var objectPosition by data
  var opacity by data
  var order by data
  var outline by data
  var outlineColor by data
  var outlineOffset by data
  var outlineStyle by data
  var outlineWidth by data
  var overflow by data
  var overflowX by data
  var overflowY by data
  var padding by data
  var paddingBottom by data
  var paddingLeft by data
  var paddingRight by data
  var paddingTop by data
  var pageBreakAfter by data
  var pageBreakBefore by data
  var pageBreakInside by data
  var perspective by data
  var perspectiveOrigin by data
  var pointerEvents by data
  var position by data
  var quotes by data
  var resize by data
  var right by data
  var scrollBehavior by data
  var tabSize by data
  var tableLayout by data
  var textAlign by data
  var textAlignLast by data
  var textDecoration by data
  var textDecorationColor by data
  var textDecorationLine by data
  var textDecorationStyle by data
  var textIndent by data
  var textJustify by data
  var textOverflow by data
  var textShadow by data
  var textTransform by data
  var top by data
  var transform by data
  var transformOrigin by data
  var transformStyle by data
  var transition by data
  var transitionDelay by data
  var transitionDuration by data
  var transitionProperty by data
  var transitionTimingFunction by data
  var unicodeBidi by data
  var userSelect by data
  var verticalAlign by data
  var visibility by data
  var whiteSpace by data
  var width by data
  var wordBreak by data
  var wordSpacing by data
  var wordWrap by data
  var writingMode by data
  var zIndex by data

  operator fun String.invoke(block: SelectorScope.() -> Unit) {
    inner.add(SelectorScope(selectors + this).apply(block))
  }

  fun set(key: String, value: String) {
    data[key] = value
  }

  fun get(key: String): String? {
    return data[key]
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

val Int.cm get() = "${this}cm"
val Int.mm get() = "${this}mm"
val Int.inch get() = "${this}in"
val Int.px get() = "${this}px"
val Int.pt get() = "${this}pt"
val Int.pc get() = "${this}pc"
val Int.em get() = "${this}em"
val Int.ex get() = "${this}ex"
val Int.ch get() = "${this}ch"
val Int.rem get() = "${this}rem"
val Int.vw get() = "${this}vw"
val Int.vh get() = "${this}vh"
val Int.vmin get() = "${this}vmin"
val Int.vmax get() = "${this}vmax"
val Int.percent get() = "${this}%"

val Double.cm get() = "${this}cm"
val Double.mm get() = "${this}mm"
val Double.inch get() = "${this}in"
val Double.px get() = "${this}px"
val Double.pt get() = "${this}pt"
val Double.pc get() = "${this}pc"
val Double.em get() = "${this}em"
val Double.ex get() = "${this}ex"
val Double.ch get() = "${this}ch"
val Double.rem get() = "${this}rem"
val Double.vw get() = "${this}vw"
val Double.vh get() = "${this}vh"
val Double.vmin get() = "${this}vmin"
val Double.vmax get() = "${this}vmax"
val Double.percent get() = "${this}%"

val Float.cm get() = "${this}cm"
val Float.mm get() = "${this}mm"
val Float.inch get() = "${this}in"
val Float.px get() = "${this}px"
val Float.pt get() = "${this}pt"
val Float.pc get() = "${this}pc"
val Float.em get() = "${this}em"
val Float.ex get() = "${this}ex"
val Float.ch get() = "${this}ch"
val Float.rem get() = "${this}rem"
val Float.vw get() = "${this}vw"
val Float.vh get() = "${this}vh"
val Float.vmin get() = "${this}vmin"
val Float.vmax get() = "${this}vmax"
val Float.percent get() = "${this}%"



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

  if (token.type == "rule") {
    visitor(token.unsafeCast<RuleToken>())
  }
}
