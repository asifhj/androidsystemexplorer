package com.ase.explorer;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FindImage extends ListActivity {

    private static final int EXIT = 0;
    private static final int STOP = 1;
    private static final String DIRECTORY = "/sdcard/";
    private MediaPlayer mp = new MediaPlayer();
    List<String> Ringtones = new ArrayList<String>();
    Boolean hasErrors = false;
    int currentPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView lv = getListView();
        File ringtones_directory = new File(DIRECTORY);
        if (!ringtones_directory.exists()) {
            AlertDialog.Builder ad = new AlertDialog.Builder
            (
                    FindImage.this);
            ad.setTitle("Directory Not Found");
            ad.setMessage("Sorry! The ringtones directory doesn't exist.");
            ad.setPositiveButton("OK", null);
            ad.show();
            hasErrors = true;
        	}
       
        if (!ringtones_directory.canRead()) 
        {
            AlertDialog.Builder ad = new AlertDialog.Builder
            (
                    FindImage.this);
            ad.setTitle("Permissions");
            ad.setMessage("Sorry! You don't have permission to list the files in that folder");
            ad.setPositiveButton("OK", null);
            ad.show();
            hasErrors = true;
        }
        else 
        {
            Ringtones = FindFiles(false);

            if (Ringtones.size() < 1) 
            {
                AlertDialog.Builder ad = new AlertDialog.Builder(
                        FindImage.this);
                ad.setTitle("Permissions");
                ad.setMessage("Sorry! No ringtones exists in " + DIRECTORY + ".");
                ad.setPositiveButton("OK", null);
                ad.show();
                Log.e("Ringtones", "No ringtones were found.");
                hasErrors = true;
            }
        }      
       
        if (!hasErrors) {
            setListAdapter(new ArrayAdapter<String>(FindImage.this, R.layout.list_item,
                    Ringtones));
           
            lv.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> aView, View v,
                        int position, long id) {
                    currentPosition = position;
                    playRingtone(DIRECTORY+Ringtones.get(position));
                }
            });
        }
    }
   
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
       
        int NONE = Menu.NONE;
        if (mp.isPlaying()) {
            menu.add(NONE, STOP, 0, "Stop");
        }
        menu.add(NONE, EXIT, 1, "Exit");
        return true;
    }
   
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case STOP:
            mp.stop();
            break;
        case EXIT:
            quit();
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void playRingtone(String ringtone) {
        Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		File file1 = new File(ringtone);
		intent.setDataAndType(Uri.fromFile(file1), "image/*");
		startActivity(intent);
    }

    private List<String> FindFiles(Boolean fullPath) 
    {
        final List<String> tFileList = new ArrayList<String>();
        Resources resources = getResources();
        // array of valid audio file extensions
        String[] audioTypes = resources.getStringArray(R.array.image);
        FilenameFilter[] filter = new FilenameFilter[audioTypes.length];

        int i = 0;
        for (final String type : audioTypes) 
        {
            filter[i] = new FilenameFilter() 
            {
                public boolean accept(File dir, String name) 
                {
                    return name.endsWith("." + type);
                }
            };
            i++;
        }

        FileUtils fileUtils = new FileUtils();
        File[] allMatchingFiles = fileUtils.listFilesAsArray(new File(DIRECTORY), filter, -1);
        for (File f : allMatchingFiles) 
        {
            if (fullPath) 
            {
                tFileList.add(f.getAbsolutePath());
            }
            else 
            {
                tFileList.add(f.getName());
            }
        }
        return tFileList;
    }

    public class FileUtils {

        public void saveArray(String filename, List<String> output_field) {
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                GZIPOutputStream gzos = new GZIPOutputStream(fos);
                ObjectOutputStream out = new ObjectOutputStream(gzos);
                out.writeObject(output_field);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.getStackTrace();
            }
        }

        @SuppressWarnings("unchecked")
        public List<String> loadArray(String filename) 
        {
            try 
            {
                FileInputStream fis = new FileInputStream(filename);
                GZIPInputStream gzis = new GZIPInputStream(fis);
                ObjectInputStream in = new ObjectInputStream(gzis);
                List<String> read_field = (List<String>) in.readObject();
                in.close();
                return read_field;
            } catch (Exception e) {
                e.getStackTrace();
            }
            return null;
        }

        public File[] listFilesAsArray(File directory, FilenameFilter[] filter,int recurse) 
        {
            Collection<File> files = listFiles(directory, filter, recurse);
            File[] arr = new File[files.size()];
            return files.toArray(arr);
        }

        public Collection<File> listFiles(File directory,FilenameFilter[] filter, int recurse) 
        {
            Vector<File> files = new Vector<File>();
            File[] entries = directory.listFiles();
            if (entries != null) 
            {
                for (File entry : entries) 
                {
                    for (FilenameFilter filefilter : filter) 
                    {
                        if (filter == null || filefilter.accept(directory, entry.getName())) 
                        {
                            files.add(entry);
                            Log.v("FileUtils", "Added: "+ entry.getName());
                        }
                    }
                    if ((recurse <= -1) || (recurse > 0 && entry.isDirectory())) 
                    {
                        recurse--;
                        files.addAll(listFiles(entry, filter, recurse));
                        recurse++;
                    }
                }
            }
            return files;
        }
    }
    @Override
    public void onBackPressed() {
            super.onBackPressed();
            this.finish();
            quit();
    }

    public void quit() {
        this.finishActivity(EXIT);
        Intent intent=new Intent(FindImage.this,ExplorerActivity.class);
        startActivity(intent);
    }
}
