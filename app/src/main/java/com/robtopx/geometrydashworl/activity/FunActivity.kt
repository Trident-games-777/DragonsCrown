package com.robtopx.geometrydashworl.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.BounceInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.robtopx.geometrydashworl.R

class FunActivity : AppCompatActivity() {
    private lateinit var iv1: ImageView
    private lateinit var iv2: ImageView
    private lateinit var iv3: ImageView
    private lateinit var btnGo: Button
    private lateinit var tvCoins: TextView

    private val drawables = listOf(
        R.drawable.e1,
        R.drawable.e2,
        R.drawable.e3,
        R.drawable.e4,
        R.drawable.e5,
    )

    private var currentDrawables = mutableListOf<Int>()
    private var coins = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun)

        iv1 = findViewById(R.id.iv1)
        iv2 = findViewById(R.id.iv2)
        iv3 = findViewById(R.id.iv3)
        btnGo = findViewById(R.id.btnGo)
        tvCoins = findViewById(R.id.tvCoins)

        btnGo.setOnClickListener {
            val won = refreshDrawables()
            if (won) {
                coins += 50
                tvCoins.text = coins.toString()
            }

            iv1.setImageResource(currentDrawables[0])
            iv2.setImageResource(currentDrawables[1])
            iv3.setImageResource(currentDrawables[2])

            val animator1 = fallingAnimator(0, iv1)
            val animator2 = fallingAnimator(0, iv2)
            val animator3 = fallingAnimator(0, iv3)
            val set = AnimatorSet()

            if (won) {
                val scaleAnimator = scaleCoinAnimator()
                set.playTogether(animator1, animator2, animator3, scaleAnimator)
            } else {
                set.playTogether(animator1, animator2, animator3)
            }
            set.start()
        }
    }

    private fun refreshDrawables(): Boolean {
        currentDrawables.clear()
        repeat(3) {
            currentDrawables.add(drawables.random())
        }
        return currentDrawables[0] == currentDrawables[1] &&
                currentDrawables[1] == currentDrawables[2]
    }

    private fun fallingAnimator(startDelay: Long, view: ImageView): Animator {
        val objectAnimator = ObjectAnimator.ofFloat(view, "translationY", 0f, 500f)
        objectAnimator.duration = 500L
        objectAnimator.startDelay = startDelay
        objectAnimator.interpolator = BounceInterpolator()
        return objectAnimator
    }

    private fun scaleCoinAnimator(): Animator {
        val increaseAnimatorX = ObjectAnimator.ofFloat(tvCoins, "scaleX", 1f, 2f)
        val increaseAnimatorY = ObjectAnimator.ofFloat(tvCoins, "scaleY", 1f, 2f)
        val increaseAnimatorSet = AnimatorSet()
        increaseAnimatorSet.playTogether(increaseAnimatorX, increaseAnimatorY)
        increaseAnimatorSet.duration = 250L

        val decreaseAnimatorX = ObjectAnimator.ofFloat(tvCoins, "scaleX", 2f, 1f)
        val decreaseAnimatorY = ObjectAnimator.ofFloat(tvCoins, "scaleY", 2f, 1f)
        val decreaseAnimatorSet = AnimatorSet()
        decreaseAnimatorSet.playTogether(decreaseAnimatorX, decreaseAnimatorY)
        decreaseAnimatorSet.duration = 250

        val scaleAnimatorSet = AnimatorSet()
        scaleAnimatorSet.playSequentially(increaseAnimatorSet, decreaseAnimatorSet)
        return scaleAnimatorSet
    }
}