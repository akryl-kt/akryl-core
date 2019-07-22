package io.akryl.rx

import kotlin.test.*

class ComputedPropertyTest : EmptyReactiveContainer {
  @Test
  fun lazy() {
    var called = false
    val prop by computed { called = true; "result" }

    assertFalse(called)

    assertEquals("result", prop)
    assertTrue(called)
  }

  @Test
  fun reacting() {
    val aProp = ObservableProperty()
    var a = 10

    val b by computed { aProp.observed(); a }
    assertEquals(10, b)

    a = 20
    assertEquals(10, b)

    aProp.fire()
    assertEquals(20, b)

    a = 30
    aProp.fire()
    assertEquals(30, b)
  }

  @Test
  fun registering() {
    class Container : ReactiveContainer, Transient {
      var prop: ReactiveHandle? = null

      override fun registerReactiveHandle(handle: ReactiveHandle) {
        prop = handle
      }
    }

    val container = Container()
    val prop by container.computed { 1 }

    assertEquals(1, prop)
    assertNotNull(container.prop)
  }

  @Test
  fun disposable() {
    val aProp = ObservableProperty()
    var a = 10

    val bProp = ComputedProperty(this) { aProp.observed(); a }
    val b by bProp
    assertEquals(10, b)

    a = 20
    aProp.fire()
    assertEquals(20, b)

    bProp.dispose()
    a = 30
    aProp.fire()
    assertEquals(20, b)
  }

  @Test
  fun observable() {
    val aProp = ObservableProperty()
    var a = 10

    val b by computed { aProp.observed(); a }
    val c by computed { b + 10 }
    assertEquals(20, c)

    a = 20
    aProp.fire()
    assertEquals(30, c)
  }

  @Test
  fun initialization() {
    class Container : ReactiveContainer {
      override fun registerReactiveHandle(handle: ReactiveHandle) {}
    }

    val container = Container()
    var counter = 0
    val prop by container.computed { counter += 1; counter }

    assertEquals(1, prop)
    assertEquals(2, prop)
    assertEquals(3, prop)

    observable(container)
    assertEquals(4, prop)
    assertEquals(4, prop)
    assertEquals(4, prop)
  }

  // todo sequential
  // todo fire only if changed
  // todo event loop test
}