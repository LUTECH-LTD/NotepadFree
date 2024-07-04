package lutech.intern.noteapp.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import lutech.intern.noteapp.R

object DrawableUtils {
    fun createGradientDrawable(context: Context, colorHex: String): GradientDrawable {
        val startColor = blendColorWithWhite(Color.parseColor(colorHex), 0.2f)
        val endColor = Color.parseColor(colorHex)
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(startColor, endColor)
        ).apply {
            this.cornerRadius = context.resources.getDimension(R.dimen.dp_6)
            setStroke(
                context.resources.getDimensionPixelSize(R.dimen.dp_1),
                ContextCompat.getColor(context, R.color.color_brown)
            )
        }
        return gradientDrawable
    }

    fun createSolidDrawable(context: Context, colorHex: String): GradientDrawable {
        val color = blendColorWithWhite(Color.parseColor(colorHex), 0.3f)
        val gradientDrawable = GradientDrawable().apply {
            setColor(color) // Set solid color
            cornerRadius = context.resources.getDimension(R.dimen.dp_6)
            setStroke(
                context.resources.getDimensionPixelSize(R.dimen.dp_1),
                ContextCompat.getColor(context, R.color.color_brown)
            )
        }
        return gradientDrawable
    }


    fun blendColorWithWhite(color: Int, ratio: Float): Int {
        val inverseRatio = 1 - ratio
        val r = Color.red(color) * inverseRatio + 255 * ratio
        val g = Color.green(color) * inverseRatio + 255 * ratio
        val b = Color.blue(color) * inverseRatio + 255 * ratio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    fun darkenColor(color: Int, ratio: Float): Int {
        val r = Color.red(color) * (1 - ratio)
        val g = Color.green(color) * (1 - ratio)
        val b = Color.blue(color) * (1 - ratio)
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }
}