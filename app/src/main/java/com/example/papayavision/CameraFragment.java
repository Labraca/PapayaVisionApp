package com.example.papayavision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RegistroViewModel viewModel;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Camera camera;
    private RegRepository repoReg;
    private FloatingActionButton photoButton;
    private String currentPhotoPath;
    private OpenCVModuleMT openCVModuleMT;
    public CameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CameraFragment newInstance(String param1, String param2) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        repoReg = new RegRepository(getActivity().getApplication());
        openCVModuleMT = OpenCVModuleMT.getOpenCVInstance(getContext());
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

                        Foto foto = openCVModuleMT.calculatePercents(finalFotoFile);
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
}