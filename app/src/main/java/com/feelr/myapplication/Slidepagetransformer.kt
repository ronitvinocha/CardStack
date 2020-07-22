package com.feelr.myapplication
import android.graphics.drawable.TransitionDrawable
import android.opengl.Visibility
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.feelr.myapplication.R

private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f
class Slidepagetransformer :ViewPager2.PageTransformer {
    var transition:TransitionDrawable?=null
    override fun transformPage(view: View, position: Float) {
        println("😄"+position)
        if(position==0f)
        {
            var childview=view.findViewById<ConstraintLayout>(R.id.main)
                transition= childview.background as TransitionDrawable?
                transition?.startTransition(200)
        }
        else
        {
            var childview=view.findViewById<ConstraintLayout>(R.id.main)
                transition= childview.background as TransitionDrawable?
                transition?.resetTransition()
        }
        if (position >= 0) {
            view.setScaleX(0.9f - 0.1f * position);
            view.setScaleY(0.9f);
            view.setTranslationX(-view.getWidth() * position);
            view.setTranslationY(30 * position);
            if(position==0.0f)
            {
                view.translationZ=1f

            }
            else
            {
                view.translationZ=0f
//                var childview=view.findViewById<ConstraintLayout>(R.id.main)
//                transition= childview.background as TransitionDrawable?
//                transition?.reverseTransition(200)
            }
        }
    }
}