@file:Suppress("unused", "SpellCheckingInspection", "EnumEntryName")

package io.akryl.css

typealias CssProps = MutableMap<String, Any?>

class ModifiersScope(
  private val properties: CssProps,
  private val name: String,
  private val value: String
) {
  fun important() {
    properties[name] = "$value !important"
  }
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

open class StringScope(protected val properties: CssProps, val name: String) {
  open operator fun invoke(value: String): ModifiersScope {
    properties[name] = value
    return ModifiersScope(properties, name, value)
  }
}

class TextAlignScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")
  fun unset() = this("unset")

  fun left() = this("left")
  fun right() = this("right")
  fun center() = this("center")
  fun justify() = this("justify")
  fun justifyAll() = this("justify-all")
  fun start() = this("start")
  fun end() = this("end")
  fun matchParent() = this("match-parent")
}

class BoxSizingScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")
  fun unset() = this("unset")

  fun contentBox() = this("content-box")
  fun borderBox() = this("border-box")
}

class BorderStyleScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun none() = this("none")
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun hidden() = this("hidden")
  fun dotted() = this("dotted")
  fun dashed() = this("dashed")
  fun solid() = this("solid")
  fun double() = this("double")
  fun groove() = this("groove")
  fun ridge() = this("ridge")
  fun inset() = this("inset")
  fun outset() = this("outset")
}

class FontStyleScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun normal() = this("normal")
  fun italic() = this("italic")
  fun oblique() = this("oblique")
}

class QuotedStringScope(properties: CssProps, name: String) : StringScope(properties, name) {
  override operator fun invoke(value: String) = super.invoke("'$value'")
}

class ListStyleTypeScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun none() = this("none")
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun disc() = this("disc")
  fun armenian() = this("armenian")
  fun circle() = this("circle")
  fun cjkIdeographic() = this("cjk-ideographic")
  fun decimal() = this("decimal")
  fun decimalLeadingZero() = this("decimal-leading-zero")
  fun georgian() = this("georgian")
  fun hebrew() = this("hebrew")
  fun hiragana() = this("hiragana")
  fun hiraganaIroha() = this("hiragana-iroha")
  fun katakana() = this("katakana")
  fun katakanaIroha() = this("katakana-iroha")
  fun lowerAlpha() = this("lower-alpha")
  fun lowerGreek() = this("lower-greek")
  fun lowerLatin() = this("lower-latin")
  fun lowerRoman() = this("lower-roman")
  fun square() = this("square")
  fun upperAlpha() = this("upper-alpha")
  fun upperGreek() = this("upper-greek")
  fun upperLatin() = this("upper-latin")
  fun upperRoman() = this("upper-roman")
}

class DisplayScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun none() = this("none")
  fun initial() = this("initial")
  fun inherit() = this("inherit")
  fun contents() = this("contents")

  fun block() = this("block")
  fun `inline`() = this("inline")
  fun runIn() = this("run-in")

  fun flow() = this("flow")
  fun flowRoot() = this("flow-root")
  fun table() = this("table")
  fun flex() = this("flex")
  fun grid() = this("grid")
  fun subgrid() = this("subgrid")

  fun listItem() = this("list-item")

  fun tableRowGroup() = this("table-row-group")
  fun tableHeaderGroup() = this("table-header-group")
  fun tableFooterGroup() = this("table-footer-group")
  fun tableRow() = this("table-row")
  fun tableCell() = this("table-cell")
  fun tableColumnGroup() = this("table-column-group")
  fun tableColumn() = this("table-column")
  fun tableCaption() = this("table-caption")

  fun inlineBlock() = this("inline-block")
  fun inlineListItem() = this("inline-list-item")
  fun inlineTable() = this("inline-table")
  fun inlineFlex() = this("inline-flex")
  fun inlineGrid() = this("inline-grid")
}

class WordBreakScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun normal() = this("normal")
  fun breakAll() = this("break-all")
  fun keepAll() = this("keep-all")
  fun breakWord() = this("break-word")
}

class AlignContentScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun stretch() = this("stretch")
  fun center() = this("center")
  fun flexStart() = this("flex-start")
  fun flexEnd() = this("flex-end")
  fun spaceBetween() = this("space-between")
  fun spaceAround() = this("space-around")
}

class AlignItemsScope(properties: CssProps, name: String) : StringScope(properties, name) {
    fun initial() = this("initial")
    fun inherit() = this("inherit")

    fun stretch() = this("stretch")
    fun center() = this("center")
    fun flexStart() = this("flex-start")
    fun flexEnd() = this("flex-end")
    fun baseline() = this("baseline")
}

class AlignSelfScope(properties: CssProps, name: String) : StringScope(properties, name) {
    fun initial() = this("initial")
    fun inherit() = this("inherit")

    fun auto() = this("auto")
    fun stretch() = this("stretch")
    fun center() = this("center")
    fun flexStart() = this("flex-start")
    fun flexEnd() = this("flex-end")
    fun baseline() = this("baseline")
}

class ExtendScope(properties: CssProps, name: String) : StringScope(properties, name) {
    fun initial() = this("initial")
    fun inherit() = this("inherit")

    fun borderBox() = this("border-box")
    fun paddingBox() = this("padding-box")
    fun contentBox() = this("content-box")
}

class BackgroundAttachmentScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun scroll() = this("scroll")
  fun fixed() = this("fixed")
  fun local() = this("local")
}

class BlendModeScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun normal() = this("normal")
  fun multiply() = this("multiply")
  fun screen() = this("screen")
  fun overlay() = this("overlay")
  fun darken() = this("darken")
  fun lighten() = this("lighten")
  fun colorDodge() = this("color-dodge")
  fun saturation() = this("saturation")
  fun color() = this("color")
  fun luminosity() = this("luminosity")
}

class ColorScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")
  fun unset() = this("unset")

  fun transparent() = this("transparent")
  fun currentColor() = this("current-color")

  fun aliceBlue() = this("aliceblue")
  fun antiqueWhite() = this("antiquewhite")
  fun aqua() = this("aqua")
  fun aquamarine() = this("aquamarine")
  fun azure() = this("azure")
  fun beige() = this("beige")
  fun bisque() = this("bisque")
  fun black() = this("black")
  fun blanchedAlmond() = this("blanchedalmond")
  fun blue() = this("blue")
  fun blueViolet() = this("blueviolet")
  fun brown() = this("brown")
  fun burlyWood() = this("burlywood")
  fun cadetBlue() = this("cadetblue")
  fun chartreuse() = this("chartreuse")
  fun chocolate() = this("chocolate")
  fun coral() = this("coral")
  fun cornflowerBlue() = this("cornflowerblue")
  fun cornsilk() = this("cornsilk")
  fun crimson() = this("crimson")
  fun cyan() = this("cyan")
  fun darkBlue() = this("darkblue")
  fun darkCyan() = this("darkcyan")
  fun darkGoldenrod() = this("darkgoldenrod")
  fun darkGray() = this("darkgray")
  fun darkGreen() = this("darkgreen")
  fun darkGrey() = this("darkgrey")
  fun darkKhaki() = this("darkkhaki")
  fun darkMagenta() = this("darkmagenta")
  fun darkOliveGreen() = this("darkolivegreen")
  fun darkOrange() = this("darkorange")
  fun darkOrchid() = this("darkorchid")
  fun darkRed() = this("darkred")
  fun darkSalmon() = this("darksalmon")
  fun darkSeaGreen() = this("darkseagreen")
  fun darkSlateBlue() = this("darkslateblue")
  fun darkSlateGray() = this("darkslategray")
  fun darkSlateGrey() = this("darkslategrey")
  fun darkTurquoise() = this("darkturquoise")
  fun darkViolet() = this("darkviolet")
  fun deepPink() = this("deeppink")
  fun deepSkyBlue() = this("deepskyblue")
  fun dimGray() = this("dimgray")
  fun dimGrey() = this("dimgrey")
  fun dodgerBlue() = this("dodgerblue")
  fun firebrick() = this("firebrick")
  fun floralWhite() = this("floralwhite")
  fun forestGreen() = this("forestgreen")
  fun fuchsia() = this("fuchsia")
  fun gainsboro() = this("gainsboro")
  fun ghostWhite() = this("ghostwhite")
  fun gold() = this("gold")
  fun goldenrod() = this("goldenrod")
  fun gray() = this("gray")
  fun green() = this("green")
  fun greenYellow() = this("greenyellow")
  fun grey() = this("grey")
  fun honeydew() = this("honeydew")
  fun hotPink() = this("hotpink")
  fun indianRed() = this("indianred")
  fun indigo() = this("indigo")
  fun ivory() = this("ivory")
  fun khaki() = this("khaki")
  fun lavender() = this("lavender")
  fun lavenderBlush() = this("lavenderblush")
  fun lawnGreen() = this("lawngreen")
  fun lemonChiffon() = this("lemonchiffon")
  fun lightBlue() = this("lightblue")
  fun lightCoral() = this("lightcoral")
  fun lightCyan() = this("lightcyan")
  fun lightGoldenrodYellow() = this("lightgoldenrodyellow")
  fun lightGray() = this("lightgray")
  fun lightGreen() = this("lightgreen")
  fun lightGrey() = this("lightgrey")
  fun lightPink() = this("lightpink")
  fun lightSalmon() = this("lightsalmon")
  fun lightSeaGreen() = this("lightseagreen")
  fun lightSkyBlue() = this("lightskyblue")
  fun lightSlateGray() = this("lightslategray")
  fun lightSlateGrey() = this("lightslategrey")
  fun lightSteelBlue() = this("lightsteelblue")
  fun lightYellow() = this("lightyellow")
  fun lime() = this("lime")
  fun limeGreen() = this("limegreen")
  fun linen() = this("linen")
  fun magenta() = this("magenta")
  fun maroon() = this("maroon")
  fun mediumAquamarine() = this("mediumaquamarine")
  fun mediumBlue() = this("mediumblue")
  fun mediumOrchid() = this("mediumorchid")
  fun mediumPurple() = this("mediumpurple")
  fun mediumSeaGreen() = this("mediumseagreen")
  fun mediumSlateBlue() = this("mediumslateblue")
  fun mediumSpringGreen() = this("mediumspringgreen")
  fun mediumTurquoise() = this("mediumturquoise")
  fun mediumVioletRed() = this("mediumvioletred")
  fun midnightBlue() = this("midnightblue")
  fun mintCream() = this("mintcream")
  fun mistyRose() = this("mistyrose")
  fun moccasin() = this("moccasin")
  fun navajoWhite() = this("navajowhite")
  fun navy() = this("navy")
  fun oldLace() = this("oldlace")
  fun olive() = this("olive")
  fun oliveDrab() = this("olivedrab")
  fun orange() = this("orange")
  fun orangeRed() = this("orangered")
  fun orchid() = this("orchid")
  fun paleGoldenrod() = this("palegoldenrod")
  fun paleGreen() = this("palegreen")
  fun paleTurquoise() = this("paleturquoise")
  fun paleVioletRed() = this("palevioletred")
  fun papayaWhip() = this("papayawhip")
  fun peachPuff() = this("peachpuff")
  fun peru() = this("peru")
  fun pink() = this("pink")
  fun plum() = this("plum")
  fun powderBlue() = this("powderblue")
  fun purple() = this("purple")
  fun red() = this("red")
  fun rosyBrown() = this("rosybrown")
  fun royalBlue() = this("royalblue")
  fun saddleBrown() = this("saddlebrown")
  fun salmon() = this("salmon")
  fun sandyBrown() = this("sandybrown")
  fun seaGreen() = this("seagreen")
  fun seaShell() = this("seashell")
  fun sienna() = this("sienna")
  fun silver() = this("silver")
  fun skyBlue() = this("skyblue")
  fun slateBlue() = this("slateblue")
  fun slateGray() = this("slategray")
  fun slateGrey() = this("slategrey")
  fun snow() = this("snow")
  fun springGreen() = this("springgreen")
  fun steelBlue() = this("steelblue")
  fun tan() = this("tan")
  fun teal() = this("teal")
  fun thistle() = this("thistle")
  fun tomato() = this("tomato")
  fun turquoise() = this("turquoise")
  fun violet() = this("violet")
  fun wheat() = this("wheat")
  fun white() = this("white")
  fun whiteSmoke() = this("whitesmoke")
  fun yellow() = this("yellow")
  fun yellowGreen() = this("yellowgreen")

  operator fun invoke(value: Int) = this('#' + value.toString(16).padStart(6, '0'))
  operator fun invoke(value: Long) = this('#' + value.toString(16).padStart(6, '0'))
  operator fun invoke(value: Color) = this(value.toString())

  fun rgb(red: Int, green: Int, blue: Int) = this("rgb($red, $green, $blue)")
  fun rgba(red: Int, green: Int, blue: Int, alpha: Double) = this("rgba($red, $green, $blue, $alpha)")
  fun hsl(hue: Int, saturation: Int, lightness: Int) = this("hsl($hue, $saturation%, $lightness%)")
  fun hsla(hue: Int, saturation: Int, lightness: Int, alpha: Double) = this("hsla($hue, $saturation%, $lightness%, $alpha)")
}

class PositionScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun static() = this("static")
  fun absolute() = this("absolute")
  fun fixed() = this("fixed")
  fun relative() = this("relative")
  fun sticky() = this("sticky")
  fun initial() = this("initial")
  fun inherit() = this("inherit")
}

class FontWeightScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")
  fun unset() = this("unset")

  fun normal() = this("normal")
  fun bold() = this("bold")
  fun bolder() = this("bolder")
  fun lighter() = this("lighter")
  fun w900() = this("900")
  fun w800() = this("800")
  fun w700() = this("700")
  fun w600() = this("600")
  fun w500() = this("500")
  fun w400() = this("400")
  fun w300() = this("300")
  fun w200() = this("200")
  fun w100() = this("100")
}

class FlexDirectionScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun row() = this("row")
  fun rowReverse() = this("row-reverse")
  fun column() = this("column")
  fun columnReverse() = this("column-reverse")
}

class FlexWrapScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun nowrap() = this("nowrap")
  fun wrap() = this("wrap")
  fun wrapReverse() = this("wrap-reverse")
}

class TextDecorationStyleScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")
  fun none() = this("none")

  fun solid() = this("solid")
  fun double() = this("double")
  fun dotted() = this("dotted")
  fun dashed() = this("dashed")
  fun wavy() = this("wavy")
}

class TextDecorationLineScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")
  fun none() = this("none")

  fun underline() = this("underline")
  fun overline() = this("overline")
  fun lineThrough() = this("line-through")
}

class CursorScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")
  fun none() = this("none")

  fun alias() = this("alias")
  fun allScroll() = this("all-scroll")
  fun auto() = this("auto")
  fun cell() = this("cell")
  fun contextMenu() = this("context-menu")
  fun colResize() = this("col-resize")
  fun copy() = this("copy")
  fun crosshair() = this("crosshair")
  fun default() = this("default")
  fun eResize() = this("e-resize")
  fun ewResize() = this("ew-resize")
  fun grab() = this("grab")
  fun grabbing() = this("grabbing")
  fun help() = this("help")
  fun move() = this("move")
  fun nResize() = this("n-resize")
  fun neResize() = this("ne-resize")
  fun neswResize() = this("nesw-resize")
  fun nsResize() = this("ns-resize")
  fun nwResize() = this("nw-resize")
  fun nwseResize() = this("nwse-resize")
  fun noDrop() = this("no-drop")
  fun notAllowed() = this("not-allowed")
  fun pointer() = this("pointer")
  fun progress() = this("progress")
  fun rowResize() = this("row-resize")
  fun sResize() = this("s-resize")
  fun seResize() = this("se-resize")
  fun swResize() = this("sw-resize")
  fun text() = this("text")
  fun verticalText() = this("vertical-text")
  fun wResize() = this("w-resize")
  fun wait() = this("wait")
  fun zoomIn() = this("zoom-in")
  fun zoomOut() = this("zoom-out")
}

class OverflowScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun visible() = this("visible")
  fun hidden() = this("hidden")
  fun scroll() = this("scroll")
  fun auto() = this("auto")
}

class FloatScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")
  fun none() = this("none")

  fun left() = this("left")
  fun right() = this("right")
}

class VerticalAlignScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun baseline() = this("baseline")
  fun sub() = this("sub")
  fun `super`() = this("super")
  fun top() = this("top")
  fun textTop() = this("text-top")
  fun middle() = this("middle")
  fun bottom() = this("bottom")
  fun textBottom() = this("text-bottom")

  operator fun invoke(value: Linear) = this(value.toString())
}

class TextOverflowScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun clip() = this("clip")
  fun ellipsis() = this("ellipsis")
}

class WhiteSpaceScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  fun normal() = this("normal")
  fun nowrap() = this("nowrap")
  fun pre() = this("pre")
  fun preLine() = this("pre-line")
  fun preWrap() = this("pre-wrap")
}

class ObjectFitScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")
  fun none() = this("none")

  fun fill() = this("fill")
  fun contain() = this("contain")
  fun cover() = this("cover")
  fun scaleDown() = this("scale-down")
}

class PositionAlignScope(properties: CssProps, name: String) : StringScope(properties, name) {
  fun initial() = this("initial")
  fun inherit() = this("inherit")

  operator fun invoke(horizontal: Horizontal, vertical: Vertical) = this("$horizontal $vertical")
  operator fun invoke(horizontal: Linear, vertical: Linear) = this("$horizontal $vertical")
}