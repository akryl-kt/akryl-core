package io.akryl.rx

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SetTest : EmptyReactiveContainer {
  @Test
  fun containsExists() {
    val set = observable(hashSetOf(1, 2, 3))
    val result by computed { 1 in set }
    assertTrue(result)

    set.remove(1)
    assertFalse(result)
  }

  @Test
  fun containsAbsent() {
    val set = observable(hashSetOf(2, 3))
    val result by computed { 1 in set }
    assertFalse(result)

    set.add(1)
    assertTrue(result)
  }

  @Test
  fun clear() {
    val set = observable(hashSetOf(1, 2, 3))
    val str by computed { set.toString() }
    val contains by computed { 1 in set }
    assertEquals("[1, 2, 3]", str)
    assertTrue(contains)

    set.clear()
    assertEquals("[]", str)
    assertFalse(contains)
  }

  @Test
  fun setToString() {
    val first = hashSetOf(1, 2, 3)
    val second = observable(HashSet(first))
    assertEquals(first.toString(), second.toString())
  }

  @Test
  fun setEquals() {
    val first = hashSetOf(1, 2, 3)
    val second = observable(HashSet(first))
    assertEquals(second, first)
  }

  @Test
  fun setHashCode() {
    val first = hashSetOf(1, 2, 3)
    val second = observable(HashSet(first))
    assertEquals(first.hashCode(), second.hashCode())
  }

  @Test
  fun iterator() {
    val set = observable(hashSetOf(1, 2, 3))

    val result by computed {
      val s = ArrayList<Int>()
      val iterator = set.iterator()
      while (iterator.hasNext()) {
        s.add(iterator.next())
      }
      s.joinToString()
    }
    assertEquals("1, 2, 3", result)

    set.add(4)
    assertEquals("1, 2, 3, 4", result)

    set.add(4)
    assertEquals("1, 2, 3, 4", result)

    set.remove(1)
    assertEquals("2, 3, 4", result)

    set.clear()
    assertEquals("", result)

    set.addAll(setOf(10, 20, 30))
    assertEquals("10, 20, 30", result)

    val iterator2 = set.iterator()
    while (iterator2.hasNext()) {
      val entry = iterator2.next()
      if (entry == 10) {
        iterator2.remove()
      }
    }
    assertEquals("20, 30", result)
  }

  @Test
  fun toList() {
    val set = observable(hashSetOf(1, 2, 3))
    val result by computed { ArrayList(set) }
    assertEquals(listOf(1, 2, 3), result)

    set.remove(2)
    assertEquals(listOf(1, 3), result)

    set.add(4)
    assertEquals(listOf(1, 3, 4), result)

    set.clear()
    assertEquals(emptyList<Int>(), result)
  }

  @Test
  fun recursive() {
    class TestClass {
      var set: MutableSet<TestClass>? = null
    }

    val set = hashSetOf(TestClass())
    set.first().set = set
    observable(set)
  }
}