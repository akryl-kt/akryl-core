package io.akryl.rx

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WatcherTest : EmptyReactiveContainer {
  @Test
  fun property() {
    var prop by reactive(0)
    var counter = 0
    var oldValue = 0
    var newValue = 0

    watch({ prop }) { old, new ->
      counter += 1
      oldValue = old
      newValue = new
    }
    assertEquals(0, counter)

    prop = 1
    assertEquals(1, counter)
    assertEquals(0, oldValue)
    assertEquals(1, newValue)

    prop = 10
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
    assertEquals(1, counter)
    prop = 2
    assertEquals(2, counter)

    container.handle?.dispose()
    prop = 3
    assertEquals(2, counter)
    prop = 4
    assertEquals(2, counter)
  }

  // todo ordering with computed
}