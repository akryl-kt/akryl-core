@file:Suppress("FunctionName")

package io.akryl

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import kotlin.browser.document
import kotlin.dom.clear
import kotlin.math.min

class HtmlWidget(
  val tag: String,
  val ns: String?,
  val cssPrefix: String?,
  val attributes: Map<String, String?> = emptyMap(),
  val style: Map<String, String?> = emptyMap(),
  val listeners: Map<String, (event: Event) -> Unit> = emptyMap(),
  val children: List<Widget> = emptyList(),
  val innerHtml: String? = null,
  key: Key? = null
) : Widget(key) {
  init {
    check(children.isEmpty() || innerHtml == null) { "Only one property of [children, innerHtml] can be set" }
  }

  override fun createElement(parent: RenderElement?) = HtmlRenderElement(parent, this)
}

class HtmlRenderElement(
  override val parent: RenderElement?,
  widget: HtmlWidget
) : RenderElement() {
  private val children = ArrayList<RenderElement>()

  override val node: Element = if (widget.ns != null) {
    document.createElementNS(widget.ns, widget.tag)
  } else {
    document.createElement(widget.tag)
  }

  init {
    widget.cssPrefix?.let { node.setAttribute(STYLE_ATTRIBUTE_NAME, it) }

    for ((k, v) in widget.attributes) {
      if (v != null) {
        node.setAttribute(k, v)
      }
    }

    val style = (node as? HTMLElement)?.style
    if (style != null) {
      for ((k, v) in widget.style) {
        if (v != null) {
          style.setProperty(k, v)
        }
      }
    }

    for ((k, v) in widget.listeners) {
      node.addEventListener(k, v)
    }

    for (child in widget.children) {
      val childElement = child.createElement(this)
      children.add(childElement)
      node.appendChild(childElement.node)
    }
    widget.innerHtml?.let { node.innerHTML = it }
  }

  override var widget: HtmlWidget = widget
    private set

  override fun mounted() {
    super.mounted()
    children.forEach { it.mounted() }
  }

  override fun update(newWidget: Widget, force: Boolean): Boolean {
    if (newWidget !is HtmlWidget) return false
    if (newWidget.tag != this.widget.tag) return false

    updateCssPrefix(newWidget)
    updateAttributes(newWidget.attributes)
    updateStyle(newWidget.style)
    updateListeners(newWidget.listeners)
    updateChildren(newWidget.children, newWidget.innerHtml, force)
    widget = newWidget

    return true
  }

  override fun unmounted() {
    super.unmounted()
    children.forEach { it.unmounted() }
  }

  private fun updateCssPrefix(newWidget: HtmlWidget) {
    if (newWidget.cssPrefix != widget.cssPrefix) {
      val prefix = newWidget.cssPrefix
      if (prefix != null) {
        node.setAttribute(STYLE_ATTRIBUTE_NAME, prefix)
      } else {
        node.removeAttribute(STYLE_ATTRIBUTE_NAME)
      }
    }
  }

  private fun updateChildren(newChildren: List<Widget>, newInnerHtml: String?, force: Boolean) {
    if (widget.innerHtml != newInnerHtml) {
      if (newInnerHtml != null) {
        node.innerHTML = newInnerHtml
      } else {
        node.clear()
      }
    }

    // todo key-based algorithm

    val commonSize = min(children.size, newChildren.size)

    for (i in 0 until commonSize) {
      val newChild = newChildren[i]
      children[i] = update(this, children[i], newChild, force)
    }

    for (i in (children.size - 1) downTo commonSize) {
      node.removeChild(children[i].node)
      val oldChild = children.removeAt(i)
      oldChild.unmounted()
    }

    for (i in commonSize until newChildren.size) {
      val newChild = newChildren[i].createElement(this)
      children.add(newChild)
      node.appendChild(newChild.node)
      newChild.mounted()
    }
  }

  private fun updateStyle(newStyle: Map<String, String?>) {
    val style = (node as? HTMLElement)?.style ?: return

    for ((k, oldValue) in widget.style) {
      val newValue = newStyle[k]
      if (oldValue != newValue) {
        updateProp(style, k, newValue)
      }
    }

    for ((k, newValue) in newStyle) {
      val oldValue = widget.style[k]
      if (oldValue == null && newValue != null) {
        updateProp(style, k, newValue)
      }
    }
  }

  private fun updateAttributes(newAttributes: Map<String, String?>) {
    for ((k, oldValue) in widget.attributes) {
      val newValue = newAttributes[k]
      if (oldValue != newValue) {
        updateProp(k, newValue)
      }
    }

    for ((k, newValue) in newAttributes) {
      val oldValue = widget.attributes[k]
      if (oldValue == null && newValue != null) {
        updateProp(k, newValue)
      }
    }
  }

  private fun updateListeners(newListeners: Map<String, (Event) -> Unit>) {
    for ((k, oldValue) in widget.listeners) {
      val newValue = newListeners[k]
      if (oldValue != newValue) {
        node.removeEventListener(k, oldValue)
        if (newValue != null) {
          node.addEventListener(k, newValue)
        }
      }
    }

    for ((k, newValue) in newListeners) {
      val oldValue = widget.style[k]
      if (oldValue == null) {
        node.addEventListener(k, newValue)
      }
    }
  }

  private fun updateProp(style: CSSStyleDeclaration, k: String, v: String?) {
    if (v != null) {
      style.setProperty(k, v)
    } else {
      style.removeProperty(k)
    }
  }

  private fun updateProp(k: String, v: String?) {
    if (v != null) {
      node.setAttribute(k, v)
    } else {
      node.removeAttribute(k)
    }
  }
}

class Text(
  val value: String
) : Widget() {
  override fun createElement(parent: RenderElement?) = TextRenderElement(parent, this)
}

class TextRenderElement(
  override val parent: RenderElement?,
  widget: Text
) : RenderElement() {
  override val node: Node = document.createTextNode(widget.value)

  override var widget: Text = widget
    private set

  override fun update(newWidget: Widget, force: Boolean): Boolean {
    if (newWidget !is Text) return false
    if (newWidget.value != widget.value) {
      node.textContent = newWidget.value
    }
    widget = newWidget
    return true
  }
}

@Suppress("UNCHECKED_CAST")
private fun listeners(vararg items: Pair<String, Any?>) = items
  .filter { it.second != null }
  .map { Pair(it.first, it.second as (Event) -> Unit) }
  .toMap()

fun classMap(vararg items: Pair<String, Boolean>) = items
  .filter { it.second }
  .map { it.first }

// todo better types
class HtmlWidgetFactory(
  private val tag: String,
  private val ns: String? = null,
  private val styled: Styled? = null
) {
  operator fun invoke(
    key: Key? = null,
    // attributes
    d: String? = null,
    disabled: Boolean = false,
    id: String? = null,
    `for`: String? = null,
    height: Int? = null,
    href: String? = null,
    placeholder: String? = null,
    role: String? = null,
    rows: Int? = null,
    selected: Boolean = false,
    src: String? = null,
    tabIndex: Int? = null,
    target: String? = null,
    title: String? = null,
    type: String? = null,
    value: String? = null,
    width: Int? = null,
    // style
    clazz: String? = null,
    classes: List<String?> = emptyList(),
    style: Map<String, String?> = emptyMap(),
    // events
    onBlur: ((event: Event) -> Unit)? = null,
    onChange: ((event: Event) -> Unit)? = null,
    onClick: ((event: Event) -> Unit)? = null,
    onDoubleClick: ((event: Event) -> Unit)? = null,
    onKeyDown: ((event: KeyboardEvent) -> Unit)? = null,
    onKeyUp: ((event: KeyboardEvent) -> Unit)? = null,
    onMouseUp: ((event: MouseEvent) -> Unit)? = null,
    onMouseDown: ((event: MouseEvent) -> Unit)? = null,
    onMouseMove: ((event: MouseEvent) -> Unit)? = null,
    // children
    child: Widget? = null,
    children: List<Widget> = emptyList(),
    innerHtml: String? = null
  ) = HtmlWidget(
    tag = tag,
    ns = ns,
    cssPrefix = styled?.style()?.prefix,
    attributes = mapOf(
      "d" to d,
      "disabled" to if (disabled) "" else null,
      "id" to id,
      "height" to height?.toString(),
      "for" to `for`,
      "href" to href,
      "placeholder" to placeholder,
      "role" to role,
      "rows" to rows?.toString(),
      "selected" to if (selected) "" else null,
      "src" to src,
      "tabindex" to tabIndex?.toString(),
      "target" to target,
      "title" to title,
      "type" to type,
      "value" to value,
      "width" to width?.toString(),
      "class" to (classes + clazz)
        .filterNotNull()
        .joinToString(" ")
        .ifEmpty { null }
    ),
    style = style,
    listeners = listeners(
      "blur" to onBlur,
      "click" to onClick,
      "dblclick" to onDoubleClick,
      "mouseup" to onMouseUp,
      "mousedown" to onMouseDown,
      "mousemove" to onMouseMove,
      "change" to onChange,
      "keydown" to onKeyDown,
      "keyup" to { event: KeyboardEvent -> onChange?.invoke(event); onKeyUp?.invoke(event) }
    ),
    children = if (child != null) children + child else children,
    innerHtml = innerHtml,
    key = key
  )
}

val A = HtmlWidgetFactory("a")
val B = HtmlWidgetFactory("b")
val Button = HtmlWidgetFactory("button")
val Div = HtmlWidgetFactory("div")
val Img = HtmlWidgetFactory("img")
val Input = HtmlWidgetFactory("input")
val H1 = HtmlWidgetFactory("h1")
val H2 = HtmlWidgetFactory("h2")
val H3 = HtmlWidgetFactory("h3")
val H4 = HtmlWidgetFactory("h4")
val H5 = HtmlWidgetFactory("h5")
val H6 = HtmlWidgetFactory("h6")
val Label = HtmlWidgetFactory("label")
val Option = HtmlWidgetFactory("option")
val Select = HtmlWidgetFactory("select")
val Span = HtmlWidgetFactory("span")
val Table get() = HtmlWidgetFactory("table")
val THead get() = HtmlWidgetFactory("thead")
val TBody get() = HtmlWidgetFactory("tbody")
val TD get() = HtmlWidgetFactory("td")
val TextArea = HtmlWidgetFactory("textarea")
val TH get() = HtmlWidgetFactory("th")
val TR get() = HtmlWidgetFactory("tr")

val Path = HtmlWidgetFactory("path", "http://www.w3.org/2000/svg")
val Svg = HtmlWidgetFactory("svg", "http://www.w3.org/2000/svg")

val Styled.A get() = HtmlWidgetFactory("a", styled = this)
val Styled.B get() = HtmlWidgetFactory("b", styled = this)
val Styled.Button get() = HtmlWidgetFactory("button", styled = this)
val Styled.Div get() = HtmlWidgetFactory("div", styled = this)
val Styled.Img get() = HtmlWidgetFactory("img", styled = this)
val Styled.Input get() = HtmlWidgetFactory("input", styled = this)
val Styled.H1 get() = HtmlWidgetFactory("h1", styled = this)
val Styled.H2 get() = HtmlWidgetFactory("h2", styled = this)
val Styled.H3 get() = HtmlWidgetFactory("h3", styled = this)
val Styled.H4 get() = HtmlWidgetFactory("h4", styled = this)
val Styled.H5 get() = HtmlWidgetFactory("h5", styled = this)
val Styled.H6 get() = HtmlWidgetFactory("h6", styled = this)
val Styled.Label get() = HtmlWidgetFactory("label", styled = this)
val Styled.Option get() = HtmlWidgetFactory("option", styled = this)
val Styled.Select get() = HtmlWidgetFactory("select", styled = this)
val Styled.Span get() = HtmlWidgetFactory("span", styled = this)
val Styled.Table get() = HtmlWidgetFactory("table", styled = this)
val Styled.THead get() = HtmlWidgetFactory("thead", styled = this)
val Styled.TBody get() = HtmlWidgetFactory("tbody", styled = this)
val Styled.TD get() = HtmlWidgetFactory("td", styled = this)
val Styled.TextArea get() = HtmlWidgetFactory("textarea", styled = this)
val Styled.TH get() = HtmlWidgetFactory("th", styled = this)
val Styled.TR get() = HtmlWidgetFactory("tr", styled = this)

val Styled.Path get() = HtmlWidgetFactory("path", "http://www.w3.org/2000/svg", styled = this)
val Styled.Svg get() = HtmlWidgetFactory("svg", "http://www.w3.org/2000/svg", styled = this)
