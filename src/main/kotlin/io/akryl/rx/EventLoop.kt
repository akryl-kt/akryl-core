package io.akryl.rx

object EventLoop {
  private val queue = PriorityQueue<() -> Unit>()

  fun submit(priority: Int, task: () -> Unit) {
    if (queue.isEmpty) {
      requestAnimationFrame()
    }
    queue.push(priority, task)
  }

  private fun requestAnimationFrame() {
    val request = js("window && window.requestAnimationFrame")
    if (request != null) {
      request(this::drain)
    }
  }

  fun drain() {
    queue.drain {
      it()
    }
  }
}
