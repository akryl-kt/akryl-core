package io.akryl.rx

class Watcher<R>(
  private val selector: () -> R,
  private val callback: (newValue: R, oldValue: R) -> Unit
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
    callback(newValue, oldValue)
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
  callback: (newValue: R, oldValue: R) -> Unit
) {
  val watcher = Watcher(selector, callback)
  this.registerReactiveHandle(watcher)
}