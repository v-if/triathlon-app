package com.tkpark86.runch;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.tkpark86.runch.common.ComFunc;
import com.tkpark86.runch.common.HttpClient;
import com.tkpark86.runch.common.ProgressDialog;
import com.tkpark86.runch.common.RoundedDrawable;
import com.tkpark86.runch.model.SignUp_Res;


/**
 * A login screen that offers login via email/password.
 */
public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "Goodruns";

    //requestCode
    private static final int REQUESTCODE_GALLERY = 1;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private SignupTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mNicknameView;
    private EditText mPasswordView;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mNicknameView = (AutoCompleteTextView) findViewById(R.id.nickname);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.password || id == EditorInfo.IME_ACTION_DONE) {
                    attemptSignup();
                    return true;
                }
                return false;
            }
        });

        ButtonRectangle btnSignup = (ButtonRectangle) findViewById(R.id.btn_signup_send);
        btnSignup.setRippleSpeed(36f);
        btnSignup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });

        RelativeLayout profile = (RelativeLayout) findViewById(R.id.signup_profile);
        profile.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                openGalleryView();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;
        if (null == data) return;
        String path = "";
        int orientation = 0;

        // Bitmap으로 가져오는 방법
//		try {
//			Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//		} catch(IOException e) {
//			Log.d(LOG_TAG, "SignUpActivity.onActivityResult: IOException");
//		}

        // File path로 가져오는 방법
        Uri uri = data.getData();
        String[] projection = { MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.ORIENTATION };

        if(VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();

            int columnIndex1 = cursor.getColumnIndex(projection[0]);
            int columnIndex2 = cursor.getColumnIndex(projection[1]);
            path = cursor.getString(columnIndex1);
            orientation = cursor.getInt(columnIndex2);
            cursor.close();
        } else {
            int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            getContentResolver().takePersistableUriPermission(uri, takeFlags);

            String id = uri.getLastPathSegment().split(":")[1];
            Uri kitkatUri = getUri();

            String selection = MediaStore.Images.Media._ID + " = " + id;

            Cursor cursor = getContentResolver().query(kitkatUri, projection, selection, null, null);
            cursor.moveToFirst();

            int columnIndex1 = cursor.getColumnIndex(projection[0]);
            int columnIndex2 = cursor.getColumnIndex(projection[1]);
            path = cursor.getString(columnIndex1);
            orientation = cursor.getInt(columnIndex2);
            cursor.close();
        }

        if(path != null) {
            String[] params = new String[]{ path, Integer.toString(orientation) };

            ImageView ivProfile = (ImageView) findViewById(R.id.signup_profile_iv);
            ProfileWorkerTask task = new ProfileWorkerTask(ivProfile);
            task.execute(params);
        }
    }

    // By using this method get the Uri of Internal/External Storage for Media
    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void openGalleryView() {
        // http://programmerguru.com/android-tutorial/how-to-pick-image-from-gallery/
        // http://stackoverflow.com/questions/19834842/android-gallery-on-kitkat-returns-different-uri-for-intent-action-get-content
        // http://stackoverflow.com/questions/20260416/retrieve-absolute-path-when-select-image-from-gallery-kitkat-android
        if(VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            // Show only images, no videos or anything else
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUESTCODE_GALLERY);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUESTCODE_GALLERY);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mNicknameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String nickname = mNicknameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String encodePassword = ComFunc.encodePassword(password);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!ComFunc.isPasswordValidator(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid nickname.
        if (TextUtils.isEmpty(nickname)) {
            mNicknameView.setError(getString(R.string.error_field_required));
            focusView = mNicknameView;
            cancel = true;
        } else if (!ComFunc.isNicknameValidator(nickname)) {
            mNicknameView.setError(getString(R.string.error_invalid_nickname));
            focusView = mNicknameView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!ComFunc.isEmailValidator(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        }

        // set Dialog
        mProgress = new ProgressDialog(this);

        String url = ComFunc.getServiceURL("signup");
        String path = "";
        String fileName = "";
        ImageView ivProfile = (ImageView) findViewById(R.id.signup_profile_iv);
        if(ivProfile.getTag(R.string.tag_path) != null) {
            path = ivProfile.getTag(R.string.tag_path).toString();
            fileName = ivProfile.getTag(R.string.tag_filename).toString();
        }
        String[] params = new String[]{ url, path, fileName, email, nickname, encodePassword };
        SignupTask task = new SignupTask();
        task.execute(params);

    }

    private String getTempPath() {
        String packageName = getPackageName();
        String dir = Environment.getExternalStorageDirectory() + "/Android/data/"+packageName+"/cache/";
        File cacheDir = new File(dir); // sd card
        if(!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir.getPath();
        //return getActivity().getCacheDir(); // local
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class SignupTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            // new String[]{ url, path, fileName, email, nickname, password };
            String url = params[0];
            String path = params[1];
            String fileName = params[2];

            String email = params[3];
            String nickname = params[4];
            String password = params[5];

            String response = "";
            try {
                HttpClient client = new HttpClient(url);
                client.connectForMultipart();
                client.addFormPart("email", email);
                client.addFormPart("nickname", nickname);
                client.addFormPart("password", password);
                if(!path.equals("")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    FileInputStream fis = new FileInputStream(new File(path));
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int length = -1;

                    while((length = fis.read(buffer)) != -1) {
                        baos.write(buffer, 0, length);
                    }
                    fis.close();
                    baos.flush();

                    client.addFilePart("uploadfile", fileName, baos.toByteArray());
                }
                client.finishMultipart();
                response = client.getResponse();

            } catch(Throwable t) {
                //Log.d(LOG_TAG, "SignUpActivity.SendHttpRequestTask.doInBackground: Throwable"+t.getMessage());
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();

            Gson gson = new Gson();
            SignUp_Res res = gson.fromJson(result, SignUp_Res.class);
            if(res != null) {
                if(res.returnCode.equals("000")) {
                    String password = mPasswordView.getText().toString();
                    String encodePassword = ComFunc.encodePassword(password);
                    ComFunc.logIn(SignupActivity.this, res.output.member_id, res.output.email, encodePassword, res.output.nickname, res.output.profile_img_url);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    // Show Popup
                    new AlertDialogWrapper.Builder(SignupActivity.this)
                            .setTitle(R.string.notice)
                            .setMessage(res.returnMessage)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "onClick: accept");
                                }
                            }).show();
                }
            } else {
                // Show Popup
                new AlertDialogWrapper.Builder(SignupActivity.this)
                        .setTitle(R.string.notice)
                        .setMessage(res.returnMessage)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: ok");
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: cancel");
                            }
                        })
                        .show();
            }
        }
    }

    class ProfileWorkerTask extends AsyncTask<String, Void, ProfileTask> {
        private final WeakReference<ImageView> imageViewReference;

        public ProfileWorkerTask (ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected ProfileTask doInBackground(String... params) {
			/* UserImg Process Logic
			 * 1.Read Bitmap Dimensions and Type
			 * 2.Ratio Calculator
			 * 3.Calculate inSampleSize
			 * 4.Decode bitmap with inSampleSize set
			 * 5.Orientation check
			 * 6.Bitmap Crop
			 * 7.Bitmap Round
			 * 8.Create TempFile
			 * 9.ImageView Set
			 */
            String path = params[0];
            int orientation = Integer.parseInt(params[1]);

            // 1.Read Bitmap Dimensions and Type
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int imageWidth = options.outWidth;
            int imageHeight = options.outHeight;
            String imageType = options.outMimeType;

            // 2.Ratio Calculator
            int displayWidth = 200;
            int displayHeight = displayWidth * imageHeight / imageWidth;

            // 3.Calculate inSampleSize
            options.inSampleSize = ComFunc.calculateInSampleSize(options, displayWidth, displayHeight);

            // 4.Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap source = BitmapFactory.decodeFile(path, options);

            // 5.Orientation check
            if(orientation != 0) {
                int width = source.getWidth();
                int height = source.getHeight();

                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                source = Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);
            }

            // 6.Bitmap Crop
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap crop = Bitmap.createBitmap(source, x, y, size, size);
            if (crop != source) {
                source.recycle();
            }

            // 7.Bitmap Round
            Drawable drawable = new RoundedDrawable(crop);
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            Bitmap round = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(round);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);

            // 8.Create TempFile
            String tempPath = getTempPath();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            Date today = new Date();
            String date = sdf.format(today);

            StringBuilder sb = new StringBuilder();
            sb.append(date).append(".png");
            String tempFileName = sb.toString();

            File tempFile = new File(tempPath, tempFileName);
            try{
                FileOutputStream fos = new FileOutputStream(tempFile);
                round.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                tempPath = tempFile.getPath();
                //Log.d(LOG_TAG, "SignUpActivity.ProfileWorkerTask.doInBackground: 8.Create TempFile: tempFileName="+tempFileName+", tempFile=" + tempPath);
            } catch(FileNotFoundException e) {
                //Log.d(LOG_TAG, "SignUpActivity.ProfileWorkerTask.doInBackground: 8.Create TempFile: FileNotFoundException");
            } catch(IOException e) {
                //Log.d(LOG_TAG, "SignUpActivity.ProfileWorkerTask.doInBackground: 8.Create TempFile: IOException");
            }

            ProfileTask item = new ProfileTask();
            item.bitmap = round;
            item.path = tempPath;
            item.fileName = tempFileName;

            return item;
        }

        @Override
        protected void onPostExecute(ProfileTask item) {
            if (isCancelled()) {
                item.bitmap = null;
            }

            if (imageViewReference != null && item.bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(item.bitmap);
                    imageView.setTag(R.string.tag_path, item.path);
                    imageView.setTag(R.string.tag_filename, item.fileName);
                }
            }
        }
    }

    public class ProfileTask {
        Bitmap bitmap;
        String path;
        String fileName;
    }
}

