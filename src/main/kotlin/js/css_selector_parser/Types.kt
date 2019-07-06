package js.css_selector_parser

interface Token {
  val type: String
  val rule: Token?
  val selectors: Array<Token>?
  val pseudos: Array<PseudoToken>?
}

interface PseudoToken {
  val name: String
  val valueType: String
  val value: dynamic
}

class AttrToken(
  val name: String,
  val operator: String,
  val value: dynamic,
  val valueType: String
)

interface RuleToken : Token {
  var tagName: String?
  var classNames: Array<String>?
  var attrs: Array<AttrToken>?
}