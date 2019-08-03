package io.akryl

import io.akryl.html.HtmlWidget
import io.akryl.html.Text
import org.w3c.dom.events.Event
import kotlin.test.assertEquals

object TestDomFactory : DomFactory {
  override fun createNode(tag: String, namespace: String?): DomNode = TestDomNode(tag, namespace)
  override fun createText(text: String): DomNode = TestDomNode("text", null, textContent = text)
}

data class TestDomNode(
  val tag: String,
  val namespace: String?,
  val children: ArrayList<TestDomNode> = ArrayList(),
  val attributes: HashMap<String, String?> = HashMap(),
  val style: HashMap<String, String?> = HashMap(),
  val listeners: HashMap<String, ArrayList<(event: Event) -> Unit>> = HashMap(),
  override var textContent: String? = null
) : DomNode {
  var parent: TestDomNode? = null
    private set

  override var innerHTML: String
    get() = children.joinToString("")
    set(_) = throw IllegalStateException()

  override fun appendChild(node: DomNode) {
    node as TestDomNode
    check(node.parent == null) { "Node $node already has parent" }
    children.add(node)
    node.parent = this
  }

  override fun insertBefore(index: Int, node: DomNode) {
    node as TestDomNode
    val nodeParent = node.parent
    if (nodeParent != null) {
      nodeParent.children.removeAll { it === node }
      node.parent = null
    }
    children.add(index, node)
    node.parent = this
  }

  override fun remove() {
    val parent = checkNotNull(parent) { "Node $this has no parent" }
    parent.children.removeAll { it === this }
    this.parent = null
  }

  override fun replace(newNode: DomNode) {
    newNode as TestDomNode
    val parent = checkNotNull(parent) { "Node $this has no parent" }
    check(newNode.parent == null) { "Node $newNode already has parent" }
    val index = parent.children.indexOfFirst { it === this }
    parent.children[index] = newNode
    newNode.parent = parent
    this.parent = null
  }

  override fun clear() {
    children.forEach { it.parent = null }
    children.clear()
  }

  override fun setAttribute(qualifiedName: String, value: String) {
    attributes[qualifiedName] = value
  }

  override fun removeAttribute(qualifiedName: String) {
    attributes.remove(qualifiedName)
  }

  override fun setStyle(property: String, value: String) {
    style[property] = value
  }

  override fun removeStyle(property: String) {
    style.remove(property)
  }

  override fun addEventListener(type: String, callback: (event: Event) -> Unit) {
    val typeListeners = listeners[type] ?: ArrayList()
    typeListeners.add(callback)
    listeners[type] = typeListeners
  }

  override fun removeEventListener(type: String, callback: (event: Event) -> Unit) {
    val typeListeners = listeners[type] ?: ArrayList()
    typeListeners.remove(callback)
    if (typeListeners.size == 0) {
      listeners.remove(type)
    } else {
      listeners[type] = typeListeners
    }
  }

  override fun toString(): String {
    if (tag == "text" && namespace == null) {
      return textContent ?: ""
    }

    var attrs: Map<String, String?> = attributes
    if (style.isNotEmpty()) {
      val styleStr = style.entries
        .joinToString("; ") { "${it.key}: ${it.value}" }
      attrs += ("style" to styleStr)
    }

    var attrStr = attrs.entries.joinToString(" ") { "${it.key}='${it.value}'" }
    if (attrStr.isNotEmpty()) attrStr = " $attrStr"

    val innerHtml = innerHTML
    val tail = if (innerHtml.isEmpty()) "/>" else ">$innerHtml</$tag>"

    return "<$tag$attrStr$tail"
  }
}

class TestRootElement : RenderElement() {
  override val prefix = ""
  override val widget get() = throw IllegalStateException()
  override val parent = this
  override val factory = TestDomFactory
  override val node: DomNode = TestDomNode("div", null)

  override fun update(newWidget: Widget, force: Boolean) = throw IllegalStateException()
  override fun ancestorStateOf(predicate: (state: State<*>) -> Boolean): State<*>? = null
}

fun fakeMount(widget: Widget): MountRef {
  return mount(TestRootElement(), widget)
}

fun assertHtml(expected: String, actual: MountRef) {
  assertEquals(expected, actual.parent.node.innerHTML)
}

fun assertHtml(expected: Widget, actual: MountRef) {
  val node = expected.toTestNode()
  assertEquals(node, actual.element.node)
}

fun Widget.toTestNode(): TestDomNode = when (this) {
  is HtmlWidget -> TestDomNode(
    tag,
    ns,
    children.mapTo(ArrayList()) { it.toTestNode() },
    HashMap(attributes),
    HashMap(style),
    listeners.mapValuesTo(HashMap()) { arrayListOf(it.value) }
  )
  is Text -> TestDomNode(tag = "text", namespace = null, textContent = value)
  else -> throw IllegalStateException("Unexpected widget in test tree '$this'")
}

fun mountTest(widget: Widget): MountRef {
  val ref = fakeMount(widget)
  assertHtml(widget, ref)
  return ref
}

fun MountRef.rebuildTest(widget: Widget) {
  this.rebuild(widget)
  assertHtml(widget, this)
}