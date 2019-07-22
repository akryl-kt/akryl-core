package io.akryl.rx

object ChangeDetector {
  private var stack = ArrayList<CallbackObserver>()

  fun <T> evaluate(fn: () -> T, callback: () -> Unit): Pair<T, ReactiveHandle> {
    check(stack.size < 1000) { "ChangeDetector stack is too deep" }
    val observer = CallbackObserver(callback)
    try {
      stack.add(observer)
      return Pair(fn(), observer)
    } finally {
      stack.removeAt(stack.size - 1)
    }
  }

  fun observed(dependency: Observable) {
    stack.lastOrNull()?.observed(dependency)
  }
}

private class CallbackObserver(private val callback: () -> Unit) : Observer, ReactiveHandle {
  private val dependencies = ArrayList<Observable>()

  override fun changed() {
    dispose()
    callback()
  }

  fun observed(dependency: Observable) {
    dependency.subscribe(this)
    dependencies.add(dependency)
  }

  override fun dispose() {
    dependencies.forEach { it.unsubscribe(this) }
    dependencies.clear()
  }
}