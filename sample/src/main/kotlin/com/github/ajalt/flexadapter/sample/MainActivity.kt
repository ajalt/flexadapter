package com.github.ajalt.flexadapter.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button_kotlin_sample.setOnClickListener { startActivity(Intent(this, SampleActivity::class.java)) }
        button_java_sample.setOnClickListener { startActivity(Intent(this, JavaSampleActivity::class.java)) }
        button_view_pager.setOnClickListener { startActivity(Intent(this, ViewPagerActivity::class.java)) }
        button_stable_ids.setOnClickListener { startActivity(Intent(this, StableIdsActivity::class.java)) }
    }
}
