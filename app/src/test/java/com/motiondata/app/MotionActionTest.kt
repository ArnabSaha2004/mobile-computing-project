package com.motiondata.app

import com.motiondata.app.data.model.MotionAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionActionTest {

    @Test
    fun testSlugGeneration() {
        assertEquals("pick_object", MotionAction.toSlug("Pick Object"))
        assertEquals("rotate_wrist", MotionAction.toSlug("Rotate Wrist"))
        assertEquals("custom_task_1", MotionAction.toSlug("Custom Task #1!"))
    }

    @Test
    fun testPresetsContainRequiredActions() {
        val ids = MotionAction.PRESETS.map { it.id }
        assertTrue(ids.contains("pick_object"))
        assertTrue(ids.contains("place_object"))
        assertTrue(ids.contains("rotate_wrist"))
        assertTrue(ids.contains("reach_forward"))
        assertTrue(ids.contains("lift_object"))
        assertTrue(ids.contains("custom"))
    }
}
