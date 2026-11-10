package $package$

import dimwit.*
import io.github.quafadas.table.*
import plotwit.*
import plotwit.PlotTargets.desktopBrowser

/** Reads the penguins from data/penguins.csv and looks at them: bill length against bill depth, and the distribution of
  * flipper lengths. The plots open in your browser.
  *
  * Run it with `sbt "runMain $package$.explore"`.
  */
object Explore:
  // One label per kind of axis: here, one entry per penguin
  trait Penguin derives Label

  // scautable reads the CSV at compile time and gives each row a type, with one named field per column:
  // (species: String, island: String, bill_length_mm: Double, ..., year: Int)
  val penguins = CSV.projectRoot("data/penguins.csv").toVector

  @main def explore(): Unit =
    dimwit.initialize()

    val billLength = penguins.column["bill_length_mm"].toTensor1(Axis[Penguin])
    val billDepth = penguins.column["bill_depth_mm"].toTensor1(Axis[Penguin])
    val flipperLength = penguins.column["flipper_length_mm"].toTensor1(Axis[Penguin], VType[Float32])
    println("Penguins: " + billLength.shape(Axis[Penguin]))

    val bills = plots.scatterPlot(
      billLength,
      billDepth,
      penguins.column["species"],
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
