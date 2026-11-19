package $package$

import dimwit.*

import plotwit.*
import plotwit.plots.{linePlot, scatterPlot}

import witdraw.*

import example.Regression.*

/** Plots of the penguin regression: body mass against each feature, with a regression line per draw, from the prior or
  * the posterior.
  */
object RegressionPlots:

  /** One line per draw, at the given features: the model run with its latents fixed to the draw, returning its `mean`,
    * the predicted body mass of each penguin (standardized). The key only draws a body mass, which is unused.
    */
  def lines(
      draws: Seq[ModelDraw],
      flipperLength: Tensor1[Penguin, Float32],
      billLength: Tensor1[Penguin, Float32]
  ) =
    draws.map: d =>
      ForwardSampler
        .run(Key(0)):
          observe(
            Alpha := d.alpha,
            BetaFlipper := d.betaFlipper,
            BetaBill := d.betaBill,
            Sigma := d.sigma
          )(model(flipperLength, billLength))
        .mean

  /** One panel: body mass against one feature, as the data (dots) with the lines on top, in grams. It knows nothing
    * about draws or latents; it gets the finished lines.
    *
    * @param feature
    *   each penguin's value of the feature, in mm, for the x axis
    * @param bodyMass
    *   each penguin's body mass, in g, for the dots
    * @param lines
    *   the lines from `lines`, standardized, one value per penguin
    * @param withAverage
    *   whether to draw the average of the lines on top (not for the prior, where it means little)
    */
  def regressionPlot(
      feature: Tensor1[Penguin, Float32],
      bodyMass: Tensor1[Penguin, Float32],
      lines: Seq[Tensor1[Penguin, Float32]],
      title: String,
      name: String,
      withAverage: Boolean
  ) =
    // Undo the standardization of body mass
    def grams(t: Tensor1[Penguin, Float32]) =
      (t *! bodyMass.std) +! bodyMass.mean
    // One orange line through (feature, ys in grams)
    def line(ys: Tensor1[Penguin, Float32], opacity: Double, width: Double) =
      linePlot(
        feature,
        grams(ys),
        _.encoding.x.title := s"$name (mm)",
        _.encoding.y.title := "body mass (g)",
        _.mark.color := "#f58518",
        _.mark.strokeWidth := width,
        _.mark.opacity := opacity,
        // Cut the lines off at the edges of the plot, whose axes the points set
        _.mark.clip := true
      )
    // The data, which also carries the title
    val points = scatterPlot(
      feature,
      bodyMass,
      _.title := title,
      _.encoding.x.title := s"$name (mm)",
      _.encoding.y.title := "body mass (g)"
    )
    // A faint line per draw, and their average (the sum divided by their number) solid
    val drawLines = lines.map(line(_, 0.15, 1))
    val averageLine =
      if withAverage then Seq(line(lines.reduce(_ + _) /! Tensor0(lines.size.toFloat), 1, 2))
      else Seq.empty
    // The layers on top of each other: dots, then faint lines, then the average
    overlay((points +: drawLines ++: averageLine)*)

  /** The two panels side by side, one per feature, since a panel has room for one feature on its x axis. Here the
    * features are picked: the model needs both, so the one not on the x axis is held at its average (0 once
    * standardized).
    */
  def regressionPlots(
      draws: Seq[ModelDraw],
      flipperLength: Tensor1[Penguin, Float32],
      billLength: Tensor1[Penguin, Float32],
      bodyMass: Tensor1[Penguin, Float32],
      what: String,
      withAverage: Boolean
  ) =
    val x1 = standardize(flipperLength)
    val x2 = standardize(billLength)
    val average = x1 *! Tensor0(0f) // 0 for every penguin
    hconcat(
      Seq(
        regressionPlot(
          flipperLength,
          bodyMass,
          lines(draws, x1, average), // real flipper lengths, average bill
          s"$what: body mass against flipper length, at average bill length",
          "flipper length",
          withAverage
        ),
        regressionPlot(
          billLength,
          bodyMass,
          lines(draws, average, x2), // average flipper, real bill lengths
          s"$what: body mass against bill length, at average flipper length",
          "bill length",
          withAverage
        )
      )
    )
