package name.dmitrym.ags.client

import android.app.Activity
import android.content.{ComponentName, Intent, Context}
import android.support.v4.content.WakefulBroadcastReceiver
import android.util.Log

class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
  override def onReceive(context: Context, intent: Intent): Unit = {
    Log.d("MainActivity", "onReceive in GcmBroadcastReceiver")
    val comp = new ComponentName(context.getPackageName, classOf[GcmIntentService].getName)
    WakefulBroadcastReceiver.startWakefulService(context, intent.setComponent(comp))
    setResultCode(Activity.RESULT_OK)
  }
}
