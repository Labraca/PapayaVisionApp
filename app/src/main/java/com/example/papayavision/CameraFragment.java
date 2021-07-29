package com.example.papayavision;

import android.app.Instrumentation;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.camera2.internal.annotation.CameraExecutor;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.CameraView;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ReportFragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.papayavision.DBUtilities.RegRepository;
import com.example.papayavision.entidades.Foto;
import com.example.papayavision.entidades.Registro;
import com.example.papayavision.entidades.RegistroDao;
import com.example.papayavision.entidades.RegistroViewModel;
import com.example.papayavision.regUtilities.OpenCVModule;
import com.example.papayavision.regUtilities.OpenCVModuleMT;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends Fragment {


    private RegistroViewModel viewModel;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Camera camera;
    private RegRepository repoReg;
    private FloatingActionButton photoButton,galeryButton;
    private String currentPhotoPath;
    private OpenCVModule openCVModule;
    public CameraFragment() {
        // Required empty public constructor
    }



    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        repoReg = new RegRepository(getActivity().getApplication());
        openCVModule = OpenCVModule.getOpenCVInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Box box = new Box(getContext());
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        ConstraintLayout layout = view.findViewById(R.id.constraintLayout);

        layout.addView(box);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(RegistroViewModel.class);
        previewView = view.findViewById(R.id.cameraView);
        startCamera();
        photoButton = view.findViewById(R.id.floatingActionButton);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {

                        int idReg = viewModel.getSelectedItem().getValue().getIdRegistro();
                        File fotof = new File(getPath(uri));
                        Foto foto = openCVModule.calculatePercents(fotof);
                        foto.setPathImage(fotof.getAbsolutePath());
                        foto.setRegistroId(idReg);

                        repoReg.insertFoto(foto,getViewLifecycleOwner());
                        Log.i("Foto","Foto Calculada");


                    }
                });
        galeryButton = view.findViewById(R.id.galleryButton);
        galeryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });
    }

    private void startCamera(){

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(getContext()));


    }

    private void takePhoto(){
        Registro reg = viewModel.getSelectedItem().getValue();

        File fotoFile = null;
        try {
            fotoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(fotoFile).build();

        File finalFotoFile = fotoFile;
        
        imageCapture.takePicture(outputFileOptions, requireContext().getMainExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.@NotNull OutputFileResults outputFileResults) {
                        // insert your code here.
                        int idReg = reg.getIdRegistro();
                        //ya que 10 fotos semanales de media podria acabar llenando la memoria vamos a comprimir la imagen
                        resizeImage(finalFotoFile);

                        Foto foto = openCVModule.calculatePercents(finalFotoFile);
                        foto.setPathImage(finalFotoFile.getAbsolutePath());
                        foto.setRegistroId(idReg);

                        repoReg.insertFoto(foto,getViewLifecycleOwner());
                        Log.i("Imagen Guardada","Se ha guardado una foto en "+finalFotoFile.getPath());
                        Toast.makeText(getActivity().getApplicationContext(), "La imagen guardada",Toast.LENGTH_LONG).show();

                    }
                    @Override
                    public void onError(ImageCaptureException error) {
                        // insert your code here.
                        Log.w("Imagen no salvada","La imagen no ha podido ser guardada");
                        error.printStackTrace();
                        Toast.makeText(getActivity().getApplicationContext(),"La imagen no ha podido ser guardada",Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PPY_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".PNG",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void resizeImage(File image){
        Bitmap b = BitmapFactory.decodeFile(image.getAbsolutePath());
        float w = b.getWidth();
        float h = b.getHeight();
        float imgratio = h/w;
        int newW = 2048;
        int newH = (int) (newW * imgratio);
        //creo un nuevo bitmap mas pequeño con el mismo ratio utilizando una compresion bilinear
        Bitmap out = null;
        try {
            out = Bitmap.createScaledBitmap(b, newW, newH, true);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            Log.e("CV","w: "+w+", h: "+h);
        }
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(image);
            out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            b.recycle();
            out.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

       // Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
        imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();

        camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageCapture, preview);
    }
    public String getPath(Uri uri)
    {
        String docId = DocumentsContract.getDocumentId(uri);
        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] split = docId.split(":");
        String selection = "_id=?";
        String[] selectionArgs = new String[]{
                split[1]
        };

        return getDataColumn(contentUri, selection, selectionArgs);
    }

    public String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = {
                column
        };
        try {
            cursor = getContext().getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(column);
                String value = cursor.getString(column_index);
                if (value.startsWith("content://") || !value.startsWith("/") && !value.startsWith("file://")) {
                    return null;
                }
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}