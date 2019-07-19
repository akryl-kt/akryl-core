package io.akryl

import io.akryl.rx.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import kotlin.browser.window
import kotlin.reflect.KClass

interface Style {
  fun build(prefix: String): Element
}

interface Styled {
  val prefix: String
  fun style(): Style?
}

object StyleRegistry {
  private val styles = HashMap<KClass<*>, Element?>()

  fun register(styled: Styled) {
    val clazz = styled::class
    if (clazz in styles) return
    styles[clazz] = styled.style()?.build(styled.prefix)
  }

  fun clear() {
    val snapshot = ArrayList(styles.values)
    styles.clear()
    window.requestAnimationFrame {
      for (style in snapshot) {
        style?.parentElement?.removeChild(style)
      }
    }
  }
}

internal const val STYLE_ATTRIBUTE_NAME = "data-id"

abstract class Key : Transient {
  private var _element: RenderElement? = null
  val element get() = _element ?: throw RuntimeException("Key not mounted")

  internal fun mount(element: RenderElement) {
    _element = element
  }

  internal fun unmount() {
    _element = null
  }
}

@Suppress("UNCHECKED_CAST")
class NativeKey<out N : Node> : Key() {
  val node get() = element.node as N
}

data class ValueKey(val value: Any) : Key()

abstract class Widget(val key: Key? = null) : Transient {
  abstract fun createElement(parent: RenderElement?): RenderElement
}

interface BuildContext : Styled {
  val widget: Widget
  val node: Node
  val isMounted: Boolean

  fun <T : Any> ancestorStateOf(clazz: KClass<T>): T?
  fun ancestorStateOf(predicate: (state: State<*>) -> Boolean): State<*>?
}

inline fun <reified T : Any> BuildContext.ancestorStateOf() = ancestorStateOf(T::class)

abstract class RenderElement : BuildContext {
  abstract override val widget: Widget
  abstract val parent: RenderElement?
  abstract override val node: Node
  override fun style(): Style? = null

  final override var isMounted: Boolean = false
    private set

  open fun mounted() {
    check(!isMounted) { "RenderElement already mounted" }
    isMounted = true
    widget.key?.mount(this)
  }

  abstract fun update(newWidget: Widget, force: Boolean): Boolean

  open fun unmounted() {
    check(isMounted) { "RenderElement is not mounted" }
    isMounted = false
    widget.key?.unmount()
  }

  override fun <T : Any> ancestorStateOf(clazz: KClass<T>): T? {
    return parent?.ancestorStateOf(clazz)
  }

  override fun ancestorStateOf(predicate: (state: State<*>) -> Boolean): State<*>? {
    return parent?.ancestorStateOf(predicate)
  }
}

class RootRenderElement(override val node: Node) : RenderElement() {
  override val parent: RenderElement? = null
  override val widget: Widget get() = throw IllegalStateException()
  override val prefix: String = ""
  override fun update(newWidget: Widget, force: Boolean) = throw IllegalStateException()
}

class MountRef(val parent: RenderElement, var element: RenderElement) {
  fun unmount() {
    if (element.isMounted) {
      element.node.parentElement!!.removeChild(element.node)
      element.unmounted()
    }
  }

  fun rebuild(widget: Widget) {
    element = update(parent, element, widget, true)
  }
}

fun mount(element: Element, widget: Widget): MountRef {
  val parent = RootRenderElement(element)
  val root = widget.createElement(parent)
  parent.node.appendChild(root.node)
  root.mounted()
  return MountRef(parent, root)
}

internal fun update(parent: RenderElement, oldElement: RenderElement, newWidget: Widget, force: Boolean): RenderElement {
  if (oldElement.widget.key == newWidget.key && oldElement.update(newWidget, force)) return oldElement

  val newElement = newWidget.createElement(parent)
  val parentNode = oldElement.node.parentElement!!
  parentNode.replaceChild(newElement.node, oldElement.node)

  oldElement.unmounted()
  newElement.mounted()

  return newElement
}

class RebuildScheduler(private val block: () -> Unit) {
  private var scheduled = false

  fun schedule() {
    if (scheduled) return
    scheduled = true

    window.requestAnimationFrame {
      scheduled = false
      block()
    }
  }
}

private object ClassRandom {
  private val values = HashMap<KClass<*>, String>()

  fun generate(obj: Any): String {
    val clazz = obj::class
    var value = values[clazz]
    if (value != null) return value

    value = (obj.hashCode().toLong() and 0xFFFFFFFFL).toString(16)
    values[clazz] = value
    return value
  }
}

// todo common implementation for ReactiveContainer

abstract class StateMixin<T : StatefulWidget> : ReactiveContainer {
  final override var isInitialized = false
    private set

  @JsName("\$computedProperties")
  private val computedProperties = ArrayList<ReactiveHandle>()

  open fun created() {
    check(!isInitialized) { "State already initialized" }
    isInitialized = true
  }

  open fun mounted() {
    check(isInitialized) { "State is not initialized" }
  }

  open fun updated(oldWidget: T, newWidget: T) {}

  open fun unmounted() {
    check(isInitialized) { "State is not initialized" }
    computedProperties.forEach { it.dispose() }
  }

  final override fun registerReactiveHandle(handle: ReactiveHandle) {
    computedProperties.add(handle)
  }
}

abstract class State<T : StatefulWidget>(
  @JsName("\$context")
  val context: BuildContext
) : ReactiveContainer, Styled {
  @JsName("\$computedProperties")
  private val computedProperties = ArrayList<ReactiveHandle>()

  @JsName("\$mixins")
  private val mixins = ArrayList<StateMixin<in T>>()

  @Suppress("UNCHECKED_CAST")
  val widget: T get() = context.widget as T

  @JsName("\$nativeElement")
  val nativeElement get() = context.node as Element

  @JsName("\$isMounted")
  val isMounted get() = context.isMounted

  override val prefix get() = widget.prefix
  override fun style() = widget.style()

  final override var isInitialized = false
    private set

  open fun created() {
    check(!isInitialized) { "State already initialized" }
    isInitialized = true
    mixins.forEach { it.created() }
  }

  open fun mounted() {
    check(isInitialized) { "State is not initialized" }
    mixins.forEach { it.mounted() }
  }

  abstract fun build(context: BuildContext): Widget

  open fun updated(oldWidget: T) {
    check(isMounted) { "State is not mounted" }
    mixins.forEach { it.updated(oldWidget, widget) }
  }

  open fun unmounted() {
    mixins.forEach { it.unmounted() }
    computedProperties.forEach { it.dispose() }
  }

  final override fun registerReactiveHandle(handle: ReactiveHandle) {
    computedProperties.add(handle)
  }

  fun <M : StateMixin<in T>> use(mixin: M): M {
    val wrapped = observable(mixin)
    mixins.add(wrapped)
    return wrapped
  }
}

abstract class StatefulWidget(
  key: Key? = null
) : Widget(key), Styled {
  final override val prefix by lazy { ClassRandom.generate(this) }
  override fun style(): Style? = null
  abstract fun createState(context: BuildContext): State<*>

  @Suppress("UNCHECKED_CAST")
  final override fun createElement(parent: RenderElement?) = StatefulElement(parent, this)
}

class StatefulElement(
  override val parent: RenderElement?,
  widget: StatefulWidget
) : RenderElement() {
  // todo ReactiveProperty
  private val widgetProp = ObservableProperty()
  override var widget: StatefulWidget = widget
    get() {
      widgetProp.observed()
      return field
    }
    private set(value) {
      field = value
      widgetProp.fire()
    }

  override val node get() = inner.node
  override val prefix get() = widget.prefix
  override fun style() = widget.style()

  private val scheduler = RebuildScheduler { rebuild(false) }

  @Suppress("UNCHECKED_CAST")
  private val state = observable(widget.createState(this)) as State<StatefulWidget>

  private var inner = build().createElement(this)
  private var handle: ReactiveHandle? = null

  init {
    state.created()
  }

  override fun mounted() {
    super.mounted()
    inner.mounted()
    state.mounted()
  }

  @Suppress("UNCHECKED_CAST")
  override fun update(newWidget: Widget, force: Boolean): Boolean {
    if (newWidget::class != widget::class) return false

    if (force || widget != newWidget) {
      val oldWidget = widget
      widget = newWidget as StatefulWidget
      rebuild(force)
      state.updated(oldWidget)
    }

    return true
  }

  override fun unmounted() {
    super.unmounted()
    inner.unmounted()
    state.unmounted()
    handle?.dispose()
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> ancestorStateOf(clazz: KClass<T>): T? {
    if (clazz.isInstance(state)) return state as T
    return super.ancestorStateOf(clazz)
  }

  override fun ancestorStateOf(predicate: (state: State<*>) -> Boolean): State<*>? {
    if (predicate(state)) return state
    return super.ancestorStateOf(predicate)
  }

  fun rebuild(force: Boolean) {
    if (isMounted) {
      inner = update(this, inner, build(), force)
    }
  }

  private fun build(): Widget {
    StyleRegistry.register(widget)
    val (widget, handle) = ChangeDetector.evaluate({ state.build(this) }, scheduler::schedule)
    this.handle = handle
    return widget
  }
}

abstract class StatelessWidget(
  key: Key? = null
) : Widget(key), Styled {
  final override val prefix by lazy { ClassRandom.generate(this) }
  override fun style(): Style? = null
  final override fun createElement(parent: RenderElement?) = StatelessElement(parent, this)
  abstract fun build(context: BuildContext): Widget
}

class StatelessElement(
  override val parent: RenderElement?,
  widget: StatelessWidget
) : RenderElement(), Styled {
  override var widget: StatelessWidget = widget
    private set

  override val node get() = inner.node
  override val prefix get() = widget.prefix
  override fun style() = widget.style()

  private val scheduler = RebuildScheduler { rebuild(false) }
  private var inner = build().createElement(this)
  private var handle: ReactiveHandle? = null

  override fun mounted() {
    super.mounted()
    inner.mounted()
  }

  @Suppress("UNCHECKED_CAST")
  override fun update(newWidget: Widget, force: Boolean): Boolean {
    if (newWidget::class != widget::class) return false

    if (force || widget != newWidget) {
      widget = newWidget as StatelessWidget
      rebuild(force)
    }

    return true
  }

  override fun unmounted() {
    super.unmounted()
    inner.unmounted()
  }

  private fun rebuild(force: Boolean) {
    if (isMounted) {
      inner = update(this, inner, build(), force)
    }
  }

  private fun build(): Widget {
    StyleRegistry.register(widget)
    val (widget, handle) = ChangeDetector.evaluate({ widget.build(this) }, scheduler::schedule)
    this.handle = handle
    return widget
  }
}