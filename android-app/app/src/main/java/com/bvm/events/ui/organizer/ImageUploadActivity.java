package com.bvm.events.ui.organizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bvm.events.databinding.ActivityImageUploadBinding;
import com.bvm.events.model.ApiResponse;
import com.bvm.events.model.EventImage;
import com.bvm.events.network.ApiCallback;
import com.bvm.events.network.ApiClient;
import com.bvm.events.util.Constants;
import com.bvm.events.util.InsetsUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class ImageUploadActivity extends AppCompatActivity {

    private ActivityImageUploadBinding binding;
    private int eventId;
    private Uri selectedImageUri;
    private Uri cameraTempUri;
    private File cameraTempFile;
    private Call<ApiResponse<EventImage>> activeCall;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    displaySelectedImage(uri);
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraTempUri != null) {
                    selectedImageUri = cameraTempUri;
                    displaySelectedImage(cameraTempUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityImageUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applyPadding(binding.getRoot(), true, true);

        eventId = getIntent().getIntExtra(Constants.EXTRA_EVENT_ID, -1);
        if (eventId <= 0) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnPickGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        binding.btnTakePhoto.setOnClickListener(v -> launchCamera());
        binding.btnUpload.setOnClickListener(v -> uploadImage());
    }

    private void launchCamera() {
        try {
            File cacheImagesDir = new File(getCacheDir(), "images");
            if (!cacheImagesDir.exists()) {
                cacheImagesDir.mkdirs();
            }
            cameraTempFile = File.createTempFile("cam_photo_", ".jpg", cacheImagesDir);
            cameraTempUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    cameraTempFile
            );
            cameraLauncher.launch(cameraTempUri);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displaySelectedImage(Uri uri) {
        binding.imgPreview.setImageURI(uri);
        binding.tvPlaceholderHint.setVisibility(View.GONE);
        hideError();
    }

    private void uploadImage() {
        hideError();

        if (selectedImageUri == null) {
            showError("Please select or capture a photo first.");
            return;
        }

        setLoading(true);

        // Process and downscale bitmap in a background thread
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) inputStream.close();

                if (originalBitmap == null) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        showError("Failed to decode image. Please choose another.");
                    });
                    return;
                }

                // Downscale to max 1600px dimension
                int maxDimension = 1600;
                int width = originalBitmap.getWidth();
                int height = originalBitmap.getHeight();
                float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);

                Bitmap scaledBitmap = originalBitmap;
                if (ratio < 1.0f) {
                    int newWidth = Math.round(ratio * width);
                    int newHeight = Math.round(ratio * height);
                    scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
                }

                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, bytesOut);
                byte[] imageBytes = bytesOut.toByteArray();

                File tempUploadFile = new File(getCacheDir(), "upload_scaled.jpg");
                FileOutputStream fos = new FileOutputStream(tempUploadFile);
                fos.write(imageBytes);
                fos.flush();
                fos.close();

                runOnUiThread(() -> performNetworkUpload(tempUploadFile));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showError("Error preparing image: " + e.getMessage());
                });
            }
        }).start();
    }

    private void performNetworkUpload(File file) {
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part bodyPart = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
        RequestBody eventIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(eventId));
        RequestBody isPosterPart = RequestBody.create(MediaType.parse("text/plain"), binding.switchIsPoster.isChecked() ? "1" : "0");

        Call<ApiResponse<EventImage>> call = ApiClient.getInstance()
                .getService()
                .uploadImage(eventIdPart, isPosterPart, bodyPart);
        activeCall = call;

        call.enqueue(new ApiCallback<EventImage>() {
            @Override
            public void onSuccess(EventImage data) {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);
                Toast.makeText(ImageUploadActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, String errorCode) {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);
                showError(message);
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnUpload.setEnabled(!loading);
        binding.btnPickGallery.setEnabled(!loading);
        binding.btnTakePhoto.setEnabled(!loading);
        binding.uploadProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String msg) {
        binding.tvUploadError.setText(msg);
        binding.tvUploadError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.tvUploadError.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeCall != null && !activeCall.isCanceled()) {
            activeCall.cancel();
        }
    }
}

