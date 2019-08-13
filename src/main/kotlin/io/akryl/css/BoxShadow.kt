@file:Suppress("EnumEntryName", "unused")

package io.akryl.css

class BoxShadowScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun none() = this("none")

  operator fun invoke(
    inset: Boolean,
    offsetX: Linear,
    offsetY: Linear,
    blurRadius: Linear,
    spreadRadius: Linear,
    color: Color
  ): BoxShadowScope {
    val value = "${if (inset) "inset " else ""}$offsetX $offsetY $blurRadius $spreadRadius $color"
    properties[name] = properties[name]?.let { "$it, $value" } ?: value
    return this
  }

  operator fun invoke(
    offsetX: Linear,
    offsetY: Linear,
    blurRadius: Linear,
    spreadRadius: Linear,
    color: Color
  ): BoxShadowScope {
    val value = "$offsetX $offsetY $blurRadius $spreadRadius $color"
    properties[name] = properties[name]?.let { "$it, $value" } ?: value
    return this
  }
}