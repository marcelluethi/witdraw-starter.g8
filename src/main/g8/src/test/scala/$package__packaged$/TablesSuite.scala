package $package$

import dimwit.*
import io.github.quafadas.table.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

trait Sample derives Label
trait Feature derives Label

private lazy val dimwitInit: Unit = dimwit.initialize()

class TablesSuite extends AnyFunSpec with Matchers:
  dimwitInit

  def rows = CSV
    .fromString("name,height,age,adult\nann,1.5,12,false\nbob,1.8,40,true\ncid,1.7,35,true")
    .toVector

  describe("toTensor1"):
    it("turns a Double column into Float32"):
      val heights: Tensor1[Sample, Float32] = rows.column["height"].toTensor1(Axis[Sample])
      heights.toArray shouldBe Array(1.5f, 1.8f, 1.7f)

    it("keeps an Int column as Int32"):
      val ages: Tensor1[Sample, Int32] = rows.column["age"].toTensor1(Axis[Sample])
      ages.toArray shouldBe Array(12, 40, 35)

    it("converts to a given value type"):
      val ages: Tensor1[Sample, Float32] = rows.column["age"].toTensor1(Axis[Sample], VType[Float32])
      ages.toArray shouldBe Array(12f, 40f, 35f)

    it("keeps a Boolean column as Bool"):
      val adults: Tensor1[Sample, Bool] = rows.column["adult"].toTensor1(Axis[Sample])
      adults.toArray shouldBe Array(false, true, true)

    it("works on the one-pass iterator a CSV reader returns"):
      val raw = CSV.fromString("x\n1.0\n2.0")
      raw.column["x"].toTensor1(Axis[Sample]).toArray shouldBe Array(1f, 2f)

    it("does not compile for a column with missing values"):
      val errors = compiletime.testing.typeCheckErrors(
        """CSV.fromString("x,y\n1.0,\n2.0,3.0").toVector.column["y"].toTensor1(Axis[Sample])"""
      )
      errors.map(_.message).mkString should include("drop or fill them first")

  describe("toTensor2"):
    it("puts the selected columns side by side as Float32, booleans as 0 and 1"):
      val x: Tensor2[Sample, Feature, Float32] =
        rows.columns[("height", "age", "adult")].toTensor2(Axis[Sample], Axis[Feature])
      x.shape(Axis[Sample]) shouldBe 3
      x.shape(Axis[Feature]) shouldBe 3
      x.toArray shouldBe Array(Array(1.5f, 12f, 0f), Array(1.8f, 40f, 1f), Array(1.7f, 35f, 1f))

    it("does not compile when a column is not numeric"):
      val errors = compiletime.testing.typeCheckErrors(
        "rows.columns[(\"name\", \"age\")].toTensor2(Axis[Sample], Axis[Feature])"
      )
      errors.map(_.message).mkString should include("cannot be part of a Float32 matrix")

  describe("columnNames"):
    it("lists the selected columns in order"):
      rows.columns[("height", "age")].columnNames shouldBe Seq("height", "age")

  describe("plain named tuples"):
    it("work without a CSV reader"):
      val points = Vector((x = 1.0, y = 2.0), (x = 3.0, y = 4.0))
      points.map(_.x).toTensor1(Axis[Sample]).toArray shouldBe Array(1f, 3f)
      points.toTensor2(Axis[Sample], Axis[Feature]).toArray shouldBe Array(Array(1f, 2f), Array(3f, 4f))
      points.columnNames shouldBe Seq("x", "y")

  describe("toCategorical"):
    it("codes the values by their index among the sorted distinct values"):
      val names = Vector("b", "a", "b", "c").toCategorical(Axis[Sample])
      names.levels shouldBe Vector("a", "b", "c")
      names.codes.toArray shouldBe Array(1, 0, 1, 2)
