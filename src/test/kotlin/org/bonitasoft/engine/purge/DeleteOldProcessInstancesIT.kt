package org.bonitasoft.engine.purge

import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @author Emmanuel Duchastenier
 */
@ExtendWith(MockKExtension::class)
internal class DeleteOldProcessInstancesIT {

    @SpyK
    private var deleteOldProcessInstances = DeleteOldProcessInstances(true, "dummy url", mockk())

}