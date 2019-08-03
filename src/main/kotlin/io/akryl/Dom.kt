package io.akryl

import org.w3c.dom.*
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.dom.clear

// todo too much casting. better typing? generics?

interface DomFactory {
  fun createNode(tag: String, namespace: String? = null): DomNode
  fun createText(text: String): DomNode
}

interface DomNode {
  var textContent: String?
  var innerHTML: String

  fun appendChild(node: DomNode)
  fun insertBefore(index: Int, node: DomNode)
  fun remove()
  fun replace(newNode: DomNode)
  fun clear()

  fun setAttribute(qualifiedName: String, value: String)
  fun removeAttribute(qualifiedName: String)

  fun setStyle(property: String, value: String)
  fun removeStyle(property: String)

  fun addEventListener(type: String, callback: (event: Event) -> Unit)
  fun removeEventListener(type: String, callback: (event: Event) -> Unit)
}

object ReadDomFactory : DomFactory {
  override fun createNode(tag: String, namespace: String?): DomNode {
    val child = if (namespace != null) document.createElementNS(namespace, tag) else document.createElement(tag)
    return RealDomNode(child)
  }

  override fun createText(text: String): DomNode {
    val child = document.createTextNode(text)
    return RealDomNode(child)
  }
}

class RealDomNode(
  val inner: Node
) : DomNode {
  override var textContent: String?
    get() = (inner as Text).textContent
    set(value) { (inner as Text).textContent = value }

  override var innerHTML: String
    get() = (inner as Element).innerHTML
    set(value) { (inner as Element).innerHTML = value }

  private val element by lazy { inner as Element }
  private val htmlElement by lazy { inner as HTMLElement }

  override fun appendChild(node: DomNode) {
    node as RealDomNode
    inner.appendChild(node.inner)
  }

  override fun insertBefore(index: Int, node: DomNode) {
    node as RealDomNode
    inner.insertBefore(node.inner, inner.childNodes[index])
  }

  override fun remove() {
    inner.parentNode!!.removeChild(inner)
  }

  override fun replace(newNode: DomNode) {
    newNode as RealDomNode
    inner.parentNode!!.replaceChild(newNode.inner, inner)
  }

  override fun clear() {
    inner.clear()
  }

  override fun setAttribute(qualifiedName: String, value: String) {
    element.setAttribute(qualifiedName, value)
  }

  override fun removeAttribute(qualifiedName: String) {
    element.removeAttribute(qualifiedName)
  }

  override fun setStyle(property: String, value: String) {
    htmlElement.style.setProperty(property, value)
  }

  override fun removeStyle(property: String) {
    htmlElement.style.removeProperty(property)
  }

  override fun addEventListener(type: String, callback: (event: Event) -> Unit) {
    element.addEventListener(type, callback)
  }

  override fun removeEventListener(type: String, callback: (event: Event) -> Unit) {
    element.removeEventListener(type, callback)
  }
}