# $name$

Bayesian models of your data with [WitDraw](https://github.com/dimwit-dev/witdraw), on
[DimWit](https://github.com/dimwit-dev/dimwit) tensors: read CSV files with
[scautable](https://github.com/Quafadas/scautable), plot them with [plotwit](https://github.com/dimwit-dev/plotwit).

## Setup

You need a JDK (17 or newer), [sbt](https://www.scala-sbt.org/) and [uv](https://docs.astral.sh/uv/). DimWit computes
with JAX, and WitDraw samples with BlackJax, both in Python; the first run creates that Python environment for you,
with uv.

## Run the examples

```bash
sbt "runMain $package$.explore"      # look at the penguins: a scatter plot and a histogram
sbt "runMain $package$.regression"   # Bayesian linear regression of body mass on flipper length, with NUTS
```

The plots open in your browser. `Regression.scala` shows the WitDraw workflow: sites, a model, `observe` to condition
it on data, `Latent.discover` for the posterior, NUTS in several chains, and `summary` and `tracePlot` to check them.
The [WitDraw quickstart](https://github.com/dimwit-dev/witdraw/blob/main/docs/quickstart.md) explains each step.

## Use your own data

1. Put your CSV file into `data/`.
2. Read it like `Explore.scala` does: `CSV.projectRoot("data/yourfile.csv").toVector`. scautable reads the file when
   it compiles your code, so the rows have a type, with one named field per column, and your editor completes the
   column names.
3. Turn columns into tensors along an axis of your own, with the helpers in `Tables.scala`:
   - `rows.column["x"].toTensor1(Axis[Sample])` for one column,
   - `rows.columns[("x", "y")].toTensor2(Axis[Sample], Axis[Feature])` for several.

   `Double` columns become `Float32`, the type plotwit plots take. For other columns, ask for it:
   `toTensor1(Axis[Sample], VType[Float32])`.

A column with missing values has an `Option` type and does not turn into a tensor until you drop or fill the gaps,
e.g. `rows.filter(_.x.isDefined)`. The compiler tells you so.

`Tables.scala` is part of your project: change or extend it as you need.

Both plotwit and WitDraw have a `plots` package. When you import both `plotwit.*` and `witdraw.*`, import plotwit's
plots by name, e.g. `import plotwit.plots.{linePlot, scatterPlot}`.

## Explore in a REPL

```bash
./repl.sh
```

starts a Scala REPL with the project on the classpath. Call `dimwit.initialize()` first. It needs
[coursier](https://get-coursier.io/) (`cs`). (`sbt console` does not work with Scala 3.9.)

## Data

`data/penguins.csv` is the [Palmer Penguins](https://allisonhorst.github.io/palmerpenguins/) data by Allison Horst,
Alison Hill and Kristen Gorman (CC0), without the 11 penguins with missing measurements.
