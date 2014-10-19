package name.dmitrym.ags.client

import java.util.concurrent.atomic.AtomicInteger

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, TextView}
import com.google.android.gms.common.{ConnectionResult, GooglePlayServicesUtil}
import com.google.android.gms.gcm.GoogleCloudMessaging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class MainActivity extends Activity {
  private[this] val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
  private[this] val LOG_TAG = classOf[MainActivity].getSimpleName

  private[this] val PROPERTY_REG_ID = "registration_id"
  private[this] val PROPERTY_APP_VERSION = "app_version"
  private[this] val SENDER_ID = "825512148130"

  private[this] val msgId = new AtomicInteger()

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
    if(checkPlayServices()){
      val btn = findViewById(R.id.btn_ping).asInstanceOf[Button]
      btn.setOnClickListener(new OnClickListener {
        override def onClick(v: View): Unit = {
          Future {
            val bundle = new Bundle()
            bundle.putString("my_message", "ping")
            bundle.putString("my_action", "name.dmitrym.ags.client.ECHO_NOW")
            val gcm = GoogleCloudMessaging.getInstance(getApplicationContext)
            val id = Integer.toString(msgId.incrementAndGet())
            gcm.send(SENDER_ID+"@gcm.googleapis.com", id , bundle)
            runOnUiThread(new Runnable(){
              override def run(): Unit = {
                val tv = findViewById(R.id.tv_log).asInstanceOf[TextView]
                tv.setText(tv.getText + "\n" + "Ping sent")
              }
            })
          }
        }
      })
      val regid = getRegistrationId(getApplicationContext)
      if(regid.isEmpty) {
        registerInBackground()
      } else {
        val tv = findViewById(R.id.tv_log).asInstanceOf[TextView]
        tv.setText("App already registered with " + regid)
      }
    } else {
      Log.e(LOG_TAG, "No valid Google Play Services APK found.")
    }
  }

  override def onResume(): Unit = {
    super.onResume()
    checkPlayServices()
  }

  private[this] def checkPlayServices(): Boolean = {
    val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
    if(resultCode != ConnectionResult.SUCCESS) {
      if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
        GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show()
      } else {
        Log.e(LOG_TAG, "Device is not supported")
        finish()
      }
      false
    } else true
  }

  private[this] def getRegistrationId(context: Context) = {
    val prefs = getSharedPreferences(classOf[MainActivity].getSimpleName, Context.MODE_PRIVATE)
    val registrationId = prefs.getString(PROPERTY_REG_ID, "")
    if(registrationId.isEmpty) {
      Log.d(LOG_TAG, "Registration not found")
      ""
    } else {
      val registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Int.MinValue)
      val currentVersion = getAppVersion(context)
      if(registeredVersion != currentVersion) {
        Log.d(LOG_TAG, "App version changed")
        ""
      } else registrationId
    }
  }

  private[this] def getAppVersion(context: Context) = {
    val packageInfo = context.getPackageManager.getPackageInfo(context.getPackageName, 0)
    packageInfo.versionCode
  }

  private[this] def registerInBackground():Unit = {
    Future {
      val gcm = GoogleCloudMessaging.getInstance(getApplicationContext)
      val regid = gcm.register(SENDER_ID)
      Log.d(LOG_TAG, "Device registered, registration ID=" + regid)
      runOnUiThread(new Runnable(){
        override def run(): Unit = {
          val tv = findViewById(R.id.tv_log).asInstanceOf[TextView]
          tv.setText(tv.getText + "\n" + "Device registered, registration ID=" + regid)
        }
      })
      regid
    } onComplete {
      case Success(regid) => sendRegistrationIdToBackend(regid) onComplete {
        case Success(_) => storeRegistrationId(regid)
        case Failure(t) => Log.e(LOG_TAG, t.getMessage, t)
      }
      case Failure(t) => Log.e(LOG_TAG, t.getMessage, t)
    }
  }

  private[this] def sendRegistrationIdToBackend(regid:String) = {
    Future {
      // do nothing
    }
  }

  private[this] def storeRegistrationId(regid: String): Unit = {
    val prefs = getSharedPreferences(classOf[MainActivity].getSimpleName, Context.MODE_PRIVATE)
    val appVersion = getAppVersion(getApplicationContext)
    Log.d(LOG_TAG, "Saving regId on app version: " + appVersion)
    prefs.edit.putString(PROPERTY_REG_ID, regid).putInt(PROPERTY_APP_VERSION, appVersion).commit()
  }
}
