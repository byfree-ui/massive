package top.byfree.massive.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.byfree.massive.batch.Batch
import top.byfree.massive.context.BootBeanGetter

/**
 * @author jbyan
 * @since 2024/5/12
 */
@RestController
@RequestMapping("batch")
class BatchController(
    private val bootBeanGetter: BootBeanGetter
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)


    @GetMapping("{batchName}")
    fun batch(
        @PathVariable batchName: String
    ): String {
        val batchBean = bootBeanGetter.getBean(batchName, Batch::class.java)

        return batchBean?.let {
            it.apply()
            "exec batch success"
        } ?: "no batch found for $batchName"
    }



}