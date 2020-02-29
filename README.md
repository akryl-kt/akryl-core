# Akryl

Kotlin wrapper around ReactJS.

With akryl, you can write simple and idiomatic Kotlin code that will be converted into React components. 
For example, you can rewrite this JavaScript code:

```jsx
const App = ({name}) => {
    return <div>Hello, {name}!</div>;
}
```

in Kotlin using akryl:

```kotlin
fun App(name: String) = component {
    Div(text = "Hello, $name!")
}
```

# Install

If you don't want to set up the project yourself, you can clone [akryl-frontend-starter](https://github.com/akryl-kt/akryl-frontend-starter) repository:

```bash
git clone https://github.com/akryl-kt/akryl-frontend-starter
cd akryl-frontend-starter
./run.sh
# open http://localhost:8080 when build completes
```

Akryl library consists of several pieces:

- [akryl-core](https://github.com/akryl-kt/akryl-core) - basic integration with React, hooks support.
- [babel-plugin-arkyl](https://github.com/akryl-kt/babel-plugin-akryl) - babel plugin for components support.
- [akryl-dom](https://github.com/akryl-kt/akryl-dom) - functions to work with HTML/CSS.
- [akryl-redux](https://github.com/akryl-kt/akryl-redux) - wrapper around `react-redux` library.

In most cases, you need to install all of them. 

This instruction assumes that you already have a [Kotlin/JS project configured with Gradle](https://kotlinlang.org/docs/reference/js-project-setup.html). 

1. Add jcenter repository:

```gradle
repositories {
    jcenter()
    ...
}
```

2. Add these dependencies into your `build.gradle` file:

```gradle
kotlin {
    sourceSets {
        main {
            dependencies {
                ...
                // kotlin dependencies
                implementation "io.akryl:akryl-core:0.+"
                implementation "io.akryl:akryl-dom:0.+"
                implementation "io.akryl:akryl-redux:0.+"

                // babel-plugin-akryl dependencies
                implementation npm("babel-loader", "8.0.6")
                implementation npm("@babel/core", "7.7.7")
                implementation npm("@babel/preset-env", "7.7.7")
                implementation npm("babel-plugin-akryl", "0.1.1")
                // react dependencies
                implementation npm("react", "16.12.0")
                implementation npm("react-dom", "16.12.0")
                implementation npm("redux", "4.0.5")
                implementation npm("react-redux", "7.1.3")
            }
        }
    }
}
```

3. Add `babel.js` file into `webpack.config.d` directory to load `babel-plugin-akryl`:

```js
config.module.rules.push({
  test: /\.m?js$/,
  exclude: /(node_modules|bower_components|packages_imported)/,
  use: {
    loader: 'babel-loader',
    options: {
      presets: ['@babel/preset-env'],
      plugins: [
        ['babel-plugin-akryl', {production: !config.devServer}],
      ],
    }
  }
});
```

4. Add `src/main/resources/index.html` file:

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Akryl Frontend Starter</title>
    <link rel="stylesheet" href="index.css">
</head>
<body>
    <div id="app"></div>
    <script type="text/javascript" language="JavaScript" src="<jour-project-name>.js"></script>
</body>
</html>
```

5. Add `src/main/kotlin/App.kt` file:

```kotlin
import io.akryl.component
import io.akryl.dom.html.Div
import react_dom.ReactDom
import kotlin.browser.document

fun app() = component {
  Div(text = "Hello, World!")
}

fun main() {
  ReactDom.render(app(), document.getElementById("app"))
}
```

6. Run the project:

```bash
./gradlew run --continuous
```

# Documentation

`akryl-core` provides basic React features:

- Components
- Context
- Hooks

To use JSX-like syntax you need to install [akryl-dom](https://github.com/akryl-kt/akryl-dom) library.

## Components

With Akryl you can use only hooks API and functional components. To create a component you must define a function, that takes props as arguments and returns a virtual DOM element. It is important to wrap the function body into a `component` function call. It tells the `babel-plugin-akryl` to convert a simple Kotlin function into a React component. 

Example:

```kotlin
fun Calc(a: Int, b: Int) = component {
    Div(text = "Sum = ${a + b}")
}
```

The `Calc` is a React component, that accepts `a` and `b` in props and returns div element. Here is the equivalent component in JSX:

```jsx
export const Calc = ({a, b}) => {
    return <div>Sum = {a + b}</div>;
};
```

and in TSX:

```typescript
export interface CalcProps {
    a: number;
    b: number;
}

export const Calc = ({a, b} : CalcProps) => {
    return <div>Sum = {a + b}</div>;
};
```

In Akryl you don't need to wrap props into an interface or in a class - just pass them as function arguments.

You can call a component function from any place in code. There is no restriction to use a component only inside another component.

Example:

```kotlin
// Akryl
val element = Calc(a = 10, b = 20)
```

```jsx
// JSX
const element = <Calc a={10} b={20} />;
```

If your component is pure, you can use `memo` instead of `component` function. It has the same effect as in React: it will prevent a component from re-render if its props are not changed.

## Hooks

Akryl has common hooks from React: 

- [useState](src/main/kotlin/io/akryl/Hooks.kt#L22)
- [useEffect](src/main/kotlin/io/akryl/Hooks.kt#L101)
- [useContext](src/main/kotlin/io/akryl/Hooks.kt#L130)
- [useCallback](src/main/kotlin/io/akryl/Hooks.kt#L147)
- [useRef](src/main/kotlin/io/akryl/Hooks.kt#L168)
- [useDebugValue](src/main/kotlin/io/akryl/Hooks.kt#L183)

All hooks have receiver argument of type `ComponentScope`, that prevents usage outside of a component at compile time.

```kotlin
// will compile
fun counter() = component {
    val (state, setState) = useState(0)
}

// will not compile: ComponentScope receiver is not provided
val (state, setState) = useState(0)
```

To create a custom hook, write a function that has the `ComponentScope` receiver parameter:

```kotlin
fun ComponentScope.useRenderCount(): Int {
    val ref = useRef(0)
    ref.current += 1
    return ref.current
}
```
