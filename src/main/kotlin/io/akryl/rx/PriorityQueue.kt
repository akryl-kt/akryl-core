package io.akryl.rx

class PriorityQueue<E> : Iterable<E> {
  private val items = ArrayList<Entry<E>>()

  private data class Entry<E>(val priority: Int, val item: E)

  private inner class QueueIterator() : Iterator<E> {
    private val inner = items.iterator()
    override fun hasNext() = inner.hasNext()
    override fun next() = inner.next().item
  }

  val isEmpty get() = items.isEmpty()

  fun drain(acceptor: (E) -> Unit) {
    var i = 0
    while (i < items.size) {
      acceptor(items[i].item)
      i += 1
    }
    items.clear()
  }

  fun push(priority: Int, item: E) {
    var i = 0
    while (i < items.size && items[i].priority <= priority) {
      i += 1
    }
    items.add(i, Entry(priority, item))
  }

  override fun iterator(): Iterator<E> = QueueIterator()
}
