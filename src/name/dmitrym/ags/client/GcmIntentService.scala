package name.dmitrym.ags.client

import android.app.{PendingIntent, NotificationManager, IntentService}
import android.content.{Context, Intent}
import android.support.v4.app.NotificationCompat.{BigTextStyle, Builder}
import android.support.v4.content.WakefulBroadcastReceiver
import android.util.Log
import com.google.android.gms.gcm.GoogleCloudMessaging

class GcmIntentService extends IntentService("GcmIntentService") {
  private[this] val NOTIFICATION_ID = 1
  override def onHandleIntent(intent: Intent): Unit = {
    Log.d("MainActivity", "Handle intent")
    val extras = intent.getExtras
    val gcm = GoogleCloudMessaging.getInstance(this)
    val messageType = gcm.getMessageType(intent)
    Log.d("MainActivity", "Message type: " + messageType)
    Log.d("MainActivity", "Has extras: " + !extras.isEmpty)
    if(!extras.isEmpty) {
      if(GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
        sendNotification("Send error: " + extras.toString)
      } else if(GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
        sendNotification("Deleted messages on server: " + extras.toString)
      } else if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
        sendNotification("Received: " + extras.toString)
      }
    }
    WakefulBroadcastReceiver.completeWakefulIntent(intent)
  }

  private[this] def sendNotification(msg: String): Unit = {
    Log.d("MainActivity", "Send notification: " + msg)
    val nm = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    val contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, classOf[MainActivity]), 0)
    val builder = new Builder(this)
    builder.setSmallIcon(R.drawable.ic_launcher).setContentTitle("GCM Notification")
    .setStyle(new BigTextStyle().bigText(msg)).setContentText(msg).setContentIntent(contentIntent)
    nm.notify(NOTIFICATION_ID, builder.build())
  }
}
