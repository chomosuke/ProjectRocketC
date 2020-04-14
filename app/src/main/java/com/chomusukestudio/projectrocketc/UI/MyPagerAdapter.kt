package com.chomusukestudio.projectrocketc.UI

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import com.chomusukestudio.projectrocketc.R

class MyPagerAdapter(private val mainActivity: MainActivity) : androidx.viewpager.widget.PagerAdapter() {
    override fun instantiateItem(container :ViewGroup, position: Int): View {
        Log.v("tutorial instItem", container.toString())

        val imageView = ImageView(mainActivity)
        val rLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        imageView.layoutParams = rLayoutParams
        rLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)


        imageView.setImageResource(when (position) {
            0 -> R.drawable.tutorial1
            1 -> R.drawable.tutorial2
            2 -> R.drawable.tutorial3
            else -> throw IllegalArgumentException()
        })



        val relativeLayout = RelativeLayout(mainActivity)
        relativeLayout.addView(imageView)

        if (position == count - 1) {
            // last tutorial page give an option to quit

//            val button = mainActivity.findViewById<Button>(R.id.finishTutorialButton)
//            val rLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
//                    RelativeLayout.LayoutParams.WRAP_CONTENT)
//            button.layoutParams = rLayoutParams
//            rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
//            rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)

            relativeLayout.addView(generateQuitTutorialButton())
        }

        container.addView(relativeLayout)
        return relativeLayout
    }

    private fun generateQuitTutorialButton(): Button {
        val button = Button(mainActivity)
        button.setOnClickListener { mainActivity.finishTutorial() }

        button.text = mainActivity.getString(R.string.finish_tutorial)

        val rLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        button.layoutParams = rLayoutParams
        rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)

        val scale: Float = mainActivity.resources.displayMetrics.density
        val dpAsPixels = (24 * scale + 0.5f).toInt()
        rLayoutParams.setMargins(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels)

        return button
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "1"
            1 -> "2"
            2 -> "3"
            else -> ""
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        Log.v("tutorial destroyItem", container.toString())
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(p0: View, p1: Any) = p0 == p1

    override fun getCount() = 3
}
