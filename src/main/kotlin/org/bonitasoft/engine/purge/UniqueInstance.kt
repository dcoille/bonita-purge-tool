/**
 * Copyright (C) 2020 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.purge

import java.io.File

/**
 * @author Emmanuel Duchastenier
 */
object UniqueInstance {

    private val lockFile = File(System.getProperty("java.io.tmpdir"), "bonita-purge-tool.lock")

    private fun isAlreadyRunning() = lockFile.exists()

    fun createSemaphore(): Boolean {
        return if (isAlreadyRunning()) {
            logger.error("""
                |An instance of Bonita Purge Tool is already running.
                |You cannot run it twice on the same machine.
                |If you think this is incorrect, remove file '${lockFile.absolutePath}' and launch again.""".trimMargin())
            false
        } else {
            lockFile.createNewFile()
            lockFile.deleteOnExit()
            true
        }
    }

    fun releaseSemaphore() {
        if (lockFile.exists())
            lockFile.delete()
    }
}