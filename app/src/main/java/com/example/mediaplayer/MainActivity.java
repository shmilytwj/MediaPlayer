package com.example.mediaplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener,ServiceConnection {

    FloatingActionButton floatingActionButton_play;
    FloatingActionButton floatingActionButton_lastSong;
    FloatingActionButton floatingActionButton_nextSong;
    TextView textView_currentTime;
    TextView textView_totalLength;
    TextView textView_songTitle;
    Switch switch_randomPlay;
    SeekBar seekBar;
// 定时器
    public Timer timer;

//互斥变量，判断是否人为出发进度条
    boolean isSeekbarChangeing;

//    MediaPlayer mediaPlayer;
//    Control_Song control_song;
//调用服务
    MediaPlayer_Service.LocalBinder mediaPlayer_localBinder;
//    MediaPlayer_Service mediaPlayer_service;
   boolean  isRandom=false;
//播放状态 用于切换按钮动作 及判断使用服务的相关方法 主要有play 和pause
   boolean station = false;
    Intent intent ;

    ListView ListView_songList;
    SimpleAdapter adapter;
    //存储音乐信息
    List<Map<String, Object>> list = new ArrayList<>() ;
    List<Map<String, Object>> song = new ArrayList<>() ;
    //共享数据
    ContentResolver contentResolver ;

    final int REQUEST_PERMISSION=0;
    //绑定服务需要用到改方法

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView_songList = findViewById(R.id.listView_songList);
        floatingActionButton_play = findViewById(R.id.floatingActionButton_play);
        floatingActionButton_nextSong = findViewById(R.id.floatingActionButton_nextSong);
        floatingActionButton_lastSong = findViewById(R.id.floatingActionButton_lastSong);
        textView_songTitle = findViewById(R.id.textView_songTitle);
        switch_randomPlay = findViewById(R.id.switch_randomPlay);
        seekBar = findViewById(R.id.seekbar);
        textView_currentTime = findViewById(R.id.textView_currentTime);
        textView_totalLength = findViewById(R.id.textView_totalLength);

        floatingActionButton_play.setOnClickListener(this);
        floatingActionButton_nextSong.setOnClickListener(this);
        floatingActionButton_lastSong.setOnClickListener(this);
        switch_randomPlay.setOnCheckedChangeListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        intent = new Intent(this, MediaPlayer_Service.class);
        bindService(intent, this, BIND_AUTO_CREATE);

        contentResolver = getApplication().getContentResolver();
        Map<String, Object> map = new HashMap<>();
        map.put("id",0);
        map.put("artist","Cat naps" );
        map.put("title", "letter");
        map.put("path", null);
        getMusicData();
        setShow(ListView_songList,list);
        registerForContextMenu(ListView_songList);
        startService(intent);


        ListView_songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title =  ((TextView)view.findViewById(R.id.tit)).getText().toString();
                String path=new String();
                String  Id=  ((TextView)view.findViewById(R.id.id)).getText().toString();
                for(int i=0;i<list.size();i++){
                    if(list.get(i).containsValue(Integer.parseInt(Id))){
                        path=String.valueOf(list.get(i).get("path"));
                    }
                }
//                Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
//                System.out.println("-------------------------------------------------" + title);
//                //查询符合条件的文件
//                Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.TITLE + "=?", new String[]{title},null);
//                while(cursor.moveToNext()){   //moveToNext 初始位置为-1。
//                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
//                }
                if(path!="null")
                    mediaPlayer_localBinder.setCurrent_music(path);
                else
                    mediaPlayer_localBinder.setCurrent_music(null);
                mediaPlayer_localBinder.setCurrent_musicName(title);
                startService(intent);
                if(mediaPlayer_localBinder.getStation()){
                    floatingActionButton_play.setImageResource(R.drawable.ic_media_play);
                    mediaPlayer_localBinder.pause();
                    station = false;
                }
                else {
                    floatingActionButton_play.setImageResource(R.drawable.ic_media_pause);
                    mediaPlayer_localBinder.play();
                    station = true;

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(!isSeekbarChangeing){
                                seekBar.setProgress((int) mediaPlayer_localBinder.getPosition() / 1000);
                            }
                        }
                    }, 0 , 100);
                }
                textView_songTitle.setText("正在播放："+mediaPlayer_localBinder.getCurrent_musicName());
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mediaPlayer_localBinder = (MediaPlayer_Service.LocalBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mediaPlayer_localBinder = null;
    }


    @Override
    public void onClick(View v) {
        int Idex = 0;
        switch (v.getId()){
            case R.id.floatingActionButton_play:
                // 传递播放状态

                if(mediaPlayer_localBinder.getStation()){
                    floatingActionButton_play.setImageResource(R.drawable.ic_media_play);
                    mediaPlayer_localBinder.pause();
                    station = false;
                }
                else {
                    floatingActionButton_play.setImageResource(R.drawable.ic_media_pause);
                    mediaPlayer_localBinder.play();
                    station = true;

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(!isSeekbarChangeing){
                                seekBar.setProgress((int) mediaPlayer_localBinder.getPosition() / 1000);
                            }
                        }
                    }, 0 , 100);
                }
                textView_songTitle.setText("正在播放："+mediaPlayer_localBinder.getCurrent_musicName());

                break;
            case R.id.floatingActionButton_nextSong:
                for(int i=0;i<list.size();i++){
                    if(list.get(i).containsValue(mediaPlayer_localBinder.getCurrent_music())){
                        Idex=i;
                    }
                }
                if(isRandom){
                    Random rand = new Random();
                    Idex=rand.nextInt(list.size());
                }
                else{
                    if(Idex+1>=list.size())
                        Idex=0;
                    else
                        Idex=Idex+1;
                }
                if(String.valueOf(list.get(Idex).get("path"))!="null")
                    mediaPlayer_localBinder.setCurrent_music(String.valueOf(list.get(Idex).get("path")));
                else
                    mediaPlayer_localBinder.setCurrent_music(null);
                mediaPlayer_localBinder.setCurrent_musicName(String.valueOf(list.get(Idex).get("title")));
                mediaPlayer_localBinder.pause();
                startService(intent);
                textView_songTitle.setText("正在播放："+mediaPlayer_localBinder.getCurrent_musicName());
                break;
            case R.id.floatingActionButton_lastSong:
                for(int i=0;i<list.size();i++){
                    if(list.get(i).containsValue(mediaPlayer_localBinder.getCurrent_music())){
                        Idex=i;
                    }
                }
                if(isRandom){
                    Random rand = new Random();
                    Idex=rand.nextInt(list.size());
                }
                else{
                    if(Idex<=0)
                        Idex=list.size()-1;
                    else
                        Idex=Idex-1;
                }
                if(String.valueOf(list.get(Idex).get("path"))!="null")
                    mediaPlayer_localBinder.setCurrent_music(String.valueOf(list.get(Idex).get("path")));
                else
                    mediaPlayer_localBinder.setCurrent_music(null);
                mediaPlayer_localBinder.setCurrent_musicName(String.valueOf(list.get(Idex).get("title")));
                mediaPlayer_localBinder.pause();
                startService(intent);
                textView_songTitle.setText("正在播放："+mediaPlayer_localBinder.getCurrent_musicName());
            break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            isRandom=true;
            Toast.makeText(this, "开", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "关", Toast.LENGTH_SHORT).show();
        }
    }


//seekBar监听器
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //获取音乐总时长
        int song_timeLength = (int) mediaPlayer_localBinder.getMusicDuration() / 1000;
        //获取当前播放位置
        int position = (int) mediaPlayer_localBinder.getPosition() / 1000;
        //开始时间
        int total_min = song_timeLength / 60;
        int total_sec = song_timeLength % 60;
        int current_min = position / 60;
        int current_sec = position % 60;

        seekBar.setMax(song_timeLength);

        textView_totalLength.setText(total_min + ":" + total_sec);
        textView_currentTime.setText(current_min + ":" + current_sec);


    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeekbarChangeing = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSeekbarChangeing = false;
        mediaPlayer_localBinder.setPosition(seekBar.getProgress() * 1000);

        int min = (int) ((mediaPlayer_localBinder.getPosition() / 1000) / 60);
        int sec = (int) ((mediaPlayer_localBinder.getPosition() / 1000) % 60);
        textView_currentTime.setText(min + ":" + sec);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id) {
            case R.id.addsong:
                addDialog();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void addDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View viewDialog = LayoutInflater.from(MainActivity.this).inflate(R.layout.adddialog, null, false);
        final ListView songList=(ListView)viewDialog.findViewById(R.id.songList);
        Map<String, Object> map = new HashMap<>();
        int id=list.size()-1;
        id=id+1;
        map.put("id",id);
        map.put("artist","Cat naps" );
        map.put("title", "Time");
        map.put("path", null);
        song.add(map);
        Map<String, Object> map1 = new HashMap<>();
        id=id+1;
        map1.put("id",id);
        map1.put("artist","Cat naps" );
        map1.put("title", "would you miss me");
        map1.put("path", null);
        song.add(map1);
        setShow(songList,song);
        builder.setTitle("添加歌曲").setView(viewDialog).setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                song.clear();
                setShow(ListView_songList,list);
            }
        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                song.clear();
            }
        });
        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String Id =  ((TextView)view.findViewById(R.id.id)).getText().toString();

                for(int i=0;i<song.size();i++)
                     if(song.get(i).containsValue(Integer.parseInt(Id))){
                         Map<String, Object> map = new HashMap<>();
                         map.put("id",song.get(i).get("id"));
                         map.put("artist",song.get(i).get("artist") );
                         map.put("title", song.get(i).get("title"));
                         map.put("path", null);
                         list.add(map);
                     }

            }
        });
        builder.create().show();

    }


    public void getMusicData(){
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,null);
       int id=0;
        while(cursor.moveToNext()){
            Map<String, Object> map = new HashMap<>();
            //获取路径
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            //获取名字
            String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String musicalbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));

//            System.out.println("------------------------------------------" + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            //可继续添加信息  暂时做测试
            id=id+1;
            map.put("id",id);
            map.put("artist", artist);
            map.put("title", song);
            map.put("path", path);
            list.add(map);

        }

    }
    public void setShow(ListView ListView,List<Map<String, Object>> list){
        adapter = new SimpleAdapter(this, list, R.layout.item,new String[]{"id","title", "artist"}, new int[]{R.id.id,R.id.tit, R.id.editor});
        ListView.setAdapter(adapter);

    }
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        this.getMenuInflater().inflate(R.menu.contextmenu, menu);
    }
    public boolean onContextItemSelected(MenuItem item) {
        TextView Id = null;

        AdapterView.AdapterContextMenuInfo info = null;
        View itemView = null;

        switch (item.getItemId()) {
            case R.id.delete:

                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                itemView = info.targetView;
                Id = (TextView) itemView.findViewById(R.id.id);
                if (Id != null) {
                    String strId = Id.getText().toString();
                    System.out.println(strId);
                    onDeleteDialog(strId);
                }
                break;
        }
        return true;
    }

    private void onDeleteDialog(String strId) {
        for(int i=0;i<list.size();i++)
            if(list.get(i).containsValue(Integer.parseInt(strId)))
                list.remove(i);
        setShow(ListView_songList,list);
    }
}