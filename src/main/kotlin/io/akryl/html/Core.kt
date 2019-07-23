@file:Suppress("FunctionName")

package io.akryl.html

import io.akryl.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.dom.events.Event
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
  override val prefix = ""

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
      updateSpecialAttribute(node, k, newValue)
    }

    for ((k, newValue) in newAttributes) {
      val oldValue = widget.attributes[k]
      if (oldValue == null && newValue != null) {
        updateProp(k, newValue)
      }
      if (newValue != null) {
        updateSpecialAttribute(node, k, newValue)
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
  override val prefix = ""
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

fun classMap(vararg items: Pair<String, Boolean>) = items
  .filter { it.second }
  .map { it.first }

private fun updateSpecialAttribute(node: dynamic, k: String, v: String?) {
  when (k) {
    "checked" -> node.checked = v != null
    "value" -> node.value = v
  }
}