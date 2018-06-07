package carrie.toy.friends.automoney.util;

import android.content.Context;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
	public static String data = "doslvhxps^^;";
	public static String get_data  = "!!;Eoqkrsowk2xla";
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    
    public static boolean file_check(String path){
		File files = new File(path);
		if(files.exists() == true) {
		return true;
		} else {
		return false;
		}
	}
    
    public static void file_delete(String path){
    	File file = new File(path);
    	file.delete();

    }
    
    public static String language(Context context){
    	String language_type =  context.getResources().getConfiguration().locale.toString();
    	return language_type;
    }
}