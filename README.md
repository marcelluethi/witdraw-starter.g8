# witdraw-starter.g8

A [giter8](https://www.foundweekends.org/giter8/) template for a project with
[WitDraw](https://github.com/dimwit-dev/witdraw), [DimWit](https://github.com/dimwit-dev/dimwit),
[plotwit](https://github.com/dimwit-dev/plotwit) and [scautable](https://github.com/Quafadas/scautable): read a CSV
file, plot it, and fit a Bayesian model to it.

```bash
sbt new dimwit-dev/witdraw-starter.g8
```

You need a JDK (17 or newer), sbt and [uv](https://docs.astral.sh/uv/). The generated project's README explains the
rest.

## What you get

- `data/penguins.csv`: the Palmer Penguins data, to start with.
- `Explore.scala`: reads the CSV with scautable and plots it.
- `Regression.scala`: a Bayesian linear regression, sampled with NUTS, checked with a summary and trace plots.
- `Tables.scala`: turns table columns and rows into DimWit tensors. It is part of the generated project, to change
  as you need.
- `repl.sh`: a Scala REPL with the project on the classpath.

## Working on the template

The template is in [src/main/g8](src/main/g8). `sbt test` generates a project from it and runs that project's tests.
To try it by hand:

```bash
sbt new file://$PWD --name="Penguin Models" --package=org.penguins
```
