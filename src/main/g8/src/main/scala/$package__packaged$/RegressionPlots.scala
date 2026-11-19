package $package$

import dimwit.*

import plotwit.*
import plotwit.plots.{linePlot, scatterPlot}

import witdraw.*

import example.Regression.*

/** The plot of the penguin regression: body mass against flipper length, with a regression line per draw, from the
  * prior or the posterior.
  */
object RegressionPlots:

  /** Body mass against flipper length, as the data (dots) with one line per draw on top, in grams. Each draw's `mean`
    * is its line already: the predicted body mass of each penguin, at its own flipper length (standardized).
    *
    * @param flipperLength
    *   each penguin's flipper length, in mm, for the x axis
    * @param bodyMass
    *   each penguin's body mass, in g, for the dots
    * @param withAverage
    *   whether to draw the average of the lines on top (not for the prior, where it means little)
    */
  def regressionPlot(
      draws: Seq[ModelDraw],
      flipperLength: Tensor1[Penguin, Float32],
      bodyMass: Tensor1[Penguin, Float32],
      title: String,
      withAverage: Boolean
  ) =
    // Undo the standardization of body mass
    def grams(t: Tensor1[Penguin, Float32]) =
      (t *! bodyMass.std) +! bodyMass.mean
    // One orange line through (flipper length, ys in grams)
    def line(ys: Tensor1[Penguin, Float32], opacity: Double, width: Double) =
      linePlot(
        flipperLength,
        grams(ys),
        _.encoding.x.title := "flipper length (mm)",
        _.encoding.y.title := "body mass (g)",
        _.mark.color := "#f58518",
        _.mark.strokeWidth := width,
        _.mark.opacity := opacity,
        // Cut the lines off at the edges of the plot, whose axes the points set
        _.mark.clip := true
      )
    // The data, which also carries the title
    val points = scatterPlot(
      flipperLength,
      bodyMass,
      _.title := title,
      _.encoding.x.title := "flipper length (mm)",
      _.encoding.y.title := "body mass (g)"
    )
    // A faint line per draw, and their average (the sum divided by their number) solid
    val lines = draws.map(_.mean)
    val drawLines = lines.map(line(_, 0.15, 1))
    val averageLine =
      if withAverage then Seq(line(lines.reduce(_ + _) /! Tensor0(lines.size.toFloat), 1, 2))
      else Seq.empty
    // The layers on top of each other: dots, then faint lines, then the average
    overlay((points +: drawLines ++: averageLine)*)
