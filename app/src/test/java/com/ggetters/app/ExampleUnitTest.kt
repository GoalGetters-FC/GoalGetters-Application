package com.ggetters.app

import org.junit.Test

import org.junit.Assert.*
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class AdditionTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}


data class Entity(val id:String, val updatedAt:Long, val dirty:Boolean)

class DirtyFlagUseCase {
    fun markDirty(local: Entity, hasRemote:Boolean): Entity =
        if (!hasRemote) local.copy(dirty = true) else local
    fun resolve(local: Entity, remote: Entity): Entity =
        if (local.updatedAt >= remote.updatedAt) local else remote.copy(dirty = false)
}

class DirtyFlagUseCaseTest {
    private val useCase = DirtyFlagUseCase()

    @Test fun `new local rows become dirty when no remote exists`() {
        val e = Entity("1", 100, dirty = false)
        val out = useCase.markDirty(e, hasRemote = false)
        assertTrue(out.dirty)
    }

    @Test fun `latest updatedAt wins`() {
        val local = Entity("1", 200, dirty = true)
        val remote = Entity("1", 150, dirty = false)
        val resolved = useCase.resolve(local, remote)
        assertEquals(local, resolved)
    }
}
