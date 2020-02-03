package org.bonitasoft.engine.purge

import io.mockk.every
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @author Emmanuel Duchastenier
 * @author Pascal Garcia
 */
@ExtendWith(MockKExtension::class)
internal class DeleteOldProcessInstancesTest {

    @SpyK
    private var deleteOldProcessInstances = DeleteOldProcessInstances(true, "dummy url", mockk())

    @Test
    fun execute_should_fail_if_process_does_not_exist() {
        TestLoggerAppender.clear()

        every { deleteOldProcessInstances.getProcessDefinition(111L) } returns emptyList()
        every { deleteOldProcessInstances.quitWithCode(1) } throws RuntimeException("For test only")

        assertThrows<RuntimeException> {
            deleteOldProcessInstances.execute(111L, 11265345666L)
        }

        verify { deleteOldProcessInstances.quitWithCode(1) }
        assertThat(TestLoggerAppender.allLogs()).anyMatch { it.message.contains("No process definition exists for id 111. Exiting.") }
    }
}