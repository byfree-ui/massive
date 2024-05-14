package top.byfree.massive.dsl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.byfree.massive.exception.MassiveParseException
import java.util.Objects
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * 单元主程序 dsl
 * @author jbyan
 * @since 2024/5/1
 */
const val START = "massive-start"
const val END = "massive-end"

const val SUCCESS = "massive-success"
const val FAIL = "massive-fail"

val log: Logger = LoggerFactory.getLogger("MassiveAction")

fun <T> massive(
    globalConfig: T,
    cb: Massive<T>.() -> kotlin.Unit
) {
    val massive = Massive(globalConfig)
    // 回调构建批量体
    cb(massive)
    // 执行批量程序
    try {
        exec(massive)
    } catch (e: MassiveParseException) {
        log.error("exec error", e)
    }
}

private fun <T> exec(massive: Massive<T>) {
    val units = massive.units
    val routeTable = massive.routeTable
    var unitId: String? = START

    while (!unitId.isNullOrBlank() && unitId != END) {
        val route = routeTable[unitId]
        if (Objects.isNull(route)) {
            throw MassiveParseException("no such route unit $unitId")
        }
        unitId = route!!.exec(units)
    }

}

fun massive(
    cb: Massive<MutableMap<String, Any>>.() -> kotlin.Unit
) {
    val mapOf = mutableMapOf<String, Any>()
    massive(mapOf, cb)
}


class Massive<T>(
    /* 全局变量 */
    val globalConfig: T
) {
    /* id -> 单元 */
    val units = mutableMapOf<String, Unit<T, Any>>()

    /* 路由表 */
    val routeTable = mutableMapOf<String, Route<T>>()
}

fun <T, I> Massive<T>.unit(innerConfig: I, cb: Unit<T, I>.() -> kotlin.Unit): String {
    val unit = Unit(globalConfig, innerConfig)
    cb(unit)
    if (Objects.isNull(unit.id) || Objects.isNull(unit.cell)) {
        throw MassiveParseException("parse unit err. the unit id or cell not be null")
    }
    if (units.contains(unit.id)) {
        throw MassiveParseException("repeat unit id [${unit.id}] is already in use")
    }
    units[unit.id!!] = unit as Unit<T, Any>

    return unit.id!!
}

fun <T> Massive<T>.route(current: String, success: String? = null, fail: String? = success) {
    routeTable[current] = DefaultRoute(current, success, fail)
}

fun <T> Massive<T>.parallelRoute(
    current: String,
    success: String? = null,
    fail: String? = success,
    cb: ParallelRoute<T>.() -> kotlin.Unit
) {
    val par = ParallelRoute<T>(current, success, fail)
    cb(par)
    routeTable[current] = par
}

fun <T> Massive<T>.unit(cb: Unit<T, MutableMap<String, Any>>.() -> kotlin.Unit): String {
    return unit(mutableMapOf(), cb)
}

open class Unit<T, I>(
    val globalConfig: T,
    val innerConfig: I,
) {

    var cell: AbstractCell<T, I>? = null
    var name: String? = null
    var id: String? = null

    fun config(cb: (Pair<T, I>) -> kotlin.Unit) {
        cb(Pair(globalConfig, innerConfig))
    }

    fun inner(cb: I.() -> kotlin.Unit) {
        cb(innerConfig)
    }

    fun global(cb: T.() -> kotlin.Unit) {
        cb(globalConfig)
    }
}

class ParallelRoute<T>(
    private val current: String,
    private val success: String?,
    private val fail: String?,
    private val threadPoll: Int = 6
) : Route<T> {
    private val routes = mutableListOf<Route<T>>()
    private val logger = LoggerFactory.getLogger(ParallelRoute::class.java)
    override fun exec(units: MutableMap<String, Unit<T, Any>>): String? {

        if (Objects.isNull(current)) {
            throw MassiveParseException("current route id not be null")
        }

        logger.info("start parallel unit [${current}], threadPoll [$threadPoll]")
        if (current != START) {
            val unit = units[current]
            unit?.let {
                logger.debug("-> unit id [${it.id}]")
                logger.debug("-> unit name [${it.name}]")
                logger.debug("-> unit cell [${it.cell!!::class.java}]")
            } ?: {
                throw MassiveParseException("route current unit id [${current}] not found")
            }
        }

        val pool = Executors.newFixedThreadPool(threadPoll)
        val resultList = mutableListOf<Future<CellStatus>>()
        routes.forEach {
            val fu = Callable {
                try {
                    val exec = it.exec(units)
                    return@Callable if (exec == SUCCESS) CellStatus.SUCCESS else if (exec == FAIL) CellStatus.FAIL else CellStatus.STOP
                } catch (e: Exception) {
                    // TODO log exception
                    return@Callable CellStatus.FAIL
                }
            }
            resultList.add(pool.submit(fu))
        }
        // 关闭线程池
        pool.shutdown()

        for (future in resultList) {
            val get = future.get()
            if (get != CellStatus.SUCCESS) {
                logger.info("end parallel route [$current], return [${CellStatus.FAIL}], next route [$fail]")
                return if (get == CellStatus.FAIL) fail else END
            }
        }
        logger.info("end parallel route [$current], return [${CellStatus.SUCCESS}], next route [$success]")
        return success
    }

    fun route(current: String) {
        routes.add(DefaultRoute(current, SUCCESS, FAIL))
    }

    fun parallelRoute(
        current: String,
        cb: ParallelRoute<T>.() -> kotlin.Unit
    ) {
        val par = ParallelRoute<T>(current, SUCCESS, FAIL)
        cb(par)
        routes.add(par)
    }

}

class DefaultRoute<T>(
    private val current: String,
    private val success: String? = null,
    private val fail: String? = success
) : Route<T> {
    private val logger = LoggerFactory.getLogger(DefaultRoute::class.java)

    override fun exec(units: MutableMap<String, Unit<T, Any>>): String? {

        if (current == START) {
            logger.info("start default route [$START]")
            logger.info("end default route [$START], return [${CellStatus.SUCCESS}], next route [$success]")
            return success
        }

        if (Objects.isNull(current)) {
            throw MassiveParseException("current route id not be null")
        }

        if (!units.containsKey(current)) {
            throw MassiveParseException("current route id no such")
        }
        val unit = units[current]!!
        logger.info("start default unit [${unit.id}]")
        logger.debug("-> unit id [${unit.id}]")
        logger.debug("-> unit name [${unit.name}]")
        logger.debug("-> unit cell [${unit.cell!!::class.java}]")

        val cell = unit.cell!!

        cell.inner = unit.innerConfig
        cell.global = unit.globalConfig


        val result = cell.apply()
        val next = if (result == CellStatus.SUCCESS) success else if (result == CellStatus.FAIL) fail else END


        logger.info("end default route [${unit.id}], return [$result], next route [$next]")


        return next
    }
}

abstract class AbstractCell<T, I> : Cell<T, I> {
    var inner: I? = null
    var global: T? = null
}

private interface Cell<T, I> {

    fun apply(): CellStatus

}

enum class CellStatus {
    SUCCESS, FAIL, STOP
}


interface Route<T> {
    fun exec(units: MutableMap<String, Unit<T, Any>>): String?
}





