package pl.mareklangiewicz.upue

/**
 * Mini collections library
 *
 * Created by Marek Langiewicz on 18.02.16.
 *
 * Collection names are abbreviations - it's an intentional strange style - mostly to differ
 * from standard Java and Kotlin collection names. (also to communicate that this lib is hacky / experimental)
 */

// TODO: rename whole thing as "MutLst" and publish as separate mini multiplatform library.
// (separate from UPue)


// TODO_maybe: read more about mixed site variance:
// http://www.cs.cornell.edu/~ross/publications/mixedsite/mixedsite-tate-fool13.pdf

interface IGet<out T> { operator fun get(idx: Int): T }

interface ISet<in T> { operator fun set(idx: Int, item: T) }

interface IContains { operator fun contains(item: Any?): Boolean }

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


interface ICol<out T> : Iterable<T>, ILen, IContains {
    override fun contains(item: Any?) = indexOf(item) >= 0
}


// IMPORTANT: methods behavior for indices: idx < 0 || idx >= size   is UNDEFINED here!
// Subclasses may define for example negative indices count backwards from the end,
// but we do NOT promise anything like that here in IArr.
// Any contract enchantments should be documented in particular implementation.
interface IArr<out T> : ICol<T>, IGet<T> {

    override operator fun iterator(): Iterator<T> = Itor(this)

    class Itor<out T>(private val arr: IArr<T>, private var idx: Int = 0) : Iterator<T> {
        override fun hasNext(): Boolean = idx < arr.len
        override fun next(): T = arr[idx++]
    }
}

interface IMutArr<T> : ICol<T>, IArr<T>, ISet<T>

object MutArrOf0: IMutArr<Nothing> {
    override fun get(idx: Int): Nothing = throw IndexOutOfBoundsException("EmptyArr has no element at idx: $idx")
    override fun set(idx: Int, item: Nothing) = throw IndexOutOfBoundsException("EmptyArr can't mutate element at idx: $idx")
    override val len: Int get() = 0
}

class MutArrOf1<T>(private var item: T): IMutArr<T> {
    override val len get() = 1
    override fun get(idx: Int) = item.also { idx.chk(0, 0) }
    override fun set(idx: Int, item: T) { idx.chk(0, 0); this.item = item }
}

class MutArrOf2<T>(private var first: T, private var second: T): IMutArr<T> {
    override val len get() = 2
    override fun get(idx: Int) = if (idx.chk(0, 1) == 0) first else second
    override fun set(idx: Int, item: T) { if(idx.chk(0, 1) == 0) this.first = item else second = item }
}

class ArrOf1Get<out T>(private val get: () -> T): IArr<T> {
    override val len get() = 1
    override fun get(idx: Int) = idx.chk(0, 0).run { get() }
}

class ArrOfSame<T>(override var len: Int, var value: T): IArr<T> {
    override fun get(idx: Int) = idx.chk(0, len-1).run { value }
}

inline fun <T> IArr<T>?.orEmpty(): IArr<T> = this ?: MutArrOf0

fun <T> mutArrOf() = MutArrOf0
fun <T> mutArrOf(item: T) = MutArrOf1(item)
fun <T> mutArrOf(first: T, second: T) = MutArrOf2(first, second)
fun <T> mutArrOf(vararg elem: T) = elem.asMutArr()
fun <T> Pair<T, T>.asMutArr() = MutArrOf2(first, second)

fun <T> arrOf() = MutArrOf0 as IArr<T>
fun <T> arrOf(item: T) = MutArrOf1(item) as IArr<T>
fun <T> arrOf(first: T, second: T) = MutArrOf2(first, second) as IArr<T>
fun <T> arrOf(vararg elem: T) = elem.asArr()
fun <T> Pair<T, T>.asArr() = MutArrOf2(first, second) as IArr<T>

/**
 * Something like Python list slicing (start idx is included, stop idx is excluded)
 * Negative start and stop indicies are also interpreted like in Python
 * IMPORTANT: this implementation assumes that underlying array does not change its size
 * TODO SOMEDAY: third argument: step (also like in Python)
 * TODO LATER: test it!
 */
open class ArrCut<out T, ArrT: IArr<T>>(val src: ArrT, astart: Int, astop: Int) : IArr<T> {

    val stop : Int = astop.pos(src.len).chk(0, src.len)
    val start: Int = astart.pos(src.len).chk(0, stop)
    override val len: Int = stop - start

    override fun get(idx: Int) = src[start + idx.pos(len).chk(0, len-1)]
    override operator fun iterator(): Iterator<T> = IArr.Itor(this)
}

class MutArrCut<T>(asrc: IMutArr<T>, astart: Int, astop: Int) : IMutArr<T>, ArrCut<T, IMutArr<T>>(asrc, astart, astop) {
    override fun set(idx: Int, item: T) { src[start + idx.pos(len).chk(0, len-1)] = item }
}

operator fun <T> IArr<T>.get(start: Int, stop: Int) = ArrCut(this, start, stop) // TODO LATER: test it
operator fun <T> IMutArr<T>.get(start: Int, stop: Int) = MutArrCut(this, start, stop) // TODO LATER: test it


open class ArrSum<out T, ArrT: IArr<T>>(val first: ArrT, val second: ArrT): IArr<T> {
    override val len get() = first.len + second.len
    override fun get(idx: Int) = if (idx < first.len) first[idx] else second[idx - first.len]
}

class MutArrSum<T>(afirst: IMutArr<T>, asecond: IMutArr<T>): IMutArr<T>, ArrSum<T, IMutArr<T>>(afirst, asecond) {
    override fun set(idx: Int, item: T) = if (idx < first.len) first[idx] =  item else second[idx - first.len] = item
}

operator fun <T> IArr<T>.plus(other: IArr<T>) = ArrSum(this, other)



interface IMutLst<T> : IMutArr<T>, IDeq<T>, IClr {
    fun ins(idx: Int, item: T)
    fun del(idx: Int): T
    fun add(item: T) = ins(len, item)
    fun mov(src: Int, dst: Int) {
        if(dst == src) return
        val item = del(src)
        ins(if(dst > src) dst-1 else dst, item)
    }
    override fun clr() { for(i in len-1 downTo 0) del(i) }
        // almost always this implementation should be overridden
}



fun <T> Collection<T>.asCol() = object : ICol<T> {
    override val len: Int get() = this@asCol.size
    override fun iterator() = this@asCol.iterator()
    override fun contains(item: Any?) = item in this@asCol
}

fun <T> List<T>.asArr() = object : IArr<T> {
    override val len: Int get() = this@asArr.size
    override fun iterator() = this@asArr.iterator()
    override fun get(idx: Int) = this@asArr.get(idx)
    override fun contains(item: Any?) = item in this@asArr
}

fun <T> Array<T>.asArr() = asMutArr() as IArr<T>
fun <T> Array<T>.asMutArr() = object : IMutArr<T> {
    override val len: Int get() = this@asMutArr.size
    override fun iterator() = this@asMutArr.iterator()
    override fun get(idx: Int) = this@asMutArr[idx]
    override fun set(idx: Int, item: T) { this@asMutArr[idx] = item }
}

fun <T> MutableList<T>.asMutLst() = object : IMutLst<T> {
    override fun get(idx: Int) = this@asMutLst[idx]
    override fun set(idx: Int, item: T) { this@asMutLst[idx] = item }

    override fun ins(idx: Int, item: T) = this@asMutLst.add(idx, item)
    override fun del(idx: Int): T = this@asMutLst.removeAt(idx)

    override val len: Int get() = this@asMutLst.size
    override fun iterator() = this@asMutLst.iterator()
    override fun contains(item: Any?): Boolean = item in this@asMutLst
    override fun clr() = this@asMutLst.clear()

    override val head = LstHead(this)
    override val tail = LstTail(this)
}

fun Int.pos(size: Int) = if(this < 0) size + this else this
fun Int.chk(min: Int, max: Int) = if(this < min || this > max) throw IndexOutOfBoundsException() else this




