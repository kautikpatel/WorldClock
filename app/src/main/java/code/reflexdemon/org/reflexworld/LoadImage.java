package code.reflexdemon.org.reflexworld;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by venkateswara on 6/20/17.
 */

class LoadImage extends AsyncTask<Object, Void, Bitmap> {

    private ImageView imv;
    private String path;

    public LoadImage(ImageView imv) {
        this.imv = imv;
        this.path = imv.getTag().toString();
    }

    @Override
    protected Bitmap doInBackground(Object... params) {
        Bitmap bitmap = null;
        try {
            URL imagePath = new URL(path);
            bitmap = BitmapFactory.decodeStream(imagePath.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }


        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (!imv.getTag().toString().equals(path)) {
               /* The path is not same. This means that this
                  image view is handled by some other async task.
                  We don't do anything and return. */
            return;
        }

        if (result != null && imv != null) {
            imv.setVisibility(View.VISIBLE);
            imv.setImageBitmap(result);
        } else {
            imv.setVisibility(View.GONE);
        }
    }

}