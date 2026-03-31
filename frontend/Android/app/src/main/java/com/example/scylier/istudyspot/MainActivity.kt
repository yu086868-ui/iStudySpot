package com.example.scylier.istudyspot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.scylier.istudyspot.fragment.HomeFragment
import com.example.scylier.istudyspot.fragment.RulesFragment
import com.example.scylier.istudyspot.fragment.MoreFragment
import com.example.scylier.istudyspot.fragment.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 获取从登录页面传递过来的token
        token = intent.getStringExtra("token") ?: ""

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment().apply {
                        arguments = Bundle().apply {
                            putString("token", token)
                        }
                    })
                    true
                }
                R.id.nav_rules -> {
                    replaceFragment(RulesFragment())
                    true
                }
                R.id.nav_more -> {
                    replaceFragment(MoreFragment().apply {
                        arguments = Bundle().apply {
                            putString("token", token)
                        }
                    })
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment().apply {
                        arguments = Bundle().apply {
                            putString("token", token)
                        }
                    })
                    true
                }
                else -> false
            }
        }

        // 默认显示首页
        replaceFragment(HomeFragment().apply {
            arguments = Bundle().apply {
                putString("token", token)
            }
        })
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}