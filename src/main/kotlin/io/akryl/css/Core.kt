package io.akryl.css

import kotlin.browser.document
import kotlin.reflect.KProperty

class CssProperty(private val name: ClassName, private val builder: CssBuilder) {
  private var initialized = false

  private fun initialize() {
    if (!initialized) {
      initialized = true
      val text = buildString { stringify(name.selector, builder) }
      if (text.isNotBlank()) {
        appendToHead(text)
      }
    }
  }

  operator fun getValue(thisRef: Any?, prop: KProperty<*>): ClassName {
    initialize()
    return name
  }
}

class CssPropertyProvider(private val scoped: Boolean, private val block: RuleSet) {
  operator fun provideDelegate(
    thisRef: Any?,
    prop: KProperty<*>
  ): CssProperty {
    val name = generateName(scoped, prop)
    val style = CssBuilder().apply(block)
    return CssProperty(name, style)
  }
}

fun css(scoped: Boolean = true, block: RuleSet = {}): CssPropertyProvider {
  return CssPropertyProvider(scoped, block)
}

fun globalCss(block: RuleSet) {
  val style = CssBuilder().apply(block)
  val text = buildString { stringify("", style) }
  if (text.isNotBlank()) {
    appendToHead(text)
  }
}

fun classMap(vararg items: Pair<CharSequence, Boolean>) = items
  .filter { it.second }
  .map { it.first }

private fun generateName(scoped: Boolean, prop: KProperty<*>): ClassName {
  return if (scoped) {
    ClassName.random(prop.name)
  } else {
    ClassName(prop.name)
  }
}

private fun appendToHead(text: String) {
  val style = document.createElement("style")
  style.setAttribute("type", "text/css")
  style.innerHTML = text
  document.head!!.appendChild(style)
}