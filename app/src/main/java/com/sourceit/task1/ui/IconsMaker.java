package com.sourceit.task1.ui;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by User on 22.02.2016.
 */
public class IconsMaker extends AsyncTask<Void, Void, Void> {

//    public interface Callback{
//        void done();
//    }

    private static final String TEMP_FILE = "temp.zip";

    AssetManager assetManager;
    String zipName;
    File folderForUnzip;
//    Callback callback;

    public IconsMaker(AssetManager assetManager, String zipName, File folderForUnzip) {
        this.assetManager = assetManager;
        this.zipName = zipName;
        this.folderForUnzip = folderForUnzip;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            folderForUnzip.mkdirs();
            File tempZip = new File(folderForUnzip, TEMP_FILE);
            tempZip.createNewFile();

            copyAssets(zipName, tempZip);

            unzip(tempZip, folderForUnzip);

            tempZip.delete();
        } catch (Exception e) {
            Log.e("IconsMaker", "I have bad news for you: ", e);
        }
        return null;
    }

    public void start() {
        execute();
    }

    private void copyAssets(String zipNameInAssets, File zip) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(zipNameInAssets);
            out = new FileOutputStream(zip);
            copyFile(in, out);
        } catch (IOException e) {
            Log.e("tag", "Failed to copy asset file", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public void unzip(File zipFile, File location) {
        try {
            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v("Decompress", "Unzipping " + ze.getName());

                if (ze.isDirectory()) {
                    _dirChecker(location, ze.getName());
                } else {
                    FileOutputStream fout = new FileOutputStream(new File(location, ze.getName()));
                    copyFile(zin, fout);
                    zin.closeEntry();
                    fout.close();
                }
            }
            zin.close();
        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
        }
    }

    private void _dirChecker(File location, String dir) {
        File f = new File(location, dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }
}
