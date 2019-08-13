@file:Suppress("EnumEntryName", "unused")

package io.akryl.css

class BackgroundScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun none() = this("none")
  fun initial() = this("initial")
  fun inherit() = this("inherit")
}

class ImageScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun none() = this("none")
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun url(value: String) = this("url('$value')")
}

class BackgroundSizeScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun auto() = this("auto")
  fun cover() = this("cover")
  fun contain() = this("contain")
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  operator fun invoke(value: Linear) = this("$value")
  operator fun invoke(width: Linear, height: Linear) = this("$width $height")
}

class BackgroundRepeatScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun repeat() = this("repeat")
  fun repeatX() = this("repeat-x")
  fun repeatY() = this("repeat-y")
  fun noRepeat() = this("no-repeat")
  fun initial() = this("initial")
  fun inherit() = this("inherit")
}

enum class Horizontal {
  left,
  center,
  right,
}

enum class Vertical {
  top,
  center,
  bottom
}

class BackgroundPositionScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  operator fun invoke(vertical: Vertical, horizontal: Horizontal) = this("$vertical $horizontal")
  operator fun invoke(horizontal: Linear, vertical: Linear) = this("$horizontal $vertical")
}