package top.byfree.massive.prh

/**
 * @author jbyan
 * @since 2024/5/4
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Index(val value: Int)
