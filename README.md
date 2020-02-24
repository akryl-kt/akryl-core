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

If you don't want to set up the project yourself, you can clone [akryl-frontend-starter](https://github.com/akryl-kt/akryl-frontend-starter) repository.

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

