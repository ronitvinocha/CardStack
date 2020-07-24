package com.feelr.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.feelr.myapplication.ui.main.MainFragment
import kotlinx.android.synthetic.main.main_activity.*
import java.util.*
import kotlin.properties.Delegates

private const val NUM_PAGES = 5
class MainActivity : AppCompatActivity() {
    private lateinit var viewpager: ViewPager2
     var position by Delegates.notNull<Int>()
    var starttime by Delegates.notNull<Long>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
         var w = getWindow() // in Activity's onCreate() for instance
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                    .replace(R.id., MainFragment.newInstance())
//                    .commitNow()
//        }
        viewpager=pager
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewpager.adapter = pagerAdapter
        viewpager.setPageTransformer(Slidepagetransformer())
        viewpager.offscreenPageLimit=2
        position=0
        viewpager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
              override fun onPageScrollStateChanged(state: Int) {
                  println("onpagescrollstatechanged"+state)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                println("onpagescrolled"+position)
            }

            override fun onPageSelected(position: Int) {
                println("onpageselected"+position)
                if(this@MainActivity.position!=position)
                {
                    if((Date().time-starttime)/1000-60>0)
                    {
                          Toast.makeText(this@MainActivity, (((Date().time-starttime)/60000)).toString()+" min",Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                          Toast.makeText(this@MainActivity, ((Date().time-starttime)/1000).toString()+" sec",Toast.LENGTH_SHORT).show()
                    }
                }
                this@MainActivity.position=position
                this@MainActivity.starttime=Date().time
            }
        })
//        for(i in 0..viewpager.adapter!!.itemCount-1)
//        {
//            setcardsstackoninitiate(i,viewpager.get(i))
//        }
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment = MainFragment()
    }
    override fun onBackPressed() {
        if (viewpager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewpager.currentItem = viewpager.currentItem - 1
        }
    }
}