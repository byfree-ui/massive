package top.byfree.massive

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * @author jbyan
 * @since 2024/5/1
 */


@SpringBootApplication
open class MassiveMain {

    companion object {
        @JvmStatic
        fun main(array: Array<String>) {
            SpringApplication.run(MassiveMain::class.java, *array)
        }
    }

}