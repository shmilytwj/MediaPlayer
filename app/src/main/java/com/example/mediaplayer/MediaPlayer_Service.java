package com.example.mediaplayer;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.zip.Inflater;

public class MediaPlayer_Service extends Service {
   // MediaPlayer mediaPlayer;
    LocalBinder localBinder = new LocalBinder();


    public class LocalBinder extends Binder{

        //当前播放音乐
        String current_music = null;
        String current_musicName="letter";
        MediaPlayer mediaPlayer = new MediaPlayer();

        MediaPlayer_Service getMediaPlayer_service(){
            return MediaPlayer_Service.this;
        }

        public void setCurrent_music(String current_music){
            this.current_music = current_music;
        }

        public String getCurrent_music() {
            return current_music;
        }

        public void setCurrent_musicName(String current_musicName) {
            this.current_musicName = current_musicName;
        }

        public String getCurrent_musicName() {
            return current_musicName;
        }

        public void play(){
            mediaPlayer.start();
        }
        public void pause(){
            mediaPlayer.pause();
        }
        //获取文件总长度
        public long getMusicDuration(){
            return mediaPlayer.getDuration();
        }
        //获取当前进度
        public long getPosition(){
            return mediaPlayer.getCurrentPosition();
        }

        //重新设置播放进度
        public void setPosition(int position){
            mediaPlayer.seekTo(position);
        }
        public boolean getStation(){
            return mediaPlayer.isPlaying();
        }

    }


    @Override
    public void onCreate(){
        super.onCreate();



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        LayoutInflater.from()
        //先判断当前音乐路径是否为空，若为空设置默认音乐  播放路径中的音乐
        if(localBinder.current_music == null){
            localBinder.mediaPlayer.pause();
            switch(localBinder.current_musicName){
                case "letter":
                    localBinder.mediaPlayer = MediaPlayer.create(this, R.raw.letter);
                    break;
                case "Time" :
                    localBinder.mediaPlayer = MediaPlayer.create(this, R.raw.time);
                    localBinder.play();
                    break;
                case "would you miss me":
                    localBinder.mediaPlayer = MediaPlayer.create(this, R.raw.wouldyoumissme);
                    localBinder.play();
                    break;
            }


        }else{
            localBinder.mediaPlayer.pause();
            localBinder.mediaPlayer = MediaPlayer.create(this, Uri.parse(localBinder.current_music));
            localBinder.play();

        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return localBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        if(localBinder.mediaPlayer.isPlaying()){

            localBinder.mediaPlayer.pause();
        }
        localBinder.mediaPlayer.release();
        super.onDestroy();
    }

}
