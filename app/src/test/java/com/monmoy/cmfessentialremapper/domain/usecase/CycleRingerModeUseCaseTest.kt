package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CycleRingerModeUseCaseTest {

    @Test
    fun testCycleFromRingToSilent() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(initialMode = RingerMode.RING)
        val cycleUseCase = CycleRingerModeUseCase(fakeRepo)

        val result = cycleUseCase()

        assertTrue(result.isSuccess)
        assertEquals(RingerMode.SILENT, result.getOrNull())
        assertEquals(RingerMode.SILENT, fakeRepo.getCurrentRingerMode())
    }

    @Test
    fun testCycleFromSilentToVibrate() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(initialMode = RingerMode.SILENT)
        val cycleUseCase = CycleRingerModeUseCase(fakeRepo)

        val result = cycleUseCase()

        assertTrue(result.isSuccess)
        assertEquals(RingerMode.VIBRATE, result.getOrNull())
        assertEquals(RingerMode.VIBRATE, fakeRepo.getCurrentRingerMode())
    }

    @Test
    fun testCycleFromVibrateToRing() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(initialMode = RingerMode.VIBRATE)
        val cycleUseCase = CycleRingerModeUseCase(fakeRepo)

        val result = cycleUseCase()

        assertTrue(result.isSuccess)
        assertEquals(RingerMode.RING, result.getOrNull())
        assertEquals(RingerMode.RING, fakeRepo.getCurrentRingerMode())
    }

    @Test
    fun testFullThreeStateCycleSequence() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(initialMode = RingerMode.RING)
        val cycleUseCase = CycleRingerModeUseCase(fakeRepo)

        // 1st cycle: RING -> SILENT
        assertEquals(RingerMode.SILENT, cycleUseCase().getOrNull())
        // 2nd cycle: SILENT -> VIBRATE
        assertEquals(RingerMode.VIBRATE, cycleUseCase().getOrNull())
        // 3rd cycle: VIBRATE -> RING
        assertEquals(RingerMode.RING, cycleUseCase().getOrNull())
    }

    @Test
    fun testCycleFailsWhenRepositoryThrows() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(
            initialMode = RingerMode.RING,
            shouldFailSetMode = true
        )
        val cycleUseCase = CycleRingerModeUseCase(fakeRepo)

        val result = cycleUseCase()

        assertTrue(result.isFailure)
        assertEquals("Hardware audio service error", result.exceptionOrNull()?.message)
    }
}

