@file:Suppress("unused")

package io.akryl.css

class FontFamilyScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun serif() = this("serif")
  fun sansSerif() = this("sans-serif")
  fun monospace() = this("monospace")
}