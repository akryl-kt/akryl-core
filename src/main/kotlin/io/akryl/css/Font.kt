@file:Suppress("unused")

package io.akryl.css

class FontScope(properties: CssProps, name: String) : StringScope(properties, name) {
  operator fun invoke(size: Linear, vararg family: String): ModifiersScope {
    val familyStr = family.joinToString { "'$it'" }
    return this("$size $familyStr")
  }
}

class FontFamilyScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun serif() = this("serif")
  fun sansSerif() = this("sans-serif")
  fun monospace() = this("monospace")
}