package top.byfree.massive.exception

/**
 * @author jbyan
 * @since 2024/5/1
 */
class MassiveParseException : RuntimeException {
    constructor() : super()

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
