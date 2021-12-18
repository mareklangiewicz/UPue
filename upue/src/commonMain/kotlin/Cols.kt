package pl.mareklangiewicz.upue

/**
 * Mini collections library
 *
 * Created by Marek Langiewicz on 18.02.16.
 *
 * Collection names are abbreviations to differ from standard Java and Kotlin collection names.
 */

// NOTE: I had some strange compilation problems related to type variance
// TODO SOMEDAY: read more about mixed site variance:
// http://www.cs.cornell.edu/~ross/publications/mixedsite/mixedsite-tate-fool13.pdf

interface IGet<T> { operator fun get(idx: Int): T }

interface ISet<T> { operator fun set(idx: Int, item: T) }

interface IContains<T> { operator fun contains(item: T): Boolean }

interface ILen { val len: Int }

interface IClr { fun clr() }


/** minimalist stack. pushes and pulls elements from the same side */
interface ISak<T> : IPush<T>, IPull<T?>, IPeek<T?> // I don't require the "len" property in all stacks intentionally.

/** minimalist queue. pushes elements to one side and pulls from the other */
interface IQue<T> : IPush<T>, IPull<T?>, IPeek<T?> // I don't require the "len" property in all queues intentionally.

interface IDeq<T> {
    val head: ISak<T>
    val tail: ISak<T>
}

fun <T> IDeq<T>.rev(): IDeq<T> = object : IDeq<T> {
    override val head = this@rev.tail
    override val tail = this@rev.head
}

fun <T> IDeq<T>.asQue(): IQue<T> = object : IQue<T> {
    override val push = tail.push
    override val pull = head.pull
}


interface ICol<T> : Iterable<T>, ILen, IContains<T>, IClr {
    override fun contains(item: T) = find { it == item } !== null
    override fun clr(): Unit = throw UnsupportedOperationException()
}


// IMPORTANT: methods behavior for indicies: idx < 0 || idx >= size   is UNDEFINED here!
// Subclasses may define for example negative indicies count backwards from the end,
// but we do NOT promise anything like that here in IArr.
// Any contract enchantments should be documented in particular implementation.
// TODO: Separate types for mutable stuff! (IMCol, IMArr etc)
interface IArr<T> : ICol<T>, IGet<T>, ISet<T> {

    override fun set(idx: Int, item: T) { throw UnsupportedOperationException("This arr not mutable") }

    override operator fun iterator(): Iterator<T> = Itor(this)

    class Itor<out T>(private val arr: IArr<T>, private var idx: Int = 0) : Iterator<T> {
        override fun hasNext(): Boolean = idx < arr.len
        override fun next(): T = arr[idx++]
    }
}

object ArrOf0: IArr<Nothing> {
    override fun get(idx: Int): Nothing = throw IndexOutOfBoundsException("EmptyArr has no element at idx: $idx")
    override fun set(idx: Int, item: Nothing) = throw IndexOutOfBoundsException("EmptyArr can't mutate element at idx: $idx")
    override val len: Int get() = 0
}

class ArrOf1<T>(private var item: T, private val mutable: Boolean = false): IArr<T> {
    override val len get() = 1
    override fun get(idx: Int) = item.also { idx.chk(0, 0) }
    override fun set(idx: Int, item: T) = if (mutable) { idx.chk(0, 0); this.item = item } else super.set(idx, item)
}

class ArrOf2<T>(private var first: T, private var second: T, private val mutable: Boolean = false): IArr<T> {
    override val len get() = 2
    override fun get(idx: Int) = if (idx.chk(0, 1) == 0) first else second
    override fun set(idx: Int, item: T) = if (mutable) { if(idx.chk(0, 1) == 0) this.first = item else second = item } else super.set(idx, item)
}

class ArrOf1Get<T>(private val get: () -> T): IArr<T> {
    override val len get() = 1
    override fun get(idx: Int) = idx.chk(0, 0).run { get() }
}

class ArrOfSame<T>(override val len: Int, private val value: T): IArr<T> {
    override fun get(idx: Int) = idx.chk(0, len-1).run { value }
}

inline fun <T> IArr<out T>?.orEmpty(): IArr<out T> = this ?: ArrOf0

fun <T> arrOf(): IArr<out T> = ArrOf0
fun <T> arrOf(item: T): IArr<T> = ArrOf1(item)
fun <T> arrOf(first: T, second: T): IArr<T> = ArrOf2(first, second)
fun <T> arrOf(vararg elem: T) = elem.asArr()
fun <T> Pair<T, T>.asArr(mutable: Boolean = false) = ArrOf2(first, second, mutable)

/**
 * Something like Python list slicing (start idx is included, stop idx is excluded)
 * Negative start and stop indicies are also interpreted like in Python
 * IMPORTANT: this implementation assumes that underlying array does not change its size
 * TODO SOMEDAY: third argument: step (also like in Python)
 * TODO LATER: test it!
 */
class ArrCut<T>(val arr: IArr<T>, astart: Int, astop: Int) : IArr<T> by arr {

    val stop : Int = astop.pos(arr.len).chk(0, arr.len)
    val start: Int = astart.pos(arr.len).chk(0, stop)
    override val len: Int = stop - start

    override fun get(idx: Int         ) = arr.get(start + idx.pos(len).chk(0, len-1)      )
    override fun set(idx: Int, item: T) = arr.set(start + idx.pos(len).chk(0, len-1), item)
    override operator fun iterator(): Iterator<T> = IArr.Itor(this) // overridden so it iterates only from start to stop
    override fun contains(item: T) = find { it == item } !== null // overridden so it searches only from start to stop
    override fun clr(): Unit = throw UnsupportedOperationException() // we make sure we do not clear whole backing arr
}

operator fun <T> IArr<T>.get(start: Int, stop: Int) = ArrCut(this, start, stop) // TODO LATER: test it


class ArrSum<T>(private val first: IArr<T>, private val second: IArr<T>): IArr<T> {
    override val len = first.len + second.len
    override fun get(idx: Int) = if (idx < first.len) first[idx] else second[idx - first.len]
    override fun set(idx: Int, item: T) = if (idx < first.len) first[idx] =  item else second[idx - first.len] = item
}

operator fun <T> IArr<T>.plus(other: IArr<T>) = ArrSum(this, other)



interface ILst<T> : IArr<T>, IDeq<T> {
    fun ins(idx: Int, item: T)
    fun del(idx: Int): T
    fun add(item: T) = ins(len, item)
    fun mov(src: Int, dst: Int) {
        if(dst == src) return
        val item = del(src)
        ins(if(dst > src) dst-1 else dst, item)
    }

    override fun clr() { // almost always this implementation should be overridden
        for(i in len-1 downTo 0)
            del(i)
    }
}







fun <T> Collection<T>.asCol() = object : ICol<T> {
    override val len: Int get() = this@asCol.size
    override fun iterator() = this@asCol.iterator()
    override fun contains(item: T) = this@asCol.contains(item)
}

fun <T> List<T>.asArr() = object : IArr<T> {
    override fun get(idx: Int) = this@asArr.get(idx)
    override val len: Int get() = this@asArr.size
    override fun iterator() = this@asArr.iterator()
    override fun contains(item: T) = this@asArr.contains(item)
}

fun <T> Array<T>.asArr(): IArr<T> = object : IArr<T> {
    override fun get(idx: Int) = this@asArr.get(idx)
    override fun set(idx: Int, item: T) { this@asArr.set(idx, item) }
    override val len: Int get() = this@asArr.size
    override fun iterator() = this@asArr.iterator()
}

fun <T> List<T>.asLst() = object : ILst<T> {
    override fun get(idx: Int) = this@asLst.get(idx)
    override fun set(idx: Int, item: T) { throw UnsupportedOperationException("This lst is not mutable") }
    override fun ins(idx: Int, item: T) { throw UnsupportedOperationException("This lst is not mutable") }
    override fun del(idx: Int): T { throw UnsupportedOperationException("This lst is not mutable") }
    override val len: Int get() = this@asLst.size
    override fun iterator() = this@asLst.iterator()
    override fun contains(item: T) = this@asLst.contains(item)

    override val head = LstHead(this)
    override val tail = LstTail(this)
}


fun <T> MutableCollection<T>.asMCol() = object : ICol<T> {
    override val len: Int get() = this@asMCol.size
    override fun iterator() = this@asMCol.iterator()
    override fun contains(item: T) = this@asMCol.contains(item)
    override fun clr() = this@asMCol.clear()
}


fun <T> MutableList<T>.asMArr() = object : IArr<T> {
    override fun get(idx: Int) = this@asMArr.get(idx)
    override fun set(idx: Int, item: T) { this@asMArr.set(idx, item) }

    override val len: Int get() = this@asMArr.size
    override fun iterator() = this@asMArr.iterator()
    override fun contains(item: T) = this@asMArr.contains(item)
    override fun clr() = this@asMArr.clear()
}

fun <T> MutableList<T>.asMLst() = object : ILst<T> {
    override fun get(idx: Int) = this@asMLst.get(idx)
    override fun set(idx: Int, item: T) { this@asMLst.set(idx, item) }

    override fun ins(idx: Int, item: T) = this@asMLst.add(idx, item)
    override fun del(idx: Int): T = this@asMLst.removeAt(idx)

    override val len: Int get() = this@asMLst.size
    override fun iterator() = this@asMLst.iterator()
    override fun contains(item: T) = this@asMLst.contains(item)
    override fun clr() = this@asMLst.clear()

    override val head = LstHead(this)
    override val tail = LstTail(this)
}

fun Int.pos(size: Int) = if(this < 0) size + this else this
fun Int.chk(min: Int, max: Int) = if(this < min || this > max) throw IndexOutOfBoundsException() else this




