package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CycleRingerModeUseCaseTest {

    @Test
    fun testCycleFromRingToVibrate() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(initialMode = RingerMode.RING)
        val cycleUseCase = CycleRingerModeUseCase(fakeRepo)

        val result = cycleUseCase()

        assertTrue(result.isSuccess)
        assertEquals(RingerMode.VIBRATE, result.getOrNull())
        assertEquals(RingerMode.VIBRATE, fakeRepo.getCurrentRingerMode())
    }

    @Test
    fun testCycleFromVibrateToSilent() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(initialMode = RingerMode.VIBRATE)
        val cycleUseCase = CycleRingerModeUseCase(fakeRepo)

        val result = cycleUseCase()

        assertTrue(result.isSuccess)
        assertEquals(RingerMode.SILENT, result.getOrNull())
        assertEquals(RingerMode.SILENT, fakeRepo.getCurrentRingerMode())
    }

    @Test
    fun testCycleFromSilentToRing() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(initialMode = RingerMode.SILENT)
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

        // 1st cycle: RING -> VIBRATE
        assertEquals(RingerMode.VIBRATE, cycleUseCase().getOrNull())
        // 2nd cycle: VIBRATE -> SILENT
        assertEquals(RingerMode.SILENT, cycleUseCase().getOrNull())
        // 3rd cycle: SILENT -> RING
        assertEquals(RingerMode.RING, cycleUseCase().getOrNull())
    }

    @Test
    fun testCycleMode2SequenceRingSilentVibrateRing() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(initialMode = RingerMode.RING)
        val cycleUseCase = CycleRingerModeUseCase(fakeRepo)

        // 1st cycle: RING -> SILENT
        assertEquals(RingerMode.SILENT, cycleUseCase(RingerCycleOrder.RING_SILENT_VIBRATE).getOrNull())
        // 2nd cycle: SILENT -> VIBRATE
        assertEquals(RingerMode.VIBRATE, cycleUseCase(RingerCycleOrder.RING_SILENT_VIBRATE).getOrNull())
        // 3rd cycle: VIBRATE -> RING
        assertEquals(RingerMode.RING, cycleUseCase(RingerCycleOrder.RING_SILENT_VIBRATE).getOrNull())
    }

    @Test
    fun testCycleMode2SkipsSilentWhenNotificationPolicyMissing() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(
            initialMode = RingerMode.RING,
            hasNotificationPolicyAccessGranted = false
        )
        val cycleUseCase = CycleRingerModeUseCase(fakeRepo)

        // When missing DND policy access, RING falls back to VIBRATE instead of SILENT
        val result = cycleUseCase(RingerCycleOrder.RING_SILENT_VIBRATE)
        assertTrue(result.isSuccess)
        assertEquals(RingerMode.VIBRATE, result.getOrNull())
    }

    @Test
    fun testCycleSkipsSilentWhenNotificationPolicyMissing() = runBlocking {
        val fakeRepo = FakeRingerModeRepository(
            initialMode = RingerMode.VIBRATE,
            hasNotificationPolicyAccessGranted = false
        )
        val cycleUseCase = CycleRingerModeUseCase(fakeRepo)

        val result = cycleUseCase()

        assertTrue(result.isSuccess)
        // When notification policy is missing, VIBRATE cycles back to RING without crashing
        assertEquals(RingerMode.RING, result.getOrNull())
        assertEquals(RingerMode.RING, fakeRepo.getCurrentRingerMode())
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

