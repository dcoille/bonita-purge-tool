package org.bonitasoft.engine.purge

import ch.qos.logback.classic.Level
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

        every { deleteOldProcessInstances.checkTenantIdValidity(any()) } returns 0L
        every { deleteOldProcessInstances.getProcessDefinition(111L) } returns emptyList()
        every { deleteOldProcessInstances.quitWithCode(1) } throws RuntimeException("For test only")

        assertThrows<RuntimeException> {
            deleteOldProcessInstances.execute(111L, 11265345666L, null)
        }

        verify { deleteOldProcessInstances.quitWithCode(1) }
        assertThat(TestLoggerAppender.allLogs()).anyMatch { it.message.contains("No process definition exists for id 111. Exiting.") }
    }

    @Test
    fun `multi-tenant platform with tenantId not set should display error message`() {
        TestLoggerAppender.clear()

        every { deleteOldProcessInstances.getAllTenants() } returns mapOf(Pair(1L, "default"), Pair(101L, "Second"))
        every { deleteOldProcessInstances.quitWithCode(1) } throws RuntimeException("For test only")

        assertThrows<RuntimeException> {
            deleteOldProcessInstances.checkTenantIdValidity(null)
        }
        assertThat(TestLoggerAppender.allLogs()).anyMatch { it.message.contains("Multiple tenants exist [1=default, 101=Second]. Please specify tenant ID as 3rd parameter") }
    }

    @Test
    fun `no-tenant platform should display error message`() {
        TestLoggerAppender.clear()

        every { deleteOldProcessInstances.getAllTenants() } returns emptyMap()
        every { deleteOldProcessInstances.quitWithCode(1) } throws RuntimeException("For test only")

        assertThrows<RuntimeException> {
            deleteOldProcessInstances.checkTenantIdValidity(null)
        }
        assertThat(TestLoggerAppender.allLogs()).anyMatch { it.message.contains("No tenant exists. Platform invalid") }
    }

    @Test
    fun `non-existing tenantId should display error message`() {
        TestLoggerAppender.clear()

        every { deleteOldProcessInstances.getAllTenants() } returns mapOf(Pair(1L, "default"))
        every { deleteOldProcessInstances.quitWithCode(1) } throws RuntimeException("For test only")

        assertThrows<RuntimeException> {
            deleteOldProcessInstances.checkTenantIdValidity(42L)
        }
        assertThat(TestLoggerAppender.allLogs()).anyMatch { it.message.contains("Tenant with ID 42 does not exist. Available tenants are") }
    }

    @Test
    fun `mono-tenant platform should use this tenant if tenantId not set`() {
        every { deleteOldProcessInstances.getAllTenants() } returns mapOf(Pair(7L, "other tenant"))

        val tenantIdValidity = deleteOldProcessInstances.checkTenantIdValidity(null)

        assertThat(tenantIdValidity).isEqualTo(7L)
    }

    @Test
    fun `mono-tenant platform should use passed tenant if tenantId IS set`() {
        every { deleteOldProcessInstances.getAllTenants() } returns mapOf(Pair(8L, "other tenant"))

        val tenantIdValidity = deleteOldProcessInstances.checkTenantIdValidity(8L)

        assertThat(tenantIdValidity).isEqualTo(8L)
    }

    @Test
    fun `should log warning and continue if no process instance exists`() {
        TestLoggerAppender.clear()

        every { deleteOldProcessInstances.checkTenantIdValidity(1000L) } returns 1000L
        every { deleteOldProcessInstances.getProcessDefinition(999888777L) } returns listOf(Pair("MyProcess", "1.1"))
        every { deleteOldProcessInstances.countArchivedProcessInstances(999888777L) } returns 0
        every { deleteOldProcessInstances.doExecutePurge(any(), any(), any())} returns Unit

        deleteOldProcessInstances.execute(999888777L, 165000000000L, 1000L)

        assertThat(TestLoggerAppender.allLogs()).anyMatch {
            it.level == Level.WARN && it.message.contains("""No finished process instance exists for process 'MyProcess' in version '1.1'.
            |Continuing will purge all archived orphan elements that may remain from a previous interrupted purge execution.""".trimMargin())
        }
    }
}