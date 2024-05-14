package top.byfree.massive.context

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class BootBeanGetter : ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    fun <T> getBean(requiredType: Class<T>): T? {
        return applicationContext.getBean(requiredType)
    }

    fun getBean(name: String): Any? {
        return applicationContext.getBean(name)
    }

    fun <T> getBean(name: String, requiredType: Class<T>): T? {
        return applicationContext.getBean(name, requiredType)
    }

    fun getApplicationContext(): ApplicationContext {
        return applicationContext
    }
}