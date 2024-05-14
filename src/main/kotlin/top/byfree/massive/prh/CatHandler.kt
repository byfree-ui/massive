package top.byfree.massive.prh

import org.slf4j.LoggerFactory
import top.byfree.massive.dsl.Handler

/**
 * @author jbyan
 * @since 2024/5/3
 */
class CatHandler<T>(val clazz: Class<T>) : Handler<T> {

    private val log = LoggerFactory.getLogger(CatHandler::class.java)

    override fun exec(e: T): T {
        log.info("cat handler -> ${e.toString()}")
        return e
    }
}