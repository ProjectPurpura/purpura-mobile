package com.purpura.app.configuration;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.purpura.app.R;
import com.purpura.app.ui.account.AccountFragment;
import com.purpura.app.ui.screens.MainActivity;

    public class Notifications {

        private static final String CHANNEL_ID = "canal_id";

        public void chamar(Activity activity, Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Notificações Importantes",
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Canal para notificações importantes");
                channel.enableVibration(true);
                channel.setShowBadge(true);
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Recuperação de senha")
                    .setContentText("O e-mail de recuperação da sua senha foi enviado!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                            1);
                    return;
                }
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(AccountFragment.NOTIFICATION_ID, builder.build());
        }
    }
