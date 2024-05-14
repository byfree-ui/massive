package top.byfree.massive.prh

import top.byfree.massive.dsl.Handler
import top.byfree.massive.orm.RowSet
import java.io.BufferedWriter
import java.io.File
import java.nio.charset.Charset

/**
 * @author jbyan
 * @since 2024/5/3
 */
class FileWriteHandler(
    private val path: String,
    private val name: String,
    private val space: String = "|",
    private val pre: String = "",
    private val post: String = "",
    private val charset: Charset = Charsets.UTF_8,
    private val order: MutableList<String>
) : Handler<RowSet> {

    private var bufferedWriter: BufferedWriter? = null

    override fun start() {

        bufferedWriter = File("${path}/${name}").bufferedWriter(charset)

    }

    override fun exec(e: RowSet): RowSet {

        val line = order.joinToString(space, prefix = pre, postfix = post) { e.getString(it) }

        bufferedWriter?.write(line)

        bufferedWriter?.flush()

        return e

    }

    override fun end() {

        bufferedWriter?.close()

    }
}