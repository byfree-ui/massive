package top.byfree.massive.dsl

import top.byfree.massive.exception.MassiveParseException
import java.util.*

/**
 * @author jbyan
 * @since 2024/5/2
 */

class PrhCell<T, E> : AbstractCell<T, MateData<E>>() {
    override fun apply(): CellStatus {

        val producer = inner!!.producer
        val handler = inner!!.handler

        if (producer == null) {
            throw MassiveParseException("Producer cannot be null")
        }

        try {
            handler.forEach { it.start() }

            producer.exec { pd ->
                var temp = pd
                handler.forEach { hl ->
                    temp = hl.exec(temp)
                }
            }

            handler.forEach { it.end() }

            return CellStatus.SUCCESS
        } catch (e: Exception) {
            // TODO log
            return CellStatus.FAIL
        }
    }
}

fun <T, E> Massive<T>.prhUnit(clazz: Class<E>, cb: PrhUnit<T, E>.() -> kotlin.Unit): String {

    val prhUnit = PrhUnit(globalConfig, MateData(clazz))

    cb(prhUnit)
    if (prhUnit.cell != null) {
        // TODO log
    }
    prhUnit.cell = PrhCell()

    if (Objects.isNull(prhUnit.id)) {
        throw MassiveParseException("parse unit err. the unit id not be null")
    }
    if (units.contains(prhUnit.id)) {
        throw MassiveParseException("repeat unit id [${prhUnit.id}] is already in use")
    }

    units[prhUnit.id!!] = prhUnit as Unit<T, Any>

    return prhUnit.id!!
}

class PrhUnit<T, E>(
    globalConfig: T,
    mateDate: MateData<E>
) : Unit<T, MateData<E>>(globalConfig, mateDate) {

    fun handler(cb: (T) -> Handler<E>) {
        this.innerConfig.handler.add(cb(globalConfig))
    }

    fun producer(cb: (T) -> Producer<E>) {
        this.innerConfig.producer = cb(globalConfig)
    }

}


class MateData<E>(
    private val clazz: Class<E>
) {
    var producer: Producer<E>? = null
    val handler: MutableList<Handler<E>> = mutableListOf()
}

interface Producer<E> {

    fun exec(cb: (E) -> kotlin.Unit)
}

interface Handler<E> {
    fun start() {}

    fun exec(e: E): E

    fun end() {}
}
