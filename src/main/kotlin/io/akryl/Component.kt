@file:Suppress("unused")

package io.akryl

import io.akryl.css.Styled
import io.akryl.react.FunctionalComponent
import io.akryl.react.ReactNode
import io.akryl.react.createElement
import io.akryl.react.memo
import io.akryl.rx.ChangeDetector
import io.akryl.rx.ReactiveHandle
import js.DataPropertyDescriptor
import js.JsObject
import org.w3c.dom.Element
import kotlin.reflect.KClass
import io.akryl.react.dom.render as reactRender
import io.akryl.react.useRef as reactUseRef
import io.akryl.react.useState as reactUseState

abstract class Component(
  val key: Any? = undefined
) : ReactNode, Styled {
  final override val prefix: String? get() = null // todo

  abstract fun render(): ReactNode
}

fun render(component: Component, container: Element) {
  reactRender(build(component), container)
}

fun render(node: ReactNode, container: Element) {
  reactRender(build(node), container)
}

internal class Wrapper(var inner: dynamic, clazz: KClass<*>) {
  val render: FunctionalComponent

  init {
    var tmp: FunctionalComponent = { props ->
      observer {
        val tree = inner.apply(props)
        build(tree.unsafeCast<ReactNode>())
      }
    }

    val name = clazz.simpleName
    JsObject.defineProperty(tmp, "name", DataPropertyDescriptor(value = name))

    val equals = clazz.js.asDynamic().prototype.equals
    if (equals != null) {
      tmp = memo(tmp, ::propsEquals)
    }

    render = tmp
  }
}

internal val wrappers = HashMap<KClass<*>, Wrapper>()

fun build(node: ReactNode): ReactNode {
  if (node !is Component) return node
  val clazz = node::class

  var wrapper = wrappers[clazz]
  if (wrapper == null) {
    val innerRender = node.asDynamic().render
    wrapper = Wrapper(innerRender, clazz)
    wrappers[clazz] = wrapper
  }

  return createElement(wrapper.render, node)
}

@Suppress("USELESS_CAST")
private fun propsEquals(a: dynamic, b: dynamic): Boolean {
  val keysA = JsObject.keys(a.unsafeCast<Any>())
  val keysB = JsObject.keys(b.unsafeCast<Any>())
  if (keysA.size != keysB.size) return false

  for (k in keysA) {
    if ((a[k] as Any?) != (b[k] as Any?)) return false
  }

  return true
}

private fun <R> observer(block: () -> R): R {
  val ref = reactUseRef<ReactiveHandle?>(null)
  val (_, setState) = reactUseState(null)

  val (result, handle) = ChangeDetector.evaluate(block) {
    ref.current?.dispose()
    setState(js("{}"))
    Unit
  }

  ref.current = handle

  useEffect(Unit) {
    dispose {
      ref.current?.dispose()
      ref.current = null
    }
  }

  return result
}