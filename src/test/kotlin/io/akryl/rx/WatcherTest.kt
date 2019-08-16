package io.akryl.rx

import kotlin.test.*

class WatcherTest : EmptyReactiveContainer {
  @Test
  fun property() {
    var prop by reactive(0)
    var counter = 0
    var oldValue = 0
    var newValue = 0

    watch({ prop }) { new, old ->
      counter += 1
      oldValue = old
      newValue = new
    }
    EventLoop.drain()
    assertEquals(0, counter)

    prop = 1
    EventLoop.drain()
    assertEquals(1, counter)
    assertEquals(0, oldValue)
    assertEquals(1, newValue)

    prop = 10
    EventLoop.drain()
    assertEquals(2, counter)
    assertEquals(1, oldValue)
    assertEquals(10, newValue)
  }

  @Test
  fun registering() {
    class Container : ReactiveContainer, Transient {
      var handle: ReactiveHandle? = null

      override fun registerReactiveHandle(handle: ReactiveHandle) {
        this.handle = handle
      }
    }

    val container = Container()
    container.watch({ true }, { _, _ -> })
    assertNotNull(container.handle)
  }

  @Test
  fun disposable() {
    class Container : ReactiveContainer, Transient {
      var handle: ReactiveHandle? = null

      override fun registerReactiveHandle(handle: ReactiveHandle) {
        this.handle = handle
      }
    }

    val container = Container()
    var prop by reactive(0)
    var counter = 0
    container.watch({ prop }, { _, _ -> counter += 1 })

    prop = 1
    EventLoop.drain()
    assertEquals(1, counter)

    prop = 2
    EventLoop.drain()
    assertEquals(2, counter)

    container.handle?.dispose()

    prop = 3
    EventLoop.drain()
    assertEquals(2, counter)

    prop = 4
    EventLoop.drain()
    assertEquals(2, counter)
  }

  @Test
  fun requireDrain() {
    var prop by reactive(0)
    var called = false

    watch({ prop }) { _, _ ->
      called = true
    }

    assertFalse(called)

    prop = 1
    assertFalse(called)

    EventLoop.drain()
    assertTrue(called)
  }

  @Test
  fun ordering() {
    val order = ArrayList<String>()

    var a by reactive(0)

    val b by computed {
      order.add("computed")
      a + 1
    }

    watch({ a + b }) { _, _ ->
      order.add("watch")
    }

    val c by computed {
      order.add("computed")
      a + 2
    }

    assertEquals(1, b)
    assertEquals(2, c)
    order.clear()

    a = 1
    EventLoop.drain()
    assertEquals(listOf("computed", "computed", "watch"), order)
  }
}