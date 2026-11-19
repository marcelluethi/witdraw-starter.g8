package $package$

import dimwit.*
import dimwit.conversions.*
import io.github.quafadas.table.*
import plotwit.*
import plotwit.PlotTargets.desktopBrowser

object Explore:

  trait Penguin derives Label

  // scautable reads the CSV at compile time and gives each row a type, with one named field per column:
  // (species: String, island: String, bill_length_mm: Double, ..., year: Int)
  val penguins = CSV.projectRoot("data/penguins.csv").toVector

  @main def runExplore(): Unit =
    dimwit.initialize()

    val billLength =
      penguins.map(_.bill_length_mm.toFloat).toTensor1(Axis[Penguin])
    val billDepth =
      penguins.map(_.bill_depth_mm.toFloat).toTensor1(Axis[Penguin])
    val flipperLength =
      penguins.map(_.flipper_length_mm.toFloat).toTensor1(Axis[Penguin])
    println("Penguins: " + billLength.shape(Axis[Penguin]))

    val bills = plots.scatterPlot(
      billLength,
      billDepth,
      penguins.map(_.species),
      _.title := "Bills",
      _.encoding.x.title := "bill length (mm)",
      _.encoding.y.title := "bill depth (mm)",
      _.encoding.color.legend.title := "species"
    )
    val flippers = plots.histogramPlot(
      flipperLength,
      _.title := "Flipper lengths (mm)"
    )
    display(hconcat(Seq(bills, flippers)))
