package io.akryl.rx

class Watcher<R>(
  private val selector: () -> R,
  private val callback: (oldValue: R, newValue: R) -> Unit
) : ReactiveHandle, Transient {
  private var first = true
  private var oldValue = watcher()
  private var handle: ReactiveHandle? = null

  override fun dispose() {
    handle?.dispose()
    handle = null
  }

  private fun watcher(): R {
    val (newValue, handle) = ChangeDetector.evaluate(selector) { watcher() }
    this.handle = handle

    if (!first) {
      callback(oldValue, newValue)
    }
    first = false
    oldValue = newValue

    return newValue
  }
}

fun <T : ReactiveContainer, R> T.watch(
  selector: () -> R,
  callback: (oldValue: R, newValue: R) -> Unit
) {
  val watcher = Watcher(selector, callback)
  this.registerReactiveHandle(watcher)
}