# Akryl

Kotlin wrapper around React.JS.

* **Ideomatic Kotlin**. Write concise and type-safe code for the browser.
* **React Hooks**. Akryl based on new [Hooks API](https://reactjs.org/docs/react-api.html#hooks). It allows writing more compact and composable code in a functional style.
* **Reactivity**. The library contains reactive primitives like computed properties and watchers. Use them to create simple and performant data flow.
* **Simple interop**. Akryl components are fully compatible with React. You can utilize any existing React library. For example, [akryl-router](https://github.com/akryl-kt/akryl-router) is just a thin wrapper around react-router.

# Installation

You can use [hello-world](https://github.com/akryl-kt/akryl-examples/tree/master/hello-world) example as a template or build configuration from scratch.

## Gradle

1. Configure your Kotlin.JS project with [kotlin-frontend-plugin](https://github.com/Kotlin/kotlin-frontend-plugin). 

2. Add jcenter repository:

```gradle
repositories {
    jcenter()
}
```

3. Add dependency:

```gradle
dependencies {
    compile "io.akryl:akryl-core:<version>"
}
```

4. Add npm packages:

```gradle
kotlinFrontend {
    npm {
        dependency("react", "16.8.6")
        dependency("react-dom", "16.8.6")
    }
}
```

# Documentation

## Quickstart

Here is the simplest Akryl application:

```kotlin
class HelloMessage(val name: String) : Component() {
  override fun render() = Div(text = "Hello, $name!")
}

fun main() {
  render(
    HelloMessage(name = "Akryl"),
    document.getElementById("container")!!
  )
}
```

This code will render "Hello, Akryl" into the container on the page. You can find more examples in the [akryl-examples](https://github.com/akryl-kt/akryl-examples) repository.

## Component

Like in React, components are the main building blocks of Akryl applications. `HelloMessage` is an example of the component. You can use it by instantiating its class. Everything passed to the primary constructor can be used inside the render function. Under the hood, Akryl will pass the component instance as `$this` into the props, and then it will execute `render()` on that instance.

### Pure Component

If your component will render the same elements for the same props, it's named a Pure Component in terms of React. You can override the `equals` method to eliminate unnecessary renders. The simplest way is to mark the class with the `data` keyword. Akryl wraps Pure Component into the [memo](https://reactjs.org/docs/react-api.html#reactmemo) higher-order component.

```kotlin
data class HelloMessage(val name: String) : Component() {
  override fun render() = Div(text = "Hello, $name!")
}

fun main() {
  val container = document.getElementById("container")!!
  // will render
  render(HelloMessage(name = "Akryl"), container)
  // will not render - props didn't change
  render(HelloMessage(name = "Akryl"), container)
}
```

## Hooks

Because of the way of instantiating a component, you can not hold state there - you lose it on the next render. Akryl using [React Hooks API](https://reactjs.org/docs/hooks-overview.html) to provide state, life-cycle methods, etc. 

Another reason to use hooks is consistency. The state, effects, and watchers look the same: they are all functions. In contrast, in Class API state is fields, effects are specific methods, and watchers must appear at the constructor.

### Reactivity

All Akryl components are reactive. There is a built-in wrapper around the render function that tracks changes. All processing happens inside the internal event loop that prevents unnecessary updates. If you are using reactive primitives described below, the component will re-render itself automatically. 

### useReactive 

```kotlin
fun <R> useReactive(initialValue: () -> R): ReactiveProperty<R>
```

This hook creates a reactive variable. You can use it inside the `render` and other reactive functions. Writes to that variable will trigger all dependencies to recalculate.

Example:

```kotlin
fun render(): ReactNode {
  val count = useReactive { 0 }
  return Div(
    text = "Count = ${count.value}, click to increment",
    onClick = { count.value += 1 }
  )
}
```

The `ReactiveProperty` implements delegated property interface that allows slightly shorter syntax:

```kotlin
var count by useReactive { 0 }
println(count) // read
count += 1 // write
```

### useComputed

```kotlin
fun <C : Component, R> C.useComputed(fn: C.() -> R): ComputedProperty<R>
```

This hook allows you to memoize the result of `fn` call. It will automatically recompute the result and re-render component if some of the reactive dependencies are changed. The `ComputedProperty` also implements delegated property interface like the `ReactiveProperty`, but it's read-only.

Note that receiving new props will also trigger the recompute. If you want to specify additional dependencies manually, you can use the second version of the hook:

```kotlin
fun <C : Component, R> C.useComputed(vararg dependencies: Any?, fn: C.() -> R): ComputedProperty<R> 
```

Example:

```kotlin
fun render(): ReactNode {
  var count by useReactive { 0 }
  val plusOne by useComputed { count + 1 }
  return Div(
    text = "Count = $count, PlusOne = $plusOne, click to increment",
    onClick = { count += 1 }
  )
}
```

### useWatch

```kotlin
fun <C : Component, R> C.useWatch(selector: C.() -> R, callback: C.(newValue: R, oldValue: R) -> Unit)
fun <C : Component, R> C.useWatch(selector: C.() -> R, callback: C.(newValue: R) -> Unit)
```

This hook will watch to changes in `selector` and call `callback` then changes happen. The first version passes both old and new value to the `callback`, the second version - new value only. Note that the hook stores old value by reference.

Example:

```kotlin
fun render(): ReactNode {
  var count by useReactive { 0 }
  useWatch({ count }) { new, old ->
    // will be called on every click
    println("new count = $new, old count = $old")
  }
  return Div(
    text = "Count = $count, click to increment",
    onClick = { count += 1 }
  )
}
```

### useEffect

```kotlin
class DisposeScope {
  fun dispose(disposer: EffectDisposer)
}
fun useEffect(vararg dependencies: Any?, callback: DisposeScope.() -> Unit)
```

This hook is similar to the React [useEffect](https://reactjs.org/docs/hooks-reference.html#useeffect). There are two differences:

* Dependencies array goes before the callback. 
* Instead of returning a function from the callback, you can call `dispose` to specify how to clean up resources.

These differences are due to better support of Kotlin syntax.

Like in React, the `callback` will be called on every render. One way to prevent that is to pass Unit to the dependencies array.

Example:

```kotlin
fun render(): ReactNode {
  var count by useReactive { 0 }
  useEffect { // will be called on every render
    document.title = count.toString()
  }
  useEffect(Unit) { // will be called only once
    println("only once")
    dispose { println("dispose") }
  }
  return Div(
    text = "Count = $count, click to increment",
    onClick = { count += 1 }
  )
}
```

### useState

```kotlin
fun <R> useState(initialValue: R): StateProperty<R>
```

This hook is similar to the React [useState](https://reactjs.org/docs/hooks-reference.html#usestate). Instead of returning the current value and update function, it will return `StateProperty`. Like others, it implements delegated property interface. 

So this React code:

```js
const [state, setState] = useState(0)
console.log(state)
setState(1)
```

is equals to this Akryl code:

```kotlin
var state by useState(0)
println(state)
state = 1
```

Example:

```kotlin
fun render(): ReactNode {
  var count by useState(0)
  return Div(
    text = "Count = $count, click to increment",
    onClick = { count += 1 }
  )
}
```

This hook is not reactive. It will not work with other reactive hooks. Most of the time, you can replace it with the `useReactive`.

## HTML

Unlike React, you don't need JSX - Kotlin is expressive enough already. In Akryl, HTML tags are functions like `Div()` or `Span()`. Attributes, styles, event listeners, and children are named parameters. 

Example:

```kotlin
return Form(clazz = "login-form", children = listOf(
  Label(clazz = "form-control", children = listOf(
    Text("Login"),
    Input(type = "email", placeholder = "Enter your email")
  )),
  Label(clazz = "form-control", children = listOf(
    Text("Password"),
    Input(type = "password", placeholder = "Enter your password")
  ))
))
```

### Children

The second version of tags' functions accepts only children:

```kotlin
return Div(
  Div(clazz = "first-child"),
  Div(clazz = "second-child")
)
```

Also, there are shortcut parameters `text` and `child`:

```kotlin
Div(text = "data") // Div(children = listOf(Text("data")))
Div(child = Span()) // Div(children = listOf(Span()))
```

### Classes

You can specify classes for a tag by `clazz` and `classes` parameters:

```kotlin
// <div class="foo"/>
Div(clazz = "foo") 
// <div class="foo bar"/>
Div(clazz = "foo bar")
// <div class="foo bar"/>
Div(classes = listOf("foo", "bar"))
// <div class="foo bar baz"/>
Div(classes = listOf("foo", "bar"), clazz = "baz")
```

For conditional classes use `classMap`:

```kotlin
// <div class="foo"/>
Div(classes = classMap(
  "foo" to true,
  "bar" to false
))
```

### Style

You can set inline element styles by passing a map to the  `style` parameter. For now, there is no typing, so every property is of type `String`. Properties names are in camel case.

```kotlin
// <div style="width: 100px; transform: translate(10px); transform-origin: 0 0" />
Div(style = mapOf(
  "width" to "100px",
  "transform" to "translate(10px)",
  "transformOrigin" to "0 0"
))
```

## CSS

### Define classes

Akryl includes DSL for creating style sheets. It allows you to write scoped classes that are not interfering with each other. To define scoped class you need this function:

```kotlin
fun css(scoped: Boolean = true, block: RuleSet): CssPropertyProvider
```

If the `scoped` parameter is true then the class will get a unique name. The `block` parameter is a lambda for the class builder. You must use this function as a factory for a delegated property.

Example:

```kotlin
// .foo_Qi45fz {
//   display: block;
//   height: 100px;
// }
val foo by css {
  display.block()
  height(100.px)
}

// .bar {
//   position: relative;
//   transform: rotate(45deg);
// }
val bar by css(scoped = false) {
  position.relative()
  transform.rotate(45.deg)
}
```

The type of `foo` and `bar` is the `ClassName`. It derives from the `CharSequence` interface so you can use it in place of the `String`:

```kotlin
Div(classes = listOf("container", foo, bar))
```

You can use pseudo-selectors, other classes, and tag selectors inside the `css` block. They will be descendants of the parent selector. If you have a complex selector that can't be described by the DSL, you can pass it as `String`. To use string as a descendant, you must add "&" prefix to it.

```kotlin
val root by css { /* .root */
  hover { /* .root:hover */
    div { /* .root:hover div */ }
    foo { /* .root:hover .foo */ }
  }
  div[foo + bar] { /* .root div.foo.bar */ }
  "&[type=checkbox]" { /* .root[type=checkbox] */ }
  "& + input" { /* .root + input */ }
  ".foo.bar" { /* .foo.bar */ }
}
```

The `CssPropertyProvider` will inject created classes into the document head on the first usage. If there will be no usage then classes will not be injected.

### Global CSS

In case you need a global CSS without root class, you can use function `globalCss`. It is not returning anything but immediately injects CSS into the document head.

```kotlin
globalCss {
  "div.foo" {
    float.right()
  }
  "input[type=checkbox]" {
    display.none()
  }
}
```
