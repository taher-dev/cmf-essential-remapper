package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetRingerModeUseCaseTest {

    @Test
    fun testSelectingCurrentModeDoesNotTriggerChange() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(initialMode = RingerMode.RING)
        val useCase = SetRingerModeUseCase(fakeRepo)

        val result = useCase(RingerMode.RING)

        assertTrue(result.isSuccess)
        assertEquals(RingerMode.RING, result.getOrNull())
        assertEquals(0, fakeRepo.setModeCallCount) // No redundant operation
    }

    @Test
    fun testSilentModeSelectionSucceedsDirectlyWithoutDnd() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(initialMode = RingerMode.RING)
        val useCase = SetRingerModeUseCase(fakeRepo)

        val result = useCase(RingerMode.SILENT)

        assertTrue(result.isSuccess)
        assertEquals(RingerMode.SILENT, result.getOrNull())
        assertEquals(1, fakeRepo.setModeCallCount)
        assertEquals(RingerMode.SILENT, fakeRepo.getCurrentRingerMode())
    }

    @Test
    fun testVibrateModeSelectionSucceeds() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(initialMode = RingerMode.RING)
        val useCase = SetRingerModeUseCase(fakeRepo)

        val result = useCase(RingerMode.VIBRATE)

        assertTrue(result.isSuccess)
        assertEquals(RingerMode.VIBRATE, result.getOrNull())
        assertEquals(1, fakeRepo.setModeCallCount)
        assertEquals(RingerMode.VIBRATE, fakeRepo.getCurrentRingerMode())
    }

    @Test
    fun testRingModeSelectionSucceeds() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(initialMode = RingerMode.VIBRATE)
        val useCase = SetRingerModeUseCase(fakeRepo)

        val result = useCase(RingerMode.RING)

        assertTrue(result.isSuccess)
        assertEquals(RingerMode.RING, result.getOrNull())
        assertEquals(1, fakeRepo.setModeCallCount)
        assertEquals(RingerMode.RING, fakeRepo.getCurrentRingerMode())
    }

    @Test
    fun testFailedModeChangePropagatesFailure() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(
            initialMode = RingerMode.RING,
            shouldFailSetMode = true
        )
        val useCase = SetRingerModeUseCase(fakeRepo)

        val result = useCase(RingerMode.VIBRATE)

        assertTrue(result.isFailure)
        assertEquals("Hardware audio service error", result.exceptionOrNull()?.message)
    }
}
