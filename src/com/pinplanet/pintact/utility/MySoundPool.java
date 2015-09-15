package com.pinplanet.pintact.utility;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import com.pinplanet.pintact.MainActivity;
import com.pinplanet.pintact.R;

/**
 * Created by Avinash on 14/2/15.
 */
public class MySoundPool {
    private static SoundPool sp;
    public static int MESSAGE_SOUND_ID;

    private MySoundPool(){

    }

    @SuppressWarnings("deprecation")
    public static void initSoundPool(Context context){
        if(true)
        {
            sp = new SoundPool(6, AudioManager.STREAM_MUSIC, 2);

            //below is the new way, supported in min api 21
           /* SoundPool.Builder soundPoolBuilder = new SoundPool.Builder();
            AudioAttributes attributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build();

            sp = soundPoolBuilder.setMaxStreams(2).setAudioAttributes(attributes).build();*/
            MESSAGE_SOUND_ID = sp.load(context, R.raw.message, 1);
        }
        else{
            sp = null;
        }
    }

    public static void playSound(int soundID){
        if(sp != null)
        {
            sp.play(soundID, 1, 1, 0, 0, 1);
        }
    }
}
