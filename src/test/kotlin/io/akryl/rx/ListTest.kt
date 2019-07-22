package io.akryl.rx

import kotlin.test.Test
import kotlin.test.assertEquals

class ListTest : EmptyReactiveContainer {
  @Test
  fun getSet() {
    val list = observable(arrayListOf<String?>(null, null, null))
    val result by computed { "${list[0]} ${list[1]}" }
    assertEquals("null null", result)

    list[0] = "foo"
    assertEquals("foo null", result)

    list[1] = "bar"
    assertEquals("foo bar", result)
  }

  @Test
  fun addFromEmpty() {
    val list = observable(arrayListOf<Int>())
    val result by computed { list.max() }
    assertEquals(null, result)

    list.add(1)
    assertEquals(1, result)
  }

  @Test
  fun addFromFilled() {
    val list = observable(arrayListOf(1))
    val result by computed { list.max() }
    assertEquals(1, result)

    list.add(2)
    assertEquals(2, result)
  }

  @Test
  fun remove() {
    val list = observable(arrayListOf(1, 2, 3))
    val result by computed { list.max() }
    assertEquals(3, result)

    list.removeAt(2)
    assertEquals(2, result)
  }

  @Test
  fun clearReduce() {
    val list = observable(arrayListOf(1, 2, 3))
    val result by computed { list.max() }
    assertEquals(3, result)

    list.clear()
    assertEquals(null, result)
  }

  @Test
  fun clearGet() {
    val list = observable(arrayListOf(1, 2, 3))
    val result by computed { list.getOrNull(0) }
    assertEquals(1, result)

    list.clear()
    assertEquals(null, result)
  }

  @Test
  fun deepInitial() {
    class TestClass {
      var a = 10
    }

    val list = observable(arrayListOf(TestClass()))
    val result by computed { list[0].a }
    assertEquals(10, result)

    list[0].a = 20
    assertEquals(20, result)
  }

  @Test
  fun deepSet() {
    class TestClass {
      var a = 10
    }

    val list = observable(arrayListOf<TestClass?>(null))
    val result by computed { list[0]?.a }
    assertEquals(null, result)

    list[0] = TestClass()
    assertEquals(10, result)

    list[0]?.a = 20
    assertEquals(20, result)
  }

  @Test
  fun deepAdd() {
    class TestClass {
      var a = 10
    }

    val list = observable(arrayListOf<TestClass>())
    val result by computed { list.getOrNull(0)?.a }
    assertEquals(null, result)

    list.add(TestClass())
    assertEquals(10, result)

    list[0].a = 20
    assertEquals(20, result)
  }

  @Test
  fun recursive() {
    class TestClass {
      var list: MutableList<TestClass>? = null
    }

    val list = arrayListOf(TestClass())
    list[0].list = list
    observable(list)
  }

  @Test
  fun listToString() {
    val first = arrayListOf(1, 2, 3)
    val second = observable(ArrayList(first))
    assertEquals(first.toString(), second.toString())
  }

  @Test
  fun listEquals() {
    val first = arrayListOf(1, 2, 3)
    val second = observable(ArrayList(first))
    assertEquals(second, first)
  }

  @Test
  fun listHashCode() {
    val first = arrayListOf(1, 2, 3)
    val second = observable(ArrayList(first))
    assertEquals(first.hashCode(), second.hashCode())
  }

  // todo ReversedList, SubList
}