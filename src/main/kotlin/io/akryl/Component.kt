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
import kotlin.js.json
import kotlin.reflect.KClass
import io.akryl.react.dom.render as reactRender
import io.akryl.react.useRef as reactUseRef
import io.akryl.react.useState as reactUseState

private const val THIS_KEY = "\$this"

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
        val tree = inner.apply(props[THIS_KEY])
        build(tree.unsafeCast<ReactNode>())
      }
    }

    // Defines the name of the React Component to be the name of the `clazz`.
    val name = clazz.simpleName
    JsObject.defineProperty(tmp, "name", DataPropertyDescriptor(value = name))

    // If `equals` is overridden, then we assume, that this is a Pure Component,
    // so it must be wrapped into the `memo`.
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

  // The `node` will lose its prototype if we pass it as a props object.
  // So it's passed inside `THIS_KEY` property.
  val props = json(THIS_KEY to node)

  return createElement(wrapper.render, props)
}

@Suppress("USELESS_CAST")
private fun propsEquals(a: dynamic, b: dynamic): Boolean {
  val aThis = a[THIS_KEY].unsafeCast<Any>()
  val bThis = b[THIS_KEY].unsafeCast<Any>()
  return aThis == bThis
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