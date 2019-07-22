package io.akryl.rx

import kotlin.test.Test
import kotlin.test.assertEquals

class PriorityQueueTest {
  @Test
  fun pushForward() {
    val queue = PriorityQueue<Int>()
    queue.push(0, 0)
    queue.push(0, 1)
    queue.push(0, 2)
    queue.push(1, 3)
    queue.push(1, 4)
    queue.push(2, 5)
    queue.push(2, 6)

    val expected = (0..6).toList()
    val actual = queue.toList()
    assertEquals(expected, actual)
  }

  @Test
  fun pushBackward() {
    val queue = PriorityQueue<Int>()
    queue.push(2, 5)
    queue.push(2, 6)
    queue.push(1, 3)
    queue.push(1, 4)
    queue.push(0, 0)
    queue.push(0, 1)
    queue.push(0, 2)

    val expected = (0..6).toList()
    val actual = queue.toList()
    assertEquals(expected, actual)
  }

  @Test
  fun drain() {
    val queue = PriorityQueue<Int>()
    queue.push(0, 1)
    queue.push(0, 2)
    queue.push(0, 3)

    val result = ArrayList<Int>()
    queue.drain {
      if (it <= 3) {
        queue.push(0, it + 3)
      }
      result.add(it)
    }

    assertEquals(listOf(1, 2, 3, 4, 5, 6), result)
  }
}