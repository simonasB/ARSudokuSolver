package com.puzzleslab.arsudokusolver.Tasks;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by Simonas on 2016-05-18.
 */
public class UploadFileTask extends AsyncTask<String, Void, SharedLinkMetadata> {
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;
    private String sharedPhotoUrl;

    public interface Callback {
        void onUploadComplete(SharedLinkMetadata result, String sharedPhotoUrl);
        void onError(Exception e);
    }

    public UploadFileTask(DbxClientV2 dbxClient, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(SharedLinkMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result, sharedPhotoUrl);
        }
    }

    @Override
    protected SharedLinkMetadata doInBackground(String... params) {
        File localFile = new File(params[0]);
        if (localFile != null) {
            try {
                InputStream inputStream = new FileInputStream(localFile);
                String fileName = "/" + UUID.randomUUID() + ".png";
                mDbxClient.files().uploadBuilder(fileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
                return mDbxClient.sharing().createSharedLinkWithSettings(fileName);
            } catch (DbxException | IOException e) {
                mException = e;
            }
        }

        return null;
    }
}
