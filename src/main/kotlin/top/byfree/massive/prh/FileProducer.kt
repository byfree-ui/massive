package top.byfree.massive.prh

import top.byfree.massive.dsl.Producer
import top.byfree.massive.exception.MassiveParseException
import java.io.File
import java.nio.charset.Charset

/**
 * @author jbyan
 * @since 2024/5/4
 */
class FileProducer<T>(
    private val mappingClass: Class<T>,
    private val path: String,
    private val name: String,
    private val space: String = "|",
    private val charset: Charset = Charsets.UTF_8,
) : Producer<T> {
    override fun exec(cb: (T) -> Unit) {
        val constructor = mappingClass.getConstructor()

        val fields = mappingClass.declaredFields

        val map = fields.filter { field ->
            field.isAnnotationPresent(Index::class.java)
        }.associateBy({ it.getAnnotation(Index::class.java).value }) {
            it
        }

        val maxKey = map.maxByOrNull { it.key }?.key

        val bufferedReader = File("${path}/${name}").bufferedReader(charset)

        bufferedReader.use { br ->
            br.lines().forEach {
                val newInstance = constructor.newInstance()

                val arr = it.split(space)

                if (maxKey == null || arr.size <= maxKey) {
                    throw MassiveParseException("pass file row len not has $maxKey Index value")
                }

                map.forEach { (index, field) ->
                    field.isAccessible = true
                    field.set(newInstance, arr[index])
                }

                cb(newInstance)

            }
        }
    }
}
