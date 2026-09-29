# Scientific Calculator (Android, Kotlin + Jetpack Compose)

A fully offline scientific calculator built for a university project. No
Firebase, no APIs, no database — everything runs on-device.

## Opening the project

1. Open Android Studio.
2. **File > Open** and select the `ScientificCalculator` folder (the one
   containing `settings.gradle.kts`).
3. Let Android Studio sync Gradle (it will download the Gradle 8.7
   distribution and dependencies the first time — this needs an internet
   connection once).
4. Press **Run ▶** on an emulator or device (minimum Android 8.0 / API 26).

> Note: `gradle/wrapper/gradle-wrapper.jar` (a small binary file) is not
> included in this export. Android Studio does **not** need it to sync,
> build, or run the app — it reads `gradle-wrapper.properties` directly. It
> is only needed if you want to run `./gradlew` from a terminal; if so, run
> `gradle wrapper` once with any locally installed Gradle to regenerate it,
> or use Android Studio's own "Gradle" tool window to build instead.

## Project structure

```
app/src/main/java/com/university/scientificcalculator/
├── MainActivity.kt              // Single activity, hosts Compose UI
├── calculator/
│   └── CalculatorEngine.kt      // Tokenizer + recursive-descent parser/evaluator
├── model/
│   ├── AngleMode.kt             // DEG / RAD
│   └── HistoryItem.kt           // One entry in the calculation history
├── viewmodel/
│   ├── CalculatorAction.kt      // Every possible keypad action
│   ├── CalculatorUiState.kt     // Immutable screen state
│   └── CalculatorViewModel.kt   // Expression editing + calls into the engine
└── ui/
    ├── CalculatorScreen.kt      // Top bar + display + keypad + history sheet
    ├── theme/                   // Material 3 colors, typography, light/dark
    └── components/              // DisplaySection, Keypad, buttons, ModeSelector, HistorySheet

app/src/test/java/.../calculator/CalculatorEngineTest.kt   // Unit tests
```

The calculation engine (`calculator/`) has **zero Android dependencies** —
it is a plain Kotlin object, so it is fully unit-testable and easy to explain
to an instructor independently of the UI.

## Running the unit tests

In Android Studio: right-click `CalculatorEngineTest.kt` → **Run**. Or from
a terminal once the Gradle wrapper jar is present: `./gradlew test`.

Tests cover: basic arithmetic, operator precedence, parentheses, powers,
roots, all trig functions in both DEG and RAD, logarithms, constants with
implicit multiplication (`2π`), factorial, and error cases (division by
zero, invalid expressions, mismatched parentheses, invalid sqrt/log/factorial
input).

## How the calculation engine works

`CalculatorEngine.evaluate(expression, angleMode)` runs the expression
through:

1. **Lexer** — turns the raw string into tokens (numbers, operators,
   functions, constants, parentheses).
2. **Parser** — a recursive-descent parser with one function per precedence
   level (`expression → term → unary → power → postfix → primary`), which
   evaluates as it parses.

Nothing is ever executed as arbitrary code — the whole engine is table/rule
based, so it can't crash or be exploited by unexpected input. Any problem
(bad syntax, division by zero, `sqrt` of a negative number, etc.) throws an
internal exception that `evaluate()` catches and turns into a friendly
`EngineResult.Error("Math Error")` or `EngineResult.Error("Invalid Expression")`
— the UI never sees a crash.

## Notable UX details

- The result line updates **live** as you type, without waiting for `=`.
- `=` commits the calculation to history and formats the result (plain
  integers, decimals, or scientific notation for very large/small values).
- `x²` and `1/x` operate on the whole current expression (e.g. typing
  `5+3` then pressing `x²` gives `(5+3)^2`); other scientific buttons insert
  a function prefix (`sin(`, `log(`, …) for you to fill in.
- Light and dark Material 3 color schemes are both defined and switch
  automatically with the system setting.
- All buttons have content descriptions and large touch targets for
  accessibility (TalkBack-friendly), and the DEG/RAD selector is shown with
  both color and a filled background so it isn't color-only.
