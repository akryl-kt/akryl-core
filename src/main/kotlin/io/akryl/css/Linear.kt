@file:Suppress("unused")

package io.akryl.css

class Linear(val value: String) {
  companion object {
    private const val ZERO = "0"

    val none = Linear("none")
    val auto = Linear("auto")
    val initial = Linear("initial")
    val inherit = Linear("inherit")
  }

  constructor(value: Number, unit: String) : this("$value$unit")

  private val valueCalcSafe: String
    get() = if (value == ZERO) "0px" else value

  operator fun unaryMinus() = Linear(when {
    value.startsWith('-') -> value.substring(1)
    value.startsWith("calc") -> "calc(0px - $value)"
    value == ZERO -> value
    else -> "-$value"
  })

  operator fun plus(other: Linear) = Linear("calc($valueCalcSafe + ${other.valueCalcSafe})")
  operator fun minus(other: Linear) = Linear("calc($valueCalcSafe - ${other.valueCalcSafe})")
  operator fun times(times: Number) = Linear("calc($valueCalcSafe * $times)")
  operator fun div(times: Number) = Linear("calc($valueCalcSafe / $times)")

  override fun toString() = value
  val important get() = Linear("$value !important")
}

val Number.cm: Linear get() = Linear(this, "cm")       // Centimeter
val Number.em: Linear get() = Linear(this, "em")
val Number.ex: Linear get() = Linear(this, "ex")
val Number.fr: Linear get() = Linear(this, "fr")       // Fraction
val Number.mm: Linear get() = Linear(this, "mm")       // Millimeter
val Number.pc: Linear get() = Linear(this, "pc")       // Pica
val Number.pct: Linear get() = Linear(this, "%")
val Number.pt: Linear get() = Linear(this, "pt")       // Point
val Number.px: Linear get() = Linear(this, "px")       // Pixel
val Number.rem: Linear get() = Linear(this, "rem")     // Root em
val Number.vmin: Linear get() = Linear(this, "vmin")   // 1/100th of the smallest side
val Number.vmax: Linear get() = Linear(this, "vmax")   // 1/100th of the largest side
val Number.vh: Linear get() = Linear(this, "vh")       // 1/100th of the viewport height
val Number.vw: Linear get() = Linear(this, "vw")       // 1/100th of the viewport width
val Number.`in`: Linear get() = Linear(this, "in")     // Inch

class DimensionScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun none() = this("none")
  fun auto() = this("auto")
  fun initial() = this("initial")
  fun inherit() = this("inherit")
  fun available() = this("available") // old name
  fun fillAvailable() = this("fill-available") // new name
  fun borderBox() = this("border-box")
  fun contentBox() = this("content-box")
  fun maxContent() = this("max-content")
  fun minContent() = this("min-content")
  fun fitContent() = this("fit-content")

  operator fun invoke(value: Linear) = this(value.toString())
}

class LinearScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun none() = this("none")
  fun auto() = this("auto")
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  operator fun invoke(value: Linear) = this(value.toString())
}