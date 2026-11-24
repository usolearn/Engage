package org.spock.companion;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowInsets;
import android.widget.*;
import java.util.Collections;

/** Foreground-only, explicitly started camera preview. No recording or network transmission. */
public final class VisualScannerActivity extends Activity {
    private static final int CAMERA_REQUEST = 41;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private TextureView preview;
    private TextView status, faceStatus;
    private Switch sound, faceCheck;
    private CameraDevice camera;
    private CameraCaptureSession session;
    private Surface output;
    private ToneGenerator tone;
    private int generation, facing = CameraCharacteristics.LENS_FACING_BACK;
    private boolean foreground, requested, opening;
    private Size previewSize;
    private int sensorOrientation, detectionMode;
    private long lastFaceUpdate;
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(10), dp(12), dp(10));
        root.setBackgroundColor(Color.rgb(17,18,24));
        root.setOnApplyWindowInsetsListener((v,insets)->{
            android.graphics.Insets safe=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
            v.setPadding(safe.left+dp(12),safe.top+dp(10),safe.right+dp(12),safe.bottom+dp(10));
            return WindowInsets.CONSUMED;
        });
        setContentView(root);
        root.requestApplyInsets();
        TextView title = label("ENGAGE • VISUAL SCANNER",22);
        title.setTextColor(Color.rgb(255,204,63)); root.addView(title);
        root.addView(label("Captain, select a camera and start the scan. Live preview only; nothing is recorded or transmitted.",14));
        LinearLayout controls = new LinearLayout(this);
        Button front = new Button(this), rear = new Button(this), stop = new Button(this);
        front.setText("Front / face"); rear.setText("Rear / scene"); stop.setText("Stop");
        controls.addView(front,new LinearLayout.LayoutParams(0,-2,1));
        controls.addView(rear,new LinearLayout.LayoutParams(0,-2,1));
        controls.addView(stop,new LinearLayout.LayoutParams(0,-2,1)); root.addView(controls);
        front.setOnClickListener(v->startScan(CameraCharacteristics.LENS_FACING_FRONT));
        rear.setOnClickListener(v->startScan(CameraCharacteristics.LENS_FACING_BACK));
        stop.setOnClickListener(v->closeScan("Scanner stopped."));
        sound = new Switch(this); sound.setText("Scanner sound"); sound.setTextColor(Color.WHITE);
        sound.setChecked(getPreferences(MODE_PRIVATE).getBoolean("scannerSound",true));
        sound.setOnCheckedChangeListener((v,on)->{getPreferences(MODE_PRIVATE).edit().putBoolean("scannerSound",on).apply();if(!on)stopTone();});
        root.addView(sound);
        faceCheck = new Switch(this); faceCheck.setText("Optional local face / landmark indicator"); faceCheck.setTextColor(Color.WHITE);
        faceCheck.setChecked(false);
        faceCheck.setOnCheckedChangeListener((v,on)->{if(requested)startScan(facing);});
        root.addView(faceCheck);
        status=label("Camera off. Select Front or Rear to begin.",14);root.addView(status);
        faceStatus=label("Face analysis off.",13);root.addView(faceStatus);
        preview=new TextureView(this);
        root.addView(preview,new LinearLayout.LayoutParams(-1,0,1));
        preview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){
            public void onSurfaceTextureAvailable(SurfaceTexture t,int w,int h){maybeOpen();}
            public void onSurfaceTextureSizeChanged(SurfaceTexture t,int w,int h){transformPreview(w,h);}
            public boolean onSurfaceTextureDestroyed(SurfaceTexture t){closeScan("Camera surface closed.");return true;}
            public void onSurfaceTextureUpdated(SurfaceTexture t){}
        });
        Button done=new Button(this);done.setText("Return to ENGAGE");done.setOnClickListener(v->finish());root.addView(done);
    }
    private TextView label(String text,int size){TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTextColor(Color.WHITE);v.setPadding(0,dp(3),0,dp(3));return v;}
    @Override protected void onResume(){super.onResume();foreground=true;}
    @Override protected void onPause(){foreground=false;closeScan("Camera off. Select a camera to resume.");super.onPause();}
    @Override protected void onDestroy(){closeScan("Scanner closed.");super.onDestroy();}

    private void startScan(int lens){
        closeScan("Preparing camera…");facing=lens;requested=true;
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_REQUEST);return;
        }
        maybeOpen();
    }
    @Override public void onRequestPermissionsResult(int request,String[] perms,int[] results){
        super.onRequestPermissionsResult(request,perms,results);
        if(request==CAMERA_REQUEST){
            if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED&&foreground){requested=true;maybeOpen();}
            else closeScan("Camera permission not available. Select a camera to retry.");
        }
    }

    private void closeScan(String message){
        generation++;requested=false;opening=false;stopTone();
        CameraCaptureSession oldSession=session;session=null;if(oldSession!=null)oldSession.close();
        CameraDevice oldCamera=camera;camera=null;if(oldCamera!=null)oldCamera.close();
        Surface oldSurface=output;output=null;if(oldSurface!=null)oldSurface.release();
        if(status!=null)status.setText(message);
        if(faceStatus!=null)faceStatus.setText("Face analysis off.");
        if(preview!=null)preview.setAlpha(0f);
    }
    private void maybeOpen(){
        if(!foreground||!requested||opening||camera!=null||preview==null||!preview.isAvailable())return;
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)return;
        final int ticket=++generation;opening=true;
        try{
            CameraManager manager=getSystemService(CameraManager.class);
            if(manager==null){closeScan("Camera service unavailable.");return;}
            String id=null;CameraCharacteristics chars=null;
            for(String candidate:manager.getCameraIdList()){
                CameraCharacteristics c=manager.getCameraCharacteristics(candidate);
                Integer lens=c.get(CameraCharacteristics.LENS_FACING);
                if(lens!=null&&lens==facing){id=candidate;chars=c;break;}
            }
            if(id==null||chars==null){closeScan("Selected camera unavailable.");return;}
            Integer orientation=chars.get(CameraCharacteristics.SENSOR_ORIENTATION);
            sensorOrientation=orientation==null?0:orientation;
            StreamConfigurationMap map=chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes=map==null?null:map.getOutputSizes(SurfaceTexture.class);
            if(sizes==null||sizes.length==0){closeScan("Preview resolution unavailable.");return;}
            previewSize=sizes[0];
            long best=Long.MAX_VALUE;
            for(Size size:sizes){
                long score=Math.abs((long)size.getWidth()*size.getHeight()-1280L*720L);
                if(score<best){best=score;previewSize=size;}
            }
            detectionMode=CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF;
            if(faceCheck.isChecked()){
                int[] modes=chars.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
                if(modes!=null)for(int mode:modes){
                    if(mode==CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL)detectionMode=mode;
                    else if(mode==CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE&&detectionMode==0)detectionMode=mode;
                }
            }
            faceStatus.setText(!faceCheck.isChecked()?"Face analysis off.":detectionMode==0?"Local face detection unavailable on this camera.":"Local face indicator enabled. No identification or lip reading.");
            manager.openCamera(id,new CameraDevice.StateCallback(){
                @Override public void onOpened(CameraDevice d){
                    if(ticket!=generation||!foreground||!requested){d.close();return;}
                    opening=false;camera=d;createPreview(ticket);
                }
                @Override public void onDisconnected(CameraDevice d){d.close();if(ticket==generation)closeScan("Camera disconnected.");}
                @Override public void onError(CameraDevice d,int error){d.close();if(ticket==generation)closeScan("Camera unavailable ("+error+").");}
            },ui);
        }catch(CameraAccessException|RuntimeException e){closeScan("Camera could not be opened.");}
    }
    private void createPreview(final int ticket){
        try{
            SurfaceTexture texture=preview.getSurfaceTexture();
            if(texture==null||camera==null){closeScan("Preview unavailable.");return;}
            texture.setDefaultBufferSize(previewSize.getWidth(),previewSize.getHeight());
            transformPreview(preview.getWidth(),preview.getHeight());
            output=new Surface(texture);
            final CaptureRequest.Builder request=camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            request.addTarget(output);
            request.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,detectionMode);
            camera.createCaptureSession(Collections.singletonList(output),new CameraCaptureSession.StateCallback(){
                @Override public void onConfigured(CameraCaptureSession s){
                    if(ticket!=generation||!foreground||!requested){s.close();return;}
                    session=s;
                    try{
                        s.setRepeatingRequest(request.build(),new CameraCaptureSession.CaptureCallback(){
                            @Override public void onCaptureCompleted(CameraCaptureSession cs,CaptureRequest req,TotalCaptureResult result){
                                if(ticket!=generation||detectionMode==0||SystemClock.elapsedRealtime()-lastFaceUpdate<300)return;
                                lastFaceUpdate=SystemClock.elapsedRealtime();
                                Face[] faces=result.get(CaptureResult.STATISTICS_FACES);
                                int count=faces==null?0:faces.length;boolean mouth=false;
                                if(faces!=null)for(Face face:faces)if(face.getMouthPosition()!=null)mouth=true;
                                faceStatus.setText("Faces in frame: "+count+" • Mouth landmark: "+(mouth?"available":"not available")+"\nNo identity, speech or emotion inferred.");
                            }
                        },ui);
                        preview.setAlpha(1f);
                        status.setText(facing==CameraCharacteristics.LENS_FACING_FRONT?"Front camera active • local preview":"Rear camera active • local preview");
                        playScanTone();
                    }catch(CameraAccessException|RuntimeException e){closeScan("Preview could not start.");}
                }
                @Override public void onConfigureFailed(CameraCaptureSession s){s.close();if(ticket==generation)closeScan("Preview configuration failed.");}
            },ui);
        }catch(CameraAccessException|RuntimeException e){closeScan("Preview unavailable.");}
    }
    private void transformPreview(int width,int height){
        if(previewSize==null||width<=0||height<=0)return;
        int displayRotation=preview.getDisplay()==null?Surface.ROTATION_0:preview.getDisplay().getRotation();
        int degrees=displayRotation==Surface.ROTATION_90?90:displayRotation==Surface.ROTATION_180?180:displayRotation==Surface.ROTATION_270?270:0;
        int angle=(sensorOrientation+(facing==CameraCharacteristics.LENS_FACING_FRONT?degrees:-degrees)+360)%360;
        boolean swapped=angle==90||angle==270;
        float bw=previewSize.getWidth(),bh=previewSize.getHeight();
        float cx=width/2f,cy=height/2f;
        float rw=swapped?bh:bw,rh=swapped?bw:bh;
        float scale=Math.min(width/rw,height/rh);
        Matrix matrix=new Matrix();
        matrix.setRectToRect(new RectF(0,0,width,height),new RectF(cx-bw*scale/2,cy-bh*scale/2,cx+bw*scale/2,cy+bh*scale/2),Matrix.ScaleToFit.FILL);
        matrix.postRotate(angle,cx,cy);
        if(facing==CameraCharacteristics.LENS_FACING_FRONT)matrix.postScale(-1,1,cx,cy);
        preview.setTransform(matrix);
    }

    // Original diagnostic pulse sequence; no franchise audio is included.
    private void playScanTone(){
        stopTone();
        if(sound==null||!sound.isChecked())return;
        try{
            AudioManager am=getSystemService(AudioManager.class);
            NotificationManager nm=getSystemService(NotificationManager.class);
            if(am==null||nm==null||am.getRingerMode()!=AudioManager.RINGER_MODE_NORMAL||
                nm.getCurrentInterruptionFilter()!=NotificationManager.INTERRUPTION_FILTER_ALL||
                am.getStreamVolume(AudioManager.STREAM_SYSTEM)==0)return;
            tone=new ToneGenerator(AudioManager.STREAM_SYSTEM,28);
            final int ticket=generation;
            final ToneGenerator current=tone;
            current.startTone(ToneGenerator.TONE_PROP_BEEP,80);
            ui.postDelayed(()->{if(ticket==generation&&tone==current)current.startTone(ToneGenerator.TONE_PROP_BEEP2,120);},135);
            ui.postDelayed(()->{if(ticket==generation&&tone==current)current.startTone(ToneGenerator.TONE_PROP_BEEP,80);},310);
            ui.postDelayed(()->{if(tone==current)stopTone();},600);
        }catch(RuntimeException ignored){stopTone();}
    }
    private void stopTone(){ToneGenerator old=tone;tone=null;if(old!=null){try{old.stopTone();old.release();}catch(RuntimeException ignored){}}}
}
