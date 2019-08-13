package io.akryl.css

import io.akryl.utils.nextString
import kotlin.random.Random

interface Selector {
  val selector: String
}

data class ClassName(val value: String) : Selector {
  companion object {
    fun random(prefix: String, separator: String = "_", random: Random = Random): ClassName {
      val randPart = random.nextString(6)
      return ClassName("$prefix$separator$randPart")
    }
  }

  override val selector get() = ".$value"
  override fun toString() = value
  operator fun plus(other: ClassName) = ClassList(listOf(this, other))
}

data class ClassList(val classes: List<ClassName>) : Selector {
  override val selector get() = classes.joinToString("") { it.selector }
  override fun toString() = selector
  operator fun plus(other: ClassName) = ClassList(classes + other)
}

data class TagName(val value: String) : Selector {
  override val selector get() = value
  override fun toString() = value

  operator fun get(inner: ClassList) = ComplexSelector("$value${inner.selector}")
  operator fun get(inner: ClassName) = ComplexSelector("$value${inner.selector}")
}

data class ComplexSelector(val value: String) : Selector {
  override val selector get() = value
  override fun toString() = value
}