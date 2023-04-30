package com.digitaltalent.permissionhandlerexample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.digitaltalent.permissionhandler.PermissionHandler
import com.digitaltalent.permissionhandlerexample.fragment.AccountFragment
import com.digitaltalent.permissionhandlerexample.fragment.HomeFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView


const val TAG = "MyMain"
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val frame = findViewById<FrameLayout>(R.id.myFrame)
        val nav = findViewById<BottomNavigationView>(R.id.myNav)
        val homeFragment = HomeFragment()
        val accountFragment = AccountFragment()
        setCurrentFragment(homeFragment)
        nav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.btnHome -> setCurrentFragment(homeFragment)
                R.id.btnAccount -> setCurrentFragment(accountFragment)
            }
            return@setOnItemSelectedListener true
        }

    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.myFrame,fragment)
            commit()
        }
}