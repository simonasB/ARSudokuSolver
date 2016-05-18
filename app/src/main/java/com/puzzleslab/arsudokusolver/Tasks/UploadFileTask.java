package com.puzzleslab.arsudokusolver.Tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Created by Simonas on 2016-05-18.
 */
public class UploadFileTask extends AsyncTask<String, Void, FileMetadata> {
    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadComplete(FileMetadata result);
        void onError(Exception e);
    }

    public UploadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result);
        }
    }

    @Override
    protected FileMetadata doInBackground(String... params) {
        File localFile = new File(params[0]);
        if (localFile != null) {
            try {
                /*Comparator<Metadata> comparator = new Comparator<Metadata>() {
                    @Override
                    public int compare(Metadata lhs, Metadata rhs) {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                };
                List<Metadata> metadata = mDbxClient.files().listFolder("").getEntries();
                Collections.sort(metadata, comparator);
                BigInteger fileName = new BigInteger(String.valueOf(metadata.get(metadata.size() - 1).getName().charAt(0)));
                if(params[1].isEmpty()) {
                    fileName = fileName.add(new BigInteger("1"));
                }*/
                InputStream inputStream = new FileInputStream(localFile);
                return mDbxClient.files().uploadBuilder("/" + UUID.randomUUID() + ".png")
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
            } catch (DbxException | IOException e) {
                mException = e;
            }
        }

        return null;
    }
}
