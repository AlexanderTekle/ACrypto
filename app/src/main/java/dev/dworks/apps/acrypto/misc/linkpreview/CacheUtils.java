//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package dev.dworks.apps.acrypto.misc.linkpreview;

import android.content.Context;

import com.android.volley.misc.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CacheUtils {
    private DiskLruCache mDiskLruCache;

    public CacheUtils(Context context) {
        try {
            this.mDiskLruCache = DiskLruCache.open(getDiskCacheDir(context, "LinkPreviewCache"), 1, 1, 10485760L);
        } catch (IOException var3) {
            ;
        }

    }

    public void put(String key, Link value) {
        try {
            DiskLruCache.Editor ignored = this.mDiskLruCache.edit(hashKeyForDisk(key));
            ObjectOutputStream out = new ObjectOutputStream(ignored.newOutputStream(0));
            out.writeObject(value);
            out.close();
            ignored.commit();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }

    public Link get(String key) {
        try {
            DiskLruCache.Snapshot snapshot = this.mDiskLruCache.get(hashKeyForDisk(key));
            ObjectInputStream ex = new ObjectInputStream(snapshot.getInputStream(0));
            return (Link)ex.readObject();
        } catch (IOException | NullPointerException | ClassNotFoundException var4) {
            return null;
        }
    }


    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            e.update(key.getBytes());
            cacheKey = bytesToHexString(e.digest());
        } catch (NoSuchAlgorithmException var3) {
            cacheKey = String.valueOf(key.hashCode());
        }

        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < bytes.length; ++i) {
            String hex = Integer.toHexString(255 & bytes[i]);
            if(hex.length() == 1) {
                sb.append('0');
            }

            sb.append(hex);
        }

        return sb.toString();
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }
}
