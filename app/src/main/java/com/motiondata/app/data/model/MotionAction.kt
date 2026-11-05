package com.motiondata.app.data.model

/**
 * Predefined robotic and manipulation actions with standardized slugs for dataset labeling.
 */
data class MotionAction(
    val id: String,
    val displayName: String,
    val description: String,
    val isCustom: Boolean = false
) {
    companion object {
        val PRESETS = listOf(
            MotionAction("pick_object", "Pick Object", "Grasping and lifting a target object from a surface"),
            MotionAction("place_object", "Place Object", "Lowering and releasing an object onto a surface"),
            MotionAction("rotate_wrist", "Rotate Wrist", "Pronation/supination or rotation around the forearm axis"),
            MotionAction("reach_forward", "Reach Forward", "Translational arm extension toward a target point"),
            MotionAction("lift_object", "Lift Object", "Vertical elevation of an object against gravity"),
            MotionAction("custom", "Custom Action", "Specify your own experimental robotics action label", isCustom = true)
        )

        fun toSlug(name: String): String {
            return name.trim()
                .lowercase()
                .replace("\\s+".toRegex(), "_")
                .replace("[^a-z0-9_]".toRegex(), "")
        }
    }
}
