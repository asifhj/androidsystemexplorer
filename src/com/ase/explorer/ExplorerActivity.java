package com.ase.explorer;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.FormatException;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.zip.*;

public class ExplorerActivity<ActionBar> extends ListActivity {
	
	private List<String> item = null;
	private List<String> path = null;
	private String root="/";
	private TextView myPath;
	public String filepath="",temp="",tempdes="";
	int pos=0,desdir=0,desfile=0;;
	private File f1,f2,sf,df,tf;
	private String curdir="";
	int listpos=-1,tapcount=0;
	

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) 
		{
			String searchKeywords = queryIntent.getStringExtra(SearchManager.QUERY);
			System.out.print(searchKeywords+"asif");
		}
		 myPath = (TextView)findViewById(R.id.path);
		

        getDir(root);
    }
	
	 private void getDir(String dirPath)
	    {
	    	myPath.setText("Location: " + dirPath);
	    	curdir=dirPath;
	    	item = new ArrayList<String>();
	    	path = new ArrayList<String>();
	    	
	    	File f = new File(dirPath);
	    	File[] files = f.listFiles();
	    	
	    	if(!dirPath.equals(root))
	    	{
	    		item.add(root);
	    		path.add(root);
	    		
	    		item.add("Up../");
	    		path.add(f.getParent());
	    	}
	    	
	    	for(int i=0; i < files.length; i++)
	    	{
	    			File file = files[i];
	    			path.add(file.getPath());
	    			if(file.isDirectory())
	    				item.add(file.getName() + "/");
	    			else
	    				item.add(file.getName());
	    	}
	   // 	setListAdapter(new IconicAdapter());
//	        myPath=(TextView)findViewById(R.id.path);
	    //	ArrayAdapter<String> fileList = 	new ArrayAdapter<String>(this, R.layout.row, item);
	    	//setListAdapter(fileList);
	    		setListAdapter(new IconicAdapter());
	    
	    }
	 class IconicAdapter extends ArrayAdapter 
	 {
		    @SuppressWarnings("unchecked")
			IconicAdapter() 
		    {
		      super(ExplorerActivity.this, R.layout.row, item);
		    }
		   
		    public View getView(final int position, View convertView,ViewGroup parent) 
		    {
		      LayoutInflater inflater=getLayoutInflater();
		      View row=inflater.inflate(R.layout.row, parent, false);
		      TextView label=(TextView)row.findViewById(R.id.label);
		      
		      label.setText(item.get(position));
		      
		      final ImageView icon=(ImageView)row.findViewById(R.id.icon);
		        if(item.get(position).equals("/"))
		        {
		        	icon.setImageResource(R.drawable.home);
		        }
		        else
		      if (item.get(position).endsWith("/") && !item.get(position).contains("Up../")) 
		      {
		        icon.setImageResource(R.drawable.folder);
		      }else
		    	  if(item.get(position).equals("Up../"))
		    	  {
		    		  icon.setImageResource(R.drawable.up);
		    	  }
		      else
		      if(item.get(position).contains(".mp3") ||item.get(position).contains(".ogg") || item.get(position).contains(".flac") || item.get(position).contains(".mid") || item.get(position).contains(".imy") || item.get(position).contains(".xmf") || item.get(position).contains(".mxmf") || item.get(position).contains(".rtttl") || item.get(position).contains(".rtx") || item.get(position).contains(".ota") || item.get(position).contains(".wav"))
		      {
		        icon.setImageResource(R.drawable.audio);
		      }
		      else
		     if(item.get(position).contains(".3gp") || item.get(position).contains(".mp4") || item.get(position).contains(".webm"))
		     {
		    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(curdir+"/"+item.get(position),MediaStore.Images.Thumbnails.MICRO_KIND);

		    	 //icon.setImageResource(R.drawable.video);
		    	 icon.setImageBitmap(thumb);
		     }
		     else
		    if(item.get(position).contains(".jpeg")||item.get(position).contains(".jpg") || item.get(position).contains(".png") || item.get(position).contains(".gif")|| item.get(position).contains(".bmp")||item.get(position).contains(".webp") )
			{
		    	icon.setImageResource(R.drawable.photo);
		    }
		    else
		    	icon.setImageResource(R.drawable.file);
		      return(row);
		    }
		  }
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.options_menu, menu);
	   //     menu.getItem(8).setIcon(R.drawable.details);
	        return true;
	    }

		@Override
	    public boolean onOptionsItemSelected(MenuItem mitem) 
		{
	        switch (mitem.getItemId()) 
	        {
	        	
	        	case R.id.open:
	        		final File file= new File(path.get(getSelectedItemPosition()));
	        		filepath=file.getAbsolutePath();
	        		open(file);
	        		return true;
	            case R.id.Search_Audio:
	            	searchaudio();
	                return true;
	            case R.id.Send:
	            	send();
	                return true;
	            case R.id.Search_Video:
	  	            searchvideo();
	  	            return true;
	            case R.id.Search_Image:
	            	searchimage();
  	                return true;
	            case R.id.delete:
	            	deletefile();
  	                return true;
	            case R.id.newfolder:
	            	newfolder();
  	                return true;
	            case R.id.Quit:
	            	quit();
	            	return true;
	            case R.id.copy:
	            	copy();
	            	return true;
	            case R.id.paste:
	            	
	            	if(desfile==1)
	            		pastefile();
	            		if(desdir==1)
	            		{
	            			Log.d("Des", curdir);
	            			paste(sf, new File(curdir));
	            			
	            		}
	            	
	            	getDir(curdir);
	            	return true;
	            case R.id.rename:
	            	rename();
	            	return true;
	            case R.id.aboutphone:
	            	aboutdevice();
	            	return true;
	            case R.id.aboutnetwork:
	            	aboutnetwork();
	            	return true;
	            case R.id.details:
	            	details();
	            	return true;
	         /*   case R.id.reboot:
	            		reboot();
	            		return true;*/
	            case R.id.allinfo:
            		allinfo();
            		return true;
	            case R.id.aboutbattery:
            		battery();
            		return true;
	            case R.id.aboutfirmware:
            		aboutfirmware();
            		return true;
	            default:
	                return false;
	        }
	    }
		public void aboutfirmware()
		{
			sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://197328640")));

		}
		public void battery()
		{
			 BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
			        int scale = -1;
			        int level = -1;
			        int voltage = -1;
			        int temp = -1;
			        int plug=-1;
			       int health,tech=-1;
			       
				
			        @Override
			        public void onReceive(Context context, Intent intent) {
			            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			            scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			            temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
			            voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
			            plug=intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			            tech=intent.getIntExtra(BatteryManager.EXTRA_TECHNOLOGY, -1);
			            health=intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
			       int present=intent.getIntExtra(BatteryManager.EXTRA_PRESENT, -1);
			       
			          //  Log.e("BatteryManager", "level is "+level+"/"+scale+", temp is "+temp+", voltage is "+voltage);
			           new AlertDialog.Builder(context).setIcon(R.drawable.battery).setTitle("Battery details!").setMessage("Level : "+level+"/"+scale+"\nTemp : "+temp+"\nVoltage : "+voltage+"\nTechnology : "+intent.getIntExtra(BatteryManager.EXTRA_TECHNOLOGY, -1)+"\nPlugged : "+plug+"\nHealth : "+health+"\nPresent : "+present).show();
			        }
			    };
			    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			    registerReceiver(batteryReceiver, filter);
		}
		public void allinfo()
		{
		/*	String ussdCode = "*" +Uri.encode ("#")+"*"+Uri.encode ("#")+ "4636" + Uri.encode ("#")+"*"+Uri.encode ("#")+"*";
			Intent intent = new Intent(Intent.ACTION_DIAL);    
			intent.setData(Uri.parse("tel:"+ussdCode));
			startActivity(intent);*/
			sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://4636")));


		}
		public void reboot() 
	    { 
			
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			     pm.reboot("hi");
	       /* Runtime runtime = Runtime.getRuntime(); 
	        Process proc = null; 
	        OutputStreamWriter osw = null;    
	        StringBuilder sbstdOut = new StringBuilder();   
	        StringBuilder sbstdErr = new StringBuilder(); 
	        String command="/system/bin/reboot";    
	        try { 
	            // Run Script    
	            proc = runtime.exec("su");   
	            osw = new OutputStreamWriter(proc.getOutputStream());  
	            osw.write(command);   
	            osw.flush();     
	            osw.close();     
	            } catch (IOException ex) { 
	                ex.printStackTrace();   
	                } finally {       
	                    if (osw != null) {     
	                        try {      
	                            osw.close();   
	                            } catch (IOException e) { 
	                                e.printStackTrace(); 
	                                }      
	                            }    
	                    }   
	                try {     
	                    if (proc != null)    
	                        proc.waitFor();  
	                    } catch (InterruptedException e) {  
	                        e.printStackTrace(); 
	                        }   
	                    //sbstdOut.append(ReadBufferedReader(new InputStreamReader(proc.getInputStream()))); 
	                    //sbstdErr.append(ReadBufferedReader(new InputStreamReader(proc.getErrorStream()))); 
	                    if (proc.exitValue() != 0) 
	                    {    

	                    }   */     
	                    } 
		public void aboutnetwork()
		{
			final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			  
			NetworkInfo[] n=cm.getAllNetworkInfo();
			StringTokenizer st=new StringTokenizer(n[0].toString());
			String s=n[0].toString().replace(",", "\n");
			new AlertDialog.Builder(this).setMessage(s+"\n\n"+n[1].toString().replace(",", "\n")+"\n\n"+n[2].toString().replace(",", "\n")+"\n\n"+n[3].toString().replace(",", "\n")+"\n\n"+n[4].toString().replace(",", "\n")).setTitle("Network info!").setIcon(R.drawable.network).show();
		}
		public void aboutdevice()
		{
			Build b=new Build(); 
			new AlertDialog.Builder(this).setMessage("BOARD :"+b.BOARD+"\nBOOTLOADER :"+b.BOOTLOADER+"\nBrand :"+b.BRAND+"\nCPUABI :"+b.CPU_ABI+"\nCPUABI2 :"+b.CPU_ABI2+"\nDevice :"+b.DEVICE+"\nDisplay :"+b.DISPLAY+"\nFingerprint :"+b.FINGERPRINT+"\nHardware :"+b.HARDWARE+"\nHost :"+b.HOST+"\nManufacturer :"+b.MANUFACTURER+"\nModel :"+b.MODEL+"\nProduct :"+b.PRODUCT+"\nRadio :"+b.RADIO+"\nSerial :"+b.SERIAL+"\nTags :"+b.TAGS+"\nTime :"+b.TIME+"\nType :"+b.TYPE+"\nUser :"+b.USER).setTitle("About device").setIcon(R.drawable.details).show();
		}
		@SuppressWarnings("null")
		public void send()
		{
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) 
			{
			    Toast.makeText(this, "no support", 20);
			}
			if (!mBluetoothAdapter.isEnabled())
			{
			    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    startActivityForResult(enableBtIntent, 1);
			}
		/*	ArrayAdapter<String> mArrayAdapter = null;
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			// If there are paired devices
			if (pairedDevices.size() > 0) {
			    // Loop through paired devices
			    for (BluetoothDevice device : pairedDevices) {
			        
					// Add the name and address to an array adapter to show in a ListView
			        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			    }
			}*/
		/*	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
			    public void onReceive(Context context, Intent intent) {
			        String action = intent.getAction();
			        // When discovery finds a device
			        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			            // Get the BluetoothDevice object from the Intent
			            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			            // Add the name and address to an array adapter to show in a ListView
			            mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			        }
			    }
			};
*/
			/*Intent intent = new Intent();  
			intent.setAction(Intent.ACTION_SEND);  
			intent.setType("image/jpg");  
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path.get(getSelectedItemPosition()))) );  
			startActivity(intent);*/
		}
		public void details()
		{
			File detail=new File(path.get(getSelectedItemPosition()));
			if(detail.isFile())
			try 
			{
				//Process process = Runtime.getRuntime().exec("ls -l");
				long bytesize=detail.length();
				long ksize=bytesize/1024;
				long msize=ksize/1024;
				long gsize=msize/1024;
				long remby=bytesize%1024;
				if(msize>0)
				{
					ksize=ksize%1024;
				}
				//Toast.makeText(this,gsize+" GB,"+msize+" MB,"+ksize+" KB and "+remby+"Bytes" , 20).show();
				new AlertDialog.Builder(this).setTitle("Details").setMessage(gsize+" GB,"+msize+" MB,"+ksize+" KB and "+remby+"Bytes\n"+detail.getTotalSpace()/1024+" Total "+detail.getUsableSpace()/1024+"\n"+detail.getPath()).setIcon(R.drawable.details).show();
			} catch (Exception e) { 
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			else
			{
				long result=dirSize(detail);
				long bytesize=result;
				long ksize=bytesize/1024;
				long msize=ksize/1024;
				long gsize=msize/1024;
				long remby=bytesize%1024;
				if(msize>0)
				{
					ksize=ksize%1024;
				}
				new AlertDialog.Builder(this).setTitle("Details").setMessage(gsize+" GB,"+msize+" MB,"+ksize+" KB and "+remby+"Bytes\n"+detail.getTotalSpace()/1024+" Total "+detail.getUsableSpace()/1024+"\n"+detail.getPath()).show();
			}
		}
		private static long dirSize(File dir) 
		{
		    long result = 0;

		    Stack<File> dirlist= new Stack<File>();
		    dirlist.clear();

		    dirlist.push(dir);

		    while(!dirlist.isEmpty())
		    {
		        File dirCurrent = dirlist.pop();

		        File[] fileList = dirCurrent.listFiles();
		        for (int i = 0; i < fileList.length; i++) {

		            if(fileList[i].isDirectory())
		                dirlist.push(fileList[i]);
		            else
		                result += fileList[i].length();
		        }
		    }

		    return result;
		}

		public void rename()
		{
			try
			{
			final File file=new File(path.get(getSelectedItemPosition()));
			
			final EditText input = new EditText(this);
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Enter details");
			alert.setMessage("Current name : "+file.getName()+"\nNew name for file/folder: ");
			alert.setView(input);
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
						renamefolder(input.getText().toString(),file);
				  // Do something with value!
				  }
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				}).show();

			}
			catch(Exception ex)
			{
				new AlertDialog.Builder(this).setIcon(R.drawable.fail).setTitle("Error").setMessage("Please select file to delete!").setPositiveButton("Okay",
						new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) 
			           {    	}
			       }).show();
			}			
		}
		public void renamefolder(String rename,File file)
		{
			try
			{
				boolean res=false;
			if(file.isDirectory())
			{
				File newfile=new File(file.getParent(),rename);
				res=file.renameTo(newfile);
			}else
			{
				
				File newfile=new File(file.getParent(),rename);
				res=file.renameTo(newfile);
			}
				
			if(res)
			{
				success(file);
			}
			else
				fail(file);
			getDir(curdir);
		}
		catch(Exception ex)
		{
			new AlertDialog.Builder(this).setIcon(R.drawable.fail).setTitle("Error").setMessage("Please select file to rename!").setPositiveButton("Okay",
					new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) 
		           {    	}
		       }).show();
		}
		
		}
		public void copy()
		{
			
			df=sf=null;
			sf=new File(path.get(getSelectedItemPosition()));
			Log.d("SOURCE", sf.getAbsolutePath());
		
			if(sf.isDirectory())
			{
				desdir=1;
				desfile=0;
			}
			else
			{
				desfile=1;
				desdir=0;
			}
		}
		public void paste(File src, File dest)
		{
			if(src.isDirectory()){
				 
	    		//if directory not exists, create it
	    		if(!dest.exists())
	    		{
	    		   dest.mkdir();
	    		   System.out.println("Directory copied from " + src + "  to " + dest);
	    		}
	 
	    		//list all the directory contents
	    		String files[] = src.list();
	 
	    		for (String file : files) {
	    		   //construct the src and dest file structure
	    		   File srcFile = new File(src, file);
	    		   File destFile = new File(dest, file);
	    		   //recursive copy
	    		   paste(srcFile,destFile);
	    		}
	 
	    	}else{
	    		//if file, then copy it
	    		//Use bytes stream to support all file types
	    		InputStream in;
				try 
				{
					in = new FileInputStream(src);
					OutputStream out = new FileOutputStream(dest); 
					 
	    	        byte[] buffer = new byte[1024];
	 
	    	        int length;
	    	        //copy the file content in bytes 
	    	        while ((length = in.read(buffer)) > 0){
	    	    	   out.write(buffer, 0, length);
	    	        }
	 
	    	        in.close();
	    	        out.close();
	    	        System.out.println("File copied from " + src + " to " + dest);
	    	
				} 
				catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	        
	    }
		}
		public void pastefile()
		{
			
			df = new File(curdir+"/"+sf.getName());
			Log.d("SOURCE",sf.getAbsolutePath());
			Log.d("DES",df.getAbsolutePath());
			BufferedInputStream bis = null;
		    BufferedOutputStream bos = null;
			try 
		    {
		      bis = new BufferedInputStream(new FileInputStream(sf));
		      bos = new BufferedOutputStream(new FileOutputStream(df));
		      Log.d("WRITING FILE",df.getAbsolutePath());
		      byte[] buf = new byte[1024];
		      bis.read(buf);
		      
		      do 
		      {
		        bos.write(buf);
		      } while(bis.read(buf) != -1);
		    }
		    catch (IOException e) 
		    {} 
		    finally 
		    {
		      try 
		      {
		        if (bis != null) bis.close();
		        if (bos != null) bos.close();
		      } 
		      catch (IOException e) 
		      {}
		    }
		    getDir(curdir);
			
		}
		public void newfolder()
		{
			//final Editable value;
			final EditText input = new EditText(this);
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Enter details");
			alert.setMessage("Enter name for folder : ");

			alert.setView(input);
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
						createfolder(input.getText().toString());
				  // Do something with value!
				  }
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				}).show();
	}
		public void createfolder(String foldername)
		{
			
				File folder = new File(curdir+"/"+foldername);
				Log.d("NewFolder", curdir+""+foldername);
				//Log.d("Public", Environment.getExternalStoragePublicDirectory(root).toString());
				if(curdir.contains("/sdcard"))
				{
				Boolean res=folder.mkdirs();
				if(res)
				new AlertDialog.Builder(this)
				.setIcon(R.drawable.success)
				.setTitle("[" + folder.getName() + "] created succesfully!")
				.setPositiveButton("OK", 
						new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) 
							{
								// TODO Auto-generated method stub
							}
						}).show();
				else
					new AlertDialog.Builder(this)
				.setIcon(R.drawable.fail)
				.setTitle("[" + folder.getName() + "] already exists!")
				.setPositiveButton("OK", 
						new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) 
							{
								// TODO Auto-generated method stub
							}
						}).show();	
				}
				else
				new AlertDialog.Builder(this)
				.setIcon(R.drawable.fail)
				.setTitle("Error creating folder!")
				.setPositiveButton("OK", 
						new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) 
							{
								// TODO Auto-generated method stub
							}
						}).show();
			
			getDir(curdir);
		}
		public void deletefile()
		{
			try
			{
				if(getSelectedItemPosition()<=1)
				{
					new AlertDialog.Builder(this).setTitle("Parent folders can not be deleted from child folder").setIcon(R.drawable.fail).setPositiveButton("Okay", null).show();
					return;
				}
			final File file= new File(path.get(getSelectedItemPosition()));
			String filepath=file.getParent();
			
			Log.d("DELETE", path.get(getSelectedItemPosition()));
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(R.drawable.delete).setTitle("Confirmation").setMessage("Are you sure you want to delete?")
			       .setCancelable(false)
			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   boolean deleted ;
			        	   if (file.isDirectory()) 
			        	   {
			        	      deleted=deletedirectory(file);
			        	    }else
			        	    	 deleted = file.delete();

			        	      if(deleted)
			        	      {
			        	    	  success(file);
			        	    	  getDir(file.getParent());
			        	      }
			        	      else
			        	      {
			        	    	  fail(file);
			        	      }
			           }
			       })
			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       }).show();
			//AlertDialog alert = builder.create();
			getDir(filepath);
			}
			catch(Exception ex)
			{
				new AlertDialog.Builder(this).setIcon(R.drawable.fail).setTitle("Error").setMessage("Please select file to delete!").setPositiveButton("Okay",
						new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) 
			           {    	}
			       }).show();
			}
		}
		public boolean deletedirectory(File fileOrDirectory)
		{
		    if (fileOrDirectory.isDirectory())
		        for (File child : fileOrDirectory.listFiles())
		            deletedirectory(child);

		    return fileOrDirectory.delete();
		}

		public void success(File file)
		{
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.success)
			.setTitle("Operation on [" + file.getName() + "] succesfull!")
			.setPositiveButton("OK", 
					new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					}).show();
		}
		public void fail(File file)
		{
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.delete)
			.setTitle("Operation on [" + file.getName() + "] unsuccesfull!")
			.setPositiveButton("OK", 
					new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) 
						{
							// TODO Auto-generated method stub
						}
					}).show();
		}
		public void quit() 
		{
	        int pid = android.os.Process.myPid();
	        android.os.Process.killProcess(pid);
	        System.exit(0);
	    }
		public void searchaudio()
		{
			Intent intent=new Intent(ExplorerActivity.this,FindAudio.class);
			startActivity(intent);
	//		FindFilesByType f=new FindFilesByType();
			finish();
			
		}
		public void searchimage()
		{
			Intent intent=new Intent(ExplorerActivity.this,FindImage.class);
			startActivity(intent);
	//		FindFilesByType f=new FindFilesByType();
			finish();
			
		}
		public void searchvideo()
		{
			Intent intent=new Intent(ExplorerActivity.this,FindVideo.class);
			startActivity(intent);
	//		FindFilesByType f=new FindFilesByType();
			finish();
			
		}
			@Override
		protected void onListItemClick(ListView l, View v, int position, long id) 
		{
			if(tapcount==0)
			{
				listpos=position;
				tapcount++;
			}else
				tapcount++;
			if(listpos!=position)
			{
				tapcount=0;
			}
			
				
			Log.d("position", ""+position);
			Log.d("listposition", ""+listpos);
			Log.d("tapcount", ""+tapcount);
			
			if(listpos==position && tapcount==2 )
			{
				tapcount=0;
				final File file= new File(path.get(listpos));
				filepath=file.getAbsolutePath();
				if (file.isDirectory())
				{
					if(file.canRead())
						getDir(path.get(listpos));
					else
					{
						new AlertDialog.Builder(this)
						.setIcon(R.drawable.icon)
						.setTitle("[" + file.getName() + "] folder can't be read!")
						.setPositiveButton("OK", 
								new DialogInterface.OnClickListener() {
									
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
									}
								}).show();
					}
				}
				else
				{
					new AlertDialog.Builder(this)
						.setIcon(R.drawable.open)
						.setTitle("Click Ok to open this [" + file.getName() + "] file.")
						.setPositiveButton("OK", 
								new DialogInterface.OnClickListener() {
									
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										
										open(file);
										
									//	openOptionsMenu();
	/*									Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
										startActivity(intent);
	*/			}
								}).show();
				}
				


			}
			
		}
			
		
public void open(File file)
		{
			
			if(file.getName().contains(".mp3") || file.getName().contains(".ogg") || file.getName().contains(".flac") || file.getName().contains(".mid") || file.getName().contains(".imy") || file.getName().contains(".xmf") || file.getName().contains(".mxmf") || file.getName().contains(".rtttl") || file.getName().contains(".rtx") || file.getName().contains(".ota") || file.getName().contains(".wav"))
			{
			Intent intent = new Intent(Intent.ACTION_VIEW);  
			File file1 = new File(file.getAbsolutePath());  
			intent.setDataAndType(Uri.fromFile(file1),"audio/*");  
			startActivity(intent);
			}else
				if(file.getName().contains(".3gp")||file.getName().contains(".3GP")||file.getName().contains(".3g2") ||file.getName().contains(".3G2") || file.getName().contains(".mp4") || file.getName().contains(".MP4") ||file.getName().contains(".MPEG") || file.getName().contains(".WEBM") ||file.getName().contains(".webm"))
				{
					Intent intent = new Intent(Intent.ACTION_VIEW);
					File file1 = new File(file.getAbsolutePath());
					intent.setDataAndType(Uri.fromFile(file1), "video/*");
					startActivity(intent);

				}
				else
					if(file.getName().contains(".jpeg")||file.getName().contains(".JPEG")||file.getName().contains(".JPG")||file.getName().contains(".jpg") || file.getName().contains(".png") ||file.getName().contains(".PNG")|| file.getName().contains(".gif")|| file.getName().contains(".GIF")||file.getName().contains(".bmp")||file.getName().contains(".BMP")||file.getName().contains(".webp")||file.getName().contains(".WEBP") )
					{
						Intent intent = new Intent(Intent.ACTION_VIEW);
						File file1 = new File(file.getAbsolutePath());
						intent.setDataAndType(Uri.fromFile(file1), "image/*");
						startActivity(intent);
					}
					else
						//if(path.get(getSelectedItemPosition()).endsWith("/"))
						//	getDir(curdir+"/"+file.getName());
						//else
						new AlertDialog.Builder(this).setIcon(R.drawable.fail).setTitle("Error!").setMessage("Unrecognized format!").setPositiveButton("Ok",new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							}}).show();
		}
}