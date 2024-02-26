/**
 * Credit to https://github.com/Sumandora/tarasande/blob/7d0a467c4f3abe151ab23ab2dce1b4107ffa3e1f/src/main/kotlin/su/mandora/tarasande/util/render/animation/TimeAnimator.kt
 * Licensed under the GNU General Public License v3.0
 */

package su.mandora.tarasande.util.render.animation

class TimeAnimator(var animationLength: Long) {

    var reversed = false
    private var baseTime = 0L

    @JvmName("setReversedAndUpdate")
    fun setReversed(reversed: Boolean) {
        val remainingTime = (animationLength - (System.currentTimeMillis() - baseTime)).coerceAtLeast(0)
        baseTime = System.currentTimeMillis() - remainingTime
        this.reversed = reversed
    }

    fun getProgress(): Double {
        val delta = System.currentTimeMillis() - baseTime
        var animation = (delta / animationLength.toDouble()).coerceAtLeast(0.0).coerceAtMost(1.0)
        if (reversed) animation = 1.0 - animation
        return animation
    }

    fun setProgress(progress: Double) {
        baseTime = System.currentTimeMillis() - (animationLength * progress).toLong()
    }

    fun isCompleted() = System.currentTimeMillis() - baseTime > animationLength
}
