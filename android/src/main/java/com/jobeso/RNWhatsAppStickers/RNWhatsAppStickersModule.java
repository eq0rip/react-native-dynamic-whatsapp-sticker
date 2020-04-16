
package com.jobeso.RNWhatsAppStickers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.orhanobut.hawk.Hawk;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;

public class RNWhatsAppStickersModule extends ReactContextBaseJavaModule {
  public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
  public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
  public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";
  public static String path;
  public static final int ADD_PACK = 200;
  public static final String ERROR_ADDING_STICKER_PACK = "Could not add this sticker pack. Please install the latest version of WhatsApp before adding sticker pack";

  private final ReactApplicationContext reactContext;

  public RNWhatsAppStickersModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    Hawk.init(reactContext).build();
    // stickerPacks = new ArrayList<>();
    path = reactContext.getFilesDir() + "/" + "stickers_asset";
  }

  @Override
  public String getName() {
    return "RNWhatsAppStickers";
  }

  @ReactMethod
  public void prepare(String stickerPackParam, Promise promise) {
    List<StickerPack> stickerPacks = new ArrayList<StickerPack>();
    List<Sticker> mStickers  = new ArrayList<>();
    List<String> mEmojis = new ArrayList<>();
    mEmojis.add(")");

    // stickerPack.put("contents", new JSONObject(contents));
    try {
      JSONObject stickerPack = new JSONObject(stickerPackParam);
      stickerPacks.addAll(Hawk.get("sticker_pack", new ArrayList<StickerPack>()));
      // mStickers = Hawk.get(stickerPack.getString("identifier"), new ArrayList<>());
      stickerPacks.add(new StickerPack(stickerPack.getString("identifier"), stickerPack.getString("name"),
          stickerPack.getString("publisher"),
          getLastBitFromUrl(stickerPack.getString("tray_image_file")).replace(" ", "_"),
          stickerPack.getString("publisher_email"), stickerPack.getString("publisher_website"),
          stickerPack.getString("privacy_policy_website"), stickerPack.getString("license_agreement_website")));
      JSONArray stickers = stickerPack.getJSONArray("stickers");
      // Log.d(TAG, "onListLoaded: " + stickers.length());
      for (int j = 0; j < stickers.length(); j++) {
        JSONObject jsonStickersChildNode = stickers.getJSONObject(j);
        new DownloadImage().execute(jsonStickersChildNode.getString("image_file"), stickerPack.getString("identifier"),
            stickerPack.getString("name"));
        mStickers.add(new Sticker(
            getLastBitFromUrl(jsonStickersChildNode.getString("image_file")).replace(".png", ".webp"), mEmojis));
        // mDownloadFiles.add(jsonStickersChildNode.getString("image_file"));
      }
      Hawk.put(stickerPack.getString("identifier"), mStickers);
      stickerPacks.get(stickerPacks.size() - 1).setAndroidPlayStoreLink("https://play.google.com/store/apps/details?id=com.dawwati");
      stickerPacks.get(stickerPacks.size() - 1).setStickers(Hawk.get(stickerPack.getString("identifier"), new ArrayList<Sticker>()));
      Hawk.put("sticker_packs", stickerPacks);
      promise.resolve(stickerPacks.size() + stickerPacks.get(0).getContent().toString());
      // promise.resolve(mStickers.get(0).getContent());
      // promise.resolve(mStickers.get(0).getContent());
    } catch (JSONException e) {
      e.printStackTrace();
      promise.reject(ERROR_ADDING_STICKER_PACK, e);
    }
    // mStickers.clear();
  }

  public static String getContentProviderAuthority(Context context) {
    return context.getPackageName() + ".stickercontentprovider";
  }

  @ReactMethod
  public void send(String identifier, String stickerPackName, Promise promise) {
    Intent intent = new Intent();
    intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
    intent.putExtra(EXTRA_STICKER_PACK_ID, identifier);
    intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY, getContentProviderAuthority(reactContext));
    intent.putExtra(EXTRA_STICKER_PACK_NAME, stickerPackName);

    try {
      Activity activity = getCurrentActivity();
      ResolveInfo should = activity.getPackageManager().resolveActivity(intent, 0);
      if (should != null) {
        activity.startActivityForResult(intent, ADD_PACK);
        promise.resolve("OK");
      } else {
        promise.resolve("OK, but not opened");
      }
    } catch (ActivityNotFoundException e) {
      promise.reject(ERROR_ADDING_STICKER_PACK, e);
    } catch (Exception e) {
      promise.reject(ERROR_ADDING_STICKER_PACK, e);
    }
  }

  @ReactMethod
  public void getDownloadedStickers(Promise promise) {
    try {
      JSONArray identifiers = new JSONArray();
      File[] files = new File(path).listFiles();
      for (File aFile : files) {
        if (aFile.isDirectory()) {
          identifiers.put(aFile.getName());
        }
      }
      promise.resolve(identifiers.toString());
    } catch (Exception e) {
      promise.reject(ERROR_ADDING_STICKER_PACK, e);
    }
  }

  private static String getLastBitFromUrl(final String url) {
    return url.replaceFirst(".*/([^/?]+).*", "$1");
  }

  public static void SaveTryImage(Bitmap finalBitmap, String name, String identifier) {

    String root = path + "/" + identifier;
    File myDir = new File(root + "/" + "try");
    myDir.mkdirs();
    String fname = name.replace(".png", "").replace(" ", "_") + ".png";
    File file = new File(myDir, fname);
    if (file.exists())
      file.delete();
    try {
      FileOutputStream out = new FileOutputStream(file);
      finalBitmap.compress(Bitmap.CompressFormat.PNG, 40, out);
      out.flush();
      out.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void SaveImage(Bitmap finalBitmap, String name, String identifier) {

    String root = path + "/" + identifier;
    File myDir = new File(root);
    myDir.mkdirs();
    String fname = name;
    File file = new File(myDir, fname);
    if (file.exists())
      file.delete();
    try {
      FileOutputStream out = new FileOutputStream(file);
      finalBitmap.compress(Bitmap.CompressFormat.WEBP, 90, out);
      out.flush();
      out.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
    private String TAG = "DownloadImage";
    public String imageFileName;
    public String identifier;
    public String name;

    private Bitmap downloadImageBitmap(String sUrl, String sIdentifier, String sName) {
      imageFileName = getLastBitFromUrl(sUrl).replace(".png", ".webp");
      identifier = sIdentifier;
      name = sName;
      Bitmap bitmap = null;
      try {
        InputStream inputStream = new URL(sUrl).openStream(); // Download Image from URL
        bitmap = BitmapFactory.decodeStream(inputStream); // Decode Bitmap
        inputStream.close();
      } catch (Exception e) {
        Log.d(TAG, "Exception 1, Something went wrong!");
        e.printStackTrace();
      }
      return bitmap;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
      return downloadImageBitmap(params[0], params[1], params[2]);
    }

    protected void onPostExecute(Bitmap result) {
      SaveImage(result, imageFileName, identifier);
    }
  }

}
