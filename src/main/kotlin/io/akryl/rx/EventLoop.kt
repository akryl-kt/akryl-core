package io.akryl.rx

import org.w3c.dom.Window
import kotlin.browser.window
import kotlin.js.Promise

object EventLoop {
  private val queue = PriorityQueue<() -> Unit>()
  private val requestTick = getRequestTick()

  fun submit(priority: Int, task: () -> Unit) {
    if (queue.isEmpty) {
      requestTick()
    }
    queue.push(priority, task)
  }

  private fun getRequestTick(): () -> Unit {
    if (jsTypeOf(Promise) != "undefined") {
      return { Promise.resolve(Unit).then { drain() } }
    }

    val global: dynamic = if (jsTypeOf(window) != "undefined") window else global

    val setImmediate = global.setImmediate
    if (setImmediate != null) {
      return { setImmediate { drain() }; Unit }
    }

    val setTimeout = global.setTimeout
    return { setTimeout({ drain() }, 0); Unit }
  }

  fun drain() {
    queue.drain {
      it()
    }
  }
}

private external val global: Window