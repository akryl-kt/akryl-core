package io.akryl.rx

class Watcher<R>(
  private val selector: () -> R,
  private val callback: (oldValue: R, newValue: R) -> Unit
) : ReactiveHandle, Transient {
  companion object {
    const val QUEUE_PRIORITY = 200
  }

  private var oldValue = compute()
  private var handle: ReactiveHandle? = null

  override fun dispose() {
    handle?.dispose()
    handle = null
  }

  private fun watcher() {
    val newValue = compute()
    callback(oldValue, newValue)
    oldValue = newValue
  }

  private fun compute(): R {
    val (newValue, handle) = ChangeDetector.evaluate(selector, this::fire)
    this.handle = handle
    return newValue
  }

  private fun fire() {
    EventLoop.submit(QUEUE_PRIORITY, this::watcher)
  }
}

fun <T : ReactiveContainer, R> T.watch(
  selector: () -> R,
  callback: (oldValue: R, newValue: R) -> Unit
) {
  val watcher = Watcher(selector, callback)
  this.registerReactiveHandle(watcher)
}