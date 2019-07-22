package io.akryl.rx

class PriorityQueue<E> : Iterable<E> {
  private val items = ArrayList<Entry<E>>()
  private var index = 0

  private data class Entry<E>(val priority: Int, val item: E)

  private inner class QueueIterator : Iterator<E> {
    private val inner = items.iterator()
    override fun hasNext() = inner.hasNext()
    override fun next() = inner.next().item
  }

  val isEmpty get() = items.isEmpty()

  fun drain(acceptor: (E) -> Unit) {
    check(index == 0) { "Drain already running" }

    try {
      index = 0
      while (index < items.size) {
        val item = items[index].item
        index += 1
        acceptor(item)
      }
      items.clear()
    } finally {
        index = 0
    }
  }

  fun push(priority: Int, item: E) {
    var i = index
    while (i < items.size && items[i].priority <= priority) {
      i += 1
    }
    items.add(i, Entry(priority, item))
  }

  override fun iterator(): Iterator<E> = QueueIterator()
}
