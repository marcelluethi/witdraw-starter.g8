package $package$

import dimwit.*

import scala.NamedTuple.NamedTuple
import scala.annotation.implicitNotFound

/*
 * Helpers for turning columns of a data frame into a tensor
 */

@implicitNotFound(
  "A column of \${T} cannot be turned into a tensor. Supported column types are Double, Float, Int, Long and Boolean. " +
    "If the column has missing values (an Option type), drop or fill them first, e.g. " +
    "rows.filter(_.x.isDefined) followed by .map(_.get), or .map(_.getOrElse(0.0))."
)
trait ColumnToTensor[T, V]:
  def toTensor1[L: Label](axis: Axis[L], values: Iterator[T]): Tensor1[L, V]

object ColumnToTensor:
  given ColumnToTensor[Double, Float32] with
    def toTensor1[L: Label](axis: Axis[L], values: Iterator[Double]) =
      Tensor1(axis).fromArray(values.map(_.toFloat).toArray)
  given ColumnToTensor[Float, Float32] with
    def toTensor1[L: Label](axis: Axis[L], values: Iterator[Float]) = Tensor1(axis).fromArray(values.toArray)
  given ColumnToTensor[Int, Int32] with
    def toTensor1[L: Label](axis: Axis[L], values: Iterator[Int]) = Tensor1(axis).fromArray(values.toArray)
  given ColumnToTensor[Long, Int64] with
    def toTensor1[L: Label](axis: Axis[L], values: Iterator[Long]) = Tensor1(axis).fromArray(values.toArray)
  given ColumnToTensor[Boolean, Bool] with
    def toTensor1[L: Label](axis: Axis[L], values: Iterator[Boolean]) = Tensor1(axis).fromArray(values.toArray)

/** How one cell becomes a `Float` in a matrix of several columns. Booleans become 1 and 0. */
@implicitNotFound(
  "A column of \${T} cannot be part of a Float32 matrix. Supported column types are Double, Float, Int, Long, Short, " +
    "Byte and Boolean. If a column has missing values (an Option type), drop or fill them first."
)
trait CellToFloat[T]:
  def apply(value: T): Float

object CellToFloat:
  given CellToFloat[Double] = _.toFloat
  given CellToFloat[Float] = identity(_)
  given CellToFloat[Int] = _.toFloat
  given CellToFloat[Long] = _.toFloat
  given CellToFloat[Short] = _.toFloat
  given CellToFloat[Byte] = _.toFloat
  given CellToFloat[Boolean] = if _ then 1f else 0f

/** Converts the values of a row, a tuple of column values, to `Float`s. Exists when every column converts. */
@implicitNotFound(
  "Columns of types \${V} cannot be part of a Float32 matrix. Every column must be Double, Float, Int, Long, Short, " +
    "Byte or Boolean. Select only such columns, and drop or fill missing values (Option columns) first."
)
trait RowToFloats[V <: Tuple]:
  def apply(row: V, out: Array[Float], from: Int): Unit
  def size: Int

object RowToFloats:
  given RowToFloats[EmptyTuple] with
    def apply(row: EmptyTuple, out: Array[Float], from: Int): Unit = ()
    def size = 0

  given [H, T <: Tuple](using head: CellToFloat[H], tail: RowToFloats[T]): RowToFloats[H *: T] with
    def apply(row: H *: T, out: Array[Float], from: Int): Unit =
      out(from) = head(row.head)
      tail(row.tail, out, from + 1)
    def size = tail.size + 1

/** A categorical column as integer codes: `codes(i)` is the index of row `i`'s value in `levels`. */
case class Categorical[L, T](codes: Tensor1[L, Int32], levels: IndexedSeq[T]):
  /** The number of distinct values. */
  def size: Int = levels.size

extension [T](values: IterableOnce[T])
  /** The values as a tensor along `axis`, in their natural value type (see [[ColumnToTensor]]). */
  def toTensor1[L: Label, V](axis: Axis[L])(using c: ColumnToTensor[T, V]): Tensor1[L, V] =
    c.toTensor1(axis, values.iterator)

  /** The values as a tensor along `axis`, converted to `vtype`, e.g. an `Int` column as `VType[Float32]`. */
  def toTensor1[L: Label, V, W](axis: Axis[L], vtype: VType[W])(using c: ColumnToTensor[T, V]): Tensor1[L, W] =
    c.toTensor1(axis, values.iterator).asType(vtype)

  /** The values as integer codes into their distinct values, which are sorted. */
  def toCategorical[L: Label](axis: Axis[L])(using Ordering[T]): Categorical[L, T] =
    val all = values.iterator.toVector
    val levels = all.distinct.sorted
    val index = levels.zipWithIndex.toMap
    Categorical(Tensor1(axis).fromArray(all.map(index).toArray), levels)

extension [K <: Tuple, V <: Tuple](rows: IterableOnce[NamedTuple[K, V]])
  /** The rows as a `Float32` matrix: one entry along `rowAxis` per row, one along `columnAxis` per column, in the
    * order of the columns. With scautable, select the columns first with `columns[("a", "b")]`.
    */
  def toTensor2[R: Label, C: Label](rowAxis: Axis[R], columnAxis: Axis[C])(using
      row: RowToFloats[V]
  ): Tensor2[R, C, Float32] =
    val data = rows.iterator.map: r =>
      val out = new Array[Float](row.size)
      row(r.toTuple, out, 0)
      out
    .toArray
    require(data.nonEmpty, "toTensor2 needs at least one row")
    Tensor2(rowAxis, columnAxis).fromArray(data)

  /** The column names, in order, e.g. as labels for the `columnAxis` of `toTensor2`. Does not consume the rows. */
  inline def columnNames: Seq[String] =
    compiletime.constValueTuple[K].toList.map(_.toString)
