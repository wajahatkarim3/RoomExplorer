package com.wajahatkarim3.roomexplorer;

import android.content.Context;
import android.content.Intent;

import androidx.room.RoomDatabase;

import static com.wajahatkarim3.roomexplorer.RoomExplorerActivity.DATABASE_CLASS_KEY;
import static com.wajahatkarim3.roomexplorer.RoomExplorerActivity.DATABASE_NAME_KEY;

public class RoomExplorer
{
    /**
     * Launches {@link RoomExplorerActivity} from the context passed in the method.
     * @param context The context such as any activity or fragment or context reference
     * @param databaseClass The database class registered in Room with @Database annotation and extended with RoomDatabase
     * @param dbName The name of your Room Database
     */
    public static void show(Context context, Class<? extends RoomDatabase> databaseClass, String dbName)
    {
        Intent ii = new Intent(context, RoomExplorerActivity.class);
        ii.putExtra(DATABASE_CLASS_KEY, databaseClass);
        ii.putExtra(DATABASE_NAME_KEY, dbName);
        context.startActivity(ii);
    }
}
