package com.aftershoot.declutter.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.aftershoot.declutter.R
import com.aftershoot.declutter.ui.fragments.BadImageFragment
import com.aftershoot.declutter.ui.fragments.GoodImageFragment
import kotlinx.android.synthetic.main.activity_result.*


class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        supportActionBar?.elevation = 0F
        val pagerAdapter = PagerAdapter(
                supportFragmentManager,
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )

        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        viewPager.setPagingEnabled(false)

    }

    inner class PagerAdapter(fm: FragmentManager, behavior: Int) :
            FragmentPagerAdapter(fm, behavior) {

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Good Photos"
                1 -> "Bad Photos"
                else -> "Undefined"
            }
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> GoodImageFragment()
                1 -> BadImageFragment()
                else -> GoodImageFragment()
            }
        }

        override fun getCount() = 2
    }

}