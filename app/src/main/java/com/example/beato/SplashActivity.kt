package com.example.beato

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.beatmakingapp.R

class SplashActivity : AppCompatActivity() {
    var permissionsString = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_acivity)
        if(!hasPermissions(this@SplashActivity,*permissionsString))
        {
            ActivityCompat.requestPermissions(this@SplashActivity,permissionsString,24)}
        else
            Handler().postDelayed({
                var a= Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(a)
                this.finish()
            },3000)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode)
        {24->{if(grantResults.isNotEmpty()&& grantResults[0]== PackageManager.PERMISSION_GRANTED
            && grantResults[1]== PackageManager.PERMISSION_GRANTED) {
            Handler().postDelayed({
                var a=Intent(this@SplashActivity,
                    MainActivity::class.java)
                startActivity(a)
                this.finish()
            },2000)}
        else
        {
            Toast.makeText(this@SplashActivity,"Please Grant All Permissions To Continue", Toast.LENGTH_SHORT).show()
            this.finish()}
        }
            else ->{
                Toast.makeText( this@SplashActivity,"Something Went Wrong", Toast.LENGTH_SHORT).show()
                this.finish()}
        }

    }
    fun hasPermissions(context: Context, vararg permissions: String):Boolean {
        var hasAllPermission= true
        for(permission in permissions)
        {val res=context.checkCallingOrSelfPermission(permission)
            if(res!= PackageManager.PERMISSION_GRANTED)
            {hasAllPermission=false}
        }
        return hasAllPermission
    }
}
