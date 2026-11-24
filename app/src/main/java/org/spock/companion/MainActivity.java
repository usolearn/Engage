package org.spock.companion;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.role.RoleManager;
import android.view.WindowInsets;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.SharedPreferences;
import java.util.TreeSet;
import java.util.Set;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.InputType;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.ViewGroup;
import android.widget.*;
import java.util.ArrayList;
import java.util.Locale;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MainActivity extends Activity {
    private LinearLayout panel;
    private TextView report, voiceStatus, changes;
    private EditText phrase, smsNumber, smsBody;
    private static final int PICK_PHONE = 21;
    private SpeechRecognizer recognizer;
    private boolean listening;
    private static final int ASSISTANT_REQUEST = 22;
    private final int accent=Color.rgb(255,204,63);
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private TextView text(String s,int size){ TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setPadding(0,dp(6),0,dp(6));panel.addView(t);return t; }
    private void button(String label,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setOnClickListener(v->action.run()); panel.addView(b,new LinearLayout.LayoutParams(-1,-2));}
    @Override public void onCreate(Bundle state){super.onCreate(state);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(20),dp(24),dp(20),dp(24));panel.setBackgroundColor(Color.rgb(17,18,24));scroll.addView(panel);setContentView(scroll);
        scroll.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets safe=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());v.setPadding(safe.left,safe.top,safe.right,safe.bottom);return WindowInsets.CONSUMED;});
        scroll.requestApplyInsets();
        TextView name=text("ENGAGE",28);name.setTextColor(accent);
        text("YOUR INTELLIGENCE FIRST OFFICER",16).setTextColor(accent);
        text("A Current Works Advisory product • Android pilot v0.6.0",13);
        text("Systems online, Captain.",16);
        Switch startupSound=new Switch(this);startupSound.setText("Startup sound");startupSound.setTextColor(Color.WHITE);startupSound.setChecked(getPreferences(MODE_PRIVATE).getBoolean("startupCue",true));panel.addView(startupSound);startupSound.setOnCheckedChangeListener((v,enabled)->{getPreferences(MODE_PRIVATE).edit().putBoolean("startupCue",enabled).apply();if(!enabled)stopStartupCue();});
        button("Open ChatGPT / SPOCK",()->openPackage("com.openai.chatgpt"));
        button("Choose ENGAGE as digital assistant (experimental)",this::requestAssistant);
        text("Voice setup — optional local calibration",20);
        text("Select your English variety and read the eight phrases. Calibration does not record a voiceprint or retrain the Android model.",14);
        button("Choose speech language / accent",this::showVoiceLocale);
        calibrationPrompt=text("",16);
        if(getPreferences(MODE_PRIVATE).getBoolean("calibrationPassed",false))calibrationStep=CALIBRATION.length;
        updateCalibrationPrompt("");
        button("Read calibration phrase / retry",this::startCalibration);
        button("Voice command — listen once",this::listen);
        text("Permission is requested only when needed. No automatic sending, background microphone, or access to private app data.",14);
        text("Communications / Phone · SMS · WhatsApp",22);
        text("Choose a contact or enter a number. Open the phone dialer, or compose a message for SMS or WhatsApp. Review and confirm in the destination app.",14);
        smsNumber=new EditText(this);smsNumber.setId(1001);smsNumber.setSaveEnabled(false);smsNumber.setSingleLine(true);smsNumber.setInputType(InputType.TYPE_CLASS_PHONE);smsNumber.setHint("Phone number");smsNumber.setTextColor(Color.WHITE);smsNumber.setHintTextColor(Color.LTGRAY);panel.addView(smsNumber,new LinearLayout.LayoutParams(-1,-2));
        button("Choose contact",this::pickPhone);
        button("Convert Philippine mobile number to +63",()->{String number=CommandRules.phMobile(smsNumber.getText().toString());if(number==null)smsNumber.setError("Enter a Philippine mobile number starting 09, or +639.");else smsNumber.setText(number);});
        button("Open phone dialer",this::openDialer);
        smsBody=new EditText(this);smsBody.setId(1002);smsBody.setSaveEnabled(false);smsBody.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);smsBody.setMinLines(3);smsBody.setHint("Message text");smsBody.setTextColor(Color.WHITE);smsBody.setHintTextColor(Color.LTGRAY);panel.addView(smsBody,new LinearLayout.LayoutParams(-1,-2));
        button("Dictate message (on-device)",()->listen(true));
        button("Review in Google Messages",this::composeSms);
        button("Review in WhatsApp",()->composeWhatsApp("com.whatsapp"));
        button("Review in WhatsApp Business",()->composeWhatsApp("com.whatsapp.w4b"));
        button("Clear recipient and message",()->{smsNumber.setText("");smsBody.setText("");});
        text("Cloud productivity / Google · Microsoft",22);
        text("Open authorized apps or prepare an email or calendar draft. Outlook remains the primary calendar and OneDrive the primary file repository. Account sign-in and final changes take place in the destination app.",14);
        button("Google and Microsoft workspace",()->new WorkspaceActivity.Launcher(this).show());
        text("Security overview",22);report=text("",16);
        button("Refresh device checks",this::refresh);
        text("Case 001 — software changes",22);
        changes=text("",14);
        button("Save current software baseline",()->{getPreferences(MODE_PRIVATE).edit().putStringSet("appBaseline",visibleApps()).putString("baselineTime",java.time.ZonedDateTime.now().toString()).apply();refresh();});
        button("Compare with saved baseline",this::refresh);
        button("Security settings / Privacy Dashboard",()->settings(Settings.ACTION_SECURITY_SETTINGS));
        button("Privacy settings",()->settings(Settings.ACTION_PRIVACY_SETTINGS));
        button("Installed applications",()->settings(Settings.ACTION_APPLICATION_SETTINGS));
        button("Accessibility services",()->settings(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        button("Device administrator review",()->settings(Settings.ACTION_SETTINGS));
        button("Open Google Play (Play Protect)",()->openPackage("com.android.vending"));
        button("VPN settings",()->settings(Settings.ACTION_VPN_SETTINGS));
        button("Share security report — choose destination",this::share);
        text("Voice command",22);text("Tap Listen once, then say your saved phrase, open workspace, open Google Chat, open dialer, prepare SMS, or prepare WhatsApp. Commands use the selected number and draft. No always-on microphone or cloud speech fallback.",14);
        phrase=new EditText(this);phrase.setSingleLine(true);phrase.setText(getPreferences(MODE_PRIVATE).getString("phrase","Mr. Spock, analysis"));phrase.setTextColor(Color.WHITE);panel.addView(phrase,new LinearLayout.LayoutParams(-1,-2));
        button("Save wake phrase",()->{String p=phrase.getText().toString().trim();if(!p.isEmpty()){getPreferences(MODE_PRIVATE).edit().putString("phrase",p).apply();voiceStatus.setText("Phrase saved.");}});
        button("Listen once",this::listen);button("Stop microphone",this::stop);
        voiceStatus=text("Microphone idle.",14);
        button("Open ChatGPT",()->openPackage("com.openai.chatgpt"));
        text("This companion does not scan other apps' private files, diagnose spyware, read message history, or control ChatGPT sessions. A normal report does not prove a device is uncompromised.",13);
        if(state==null)receiveSharedText(getIntent());
        refresh();
        if(state==null && getIntent()!=null && Intent.ACTION_MAIN.equals(getIntent().getAction())) playStartupCue();
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);receiveSharedText(intent);}
    private void receiveSharedText(Intent intent){
        if(intent==null||!Intent.ACTION_SEND.equals(intent.getAction())||!"text/plain".equals(intent.getType()))return;
        CharSequence shared=intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        if(shared==null||shared.length()==0)return;
        // Import only text; never accept a recipient or trigger sending from an inbound intent.
        String value=shared.length()>10000?shared.subSequence(0,10000).toString():shared.toString();
        if(smsBody.length()>0){new android.app.AlertDialog.Builder(this).setTitle("Replace current message draft?").setMessage("Shared text will replace the unsent draft and clear the recipient.").setNegativeButton("Cancel",null).setPositiveButton("Replace",(d,w)->{smsNumber.setText("");smsBody.setText(value);}).show();}
        else {smsNumber.setText("");smsBody.setText(value);}
    }
    private void requestAssistant(){
        RoleManager roles=getSystemService(RoleManager.class);
        try{if(roles!=null&&roles.isRoleAvailable(RoleManager.ROLE_ASSISTANT)){
            if(roles.isRoleHeld(RoleManager.ROLE_ASSISTANT)){Toast.makeText(this,"ENGAGE is selected. Assistant gesture opens the companion; no background voice service.",Toast.LENGTH_LONG).show();return;}
            startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT),ASSISTANT_REQUEST);return;
        }}catch(RuntimeException ignored){}
        settings(Settings.ACTION_VOICE_INPUT_SETTINGS);
    }
    private void pickPhone(){
        Intent i=new Intent(Intent.ACTION_PICK);i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        try{startActivityForResult(i,PICK_PHONE);}catch(android.content.ActivityNotFoundException e){Toast.makeText(this,"Contact picker unavailable. Enter the number manually.",Toast.LENGTH_LONG).show();}
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request!=PICK_PHONE||result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri selected=data.getData();
        if(!"content".equals(selected.getScheme()))return;
        String[] fields={ContactsContract.CommonDataKinds.Phone.NUMBER};
        try(Cursor c=getContentResolver().query(selected,fields,null,null,null)){
            if(c!=null&&c.moveToFirst()){String number=c.getString(0);if(number!=null)smsNumber.setText(number);}
        }catch(Exception e){Toast.makeText(this,"Cannot read selected number. Enter it manually.",Toast.LENGTH_LONG).show();}
    }
    private String cleanNumber(){
        return CommandRules.cleanNumber(smsNumber.getText().toString());
    }
    private void openDialer(){
        String number=cleanNumber();
        if(!number.matches("\\+?[0-9]{3,20}")){smsNumber.setError("Enter one phone number.");return;}
        Intent i=new Intent(Intent.ACTION_DIAL,Uri.fromParts("tel",number,null));
        try{startActivity(i);}catch(android.content.ActivityNotFoundException|SecurityException e){Toast.makeText(this,"Phone dialer unavailable.",Toast.LENGTH_LONG).show();}
    }
    private void composeWhatsApp(String pkg){
        String number=cleanNumber();
        if(!number.matches("\\+[1-9][0-9]{6,14}")){smsNumber.setError("WhatsApp requires + and the full international number, without a local trunk prefix.");return;}
        String body=smsBody.getText().toString();
        Uri.Builder link=new Uri.Builder().scheme("https").authority("wa.me").appendPath(number.substring(1));
        if(!body.trim().isEmpty())link.appendQueryParameter("text",body);
        Intent i=new Intent(Intent.ACTION_VIEW,link.build());i.setPackage(pkg);
        try{startActivity(i);}catch(android.content.ActivityNotFoundException|SecurityException e){Toast.makeText(this,"Selected WhatsApp app unavailable or cannot open this link. No message sent.",Toast.LENGTH_LONG).show();}
    }
    private void composeSms(){
        String number=cleanNumber();
        String body=smsBody.getText().toString();
        if(!number.matches("\\+?[0-9]{3,20}")){smsNumber.setError("Enter one valid phone number, including country code if needed.");return;}
        if(body.trim().isEmpty()){smsBody.setError("Enter message text.");return;}
        Intent i=new Intent(Intent.ACTION_SENDTO,Uri.fromParts("smsto",number,null));i.putExtra("sms_body",body);
        i.setPackage("com.google.android.apps.messaging");
        try{startActivity(i);}catch(android.content.ActivityNotFoundException e){
            i.setPackage(null);
            try{startActivity(i);}catch(android.content.ActivityNotFoundException unavailable){Toast.makeText(this,"No compatible messaging app found.",Toast.LENGTH_LONG).show();}
        }
    }
    @Override protected void onResume(){super.onResume();if(report!=null)refresh();}
    private String baseline(){
        KeyguardManager k=getSystemService(KeyguardManager.class);
        boolean locked=k!=null&&k.isDeviceSecure();
        int adb=Settings.Global.getInt(getContentResolver(),Settings.Global.ADB_ENABLED,0);
        int dev=Settings.Global.getInt(getContentResolver(),Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,0);
        ConnectivityManager cm=getSystemService(ConnectivityManager.class);
        NetworkCapabilities caps=cm==null?null:cm.getNetworkCapabilities(cm.getActiveNetwork());
        String vpn=caps==null?"Unknown / offline":caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)?"Active (not evidence of compromise)":"Not detected on default connection";
        String age="";try{long days=ChronoUnit.DAYS.between(LocalDate.parse(Build.VERSION.SECURITY_PATCH),LocalDate.now());age=" ("+days+" days; check for updates in Settings)";}catch(Exception ignored){}
        return "ENGAGE device security baseline\nChecked: "+java.time.ZonedDateTime.now()+"\nAndroid: "+Build.VERSION.RELEASE+"\nSecurity patch: "+Build.VERSION.SECURITY_PATCH+age+"\nScreen lock: "+(locked?"Configured":"Review: not configured")+"\nUSB debugging: "+(adb==1?"Enabled — review if not needed":"Not reported enabled")+"\nDeveloper options: "+(dev==1?"Enabled — review if not needed":"Not reported enabled")+"\nVPN: "+vpn+"\n\nManual review required: Play Protect, privacy dashboard, accessibility, device admins, account sign-ins. These checks do not establish compromise.";
    }
    private Set<String> visibleApps(){
        TreeSet<String> entries=new TreeSet<>();
        PackageManager pm=getPackageManager();
        Intent query=new Intent(Intent.ACTION_MAIN);query.addCategory(Intent.CATEGORY_LAUNCHER);
        for(ResolveInfo ri:pm.queryIntentActivities(query,0)){
            if(ri.activityInfo==null)continue;
            ApplicationInfo ai=ri.activityInfo.applicationInfo;
            if(ai==null||(ai.flags&ApplicationInfo.FLAG_SYSTEM)!=0)continue;
            String pkg=ai.packageName;
            if(pkg.equals(getPackageName()))continue;
            String version="unknown";try{version=Long.toString(pm.getPackageInfo(pkg,0).getLongVersionCode());}catch(Exception ignored){}
            entries.add(pkg+" | version "+version);
        }
        return entries;
    }
    private String appChanges(){
        Set<String> now=visibleApps();SharedPreferences p=getPreferences(MODE_PRIVATE);
        StringBuilder out=new StringBuilder("Visible non-system launcher apps: "+now.size()+"\n");
        if(!p.contains("appBaseline")){out.append("No baseline saved. Review current apps before saving.\n");for(String a:now)out.append(a).append("\n");}
        else {Set<String> prior=p.getStringSet("appBaseline",new TreeSet<>());TreeSet<String> added=new TreeSet<>(now);added.removeAll(prior);TreeSet<String> removed=new TreeSet<>(prior);removed.removeAll(now);
            out.append("Baseline: ").append(p.getString("baselineTime","unknown")).append("\n");
            if(added.isEmpty()&&removed.isEmpty())out.append("No changes among visible launcher apps.\n");
            for(String a:added)out.append("New or updated: ").append(a).append("\n");
            for(String a:removed)out.append("Removed or previous version: ").append(a).append("\n");
        }
        return out.append("Android limits package visibility. System apps, hidden apps and private app behavior are not fully covered. Changes are not evidence of malicious activity.").toString();
    }
    private void refresh(){report.setText(baseline());if(changes!=null)changes.setText(appChanges());}
    private void settings(String action){try{startActivity(new Intent(action));}catch(Exception ex){startActivity(new Intent(Settings.ACTION_SETTINGS));}}
    private void openPackage(String pkg){try{Intent i=getPackageManager().getLaunchIntentForPackage(pkg);if(i==null){Toast.makeText(this,"Application not available. Use Google Play.",Toast.LENGTH_LONG).show();return;}startActivity(i);}catch(Exception ex){Toast.makeText(this,"Unable to open application.",Toast.LENGTH_LONG).show();}}
    private void share(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,baseline()+"\n\n"+appChanges());startActivity(Intent.createChooser(i,"Share baseline report"));}
    private void routeVoice(String route){
        switch(route){
            case "CHATGPT": openPackage("com.openai.chatgpt");break;
            case "WORKSPACE": new WorkspaceActivity.Launcher(this).show();break;
            case "GOOGLE_CHAT": new WorkspaceActivity.Launcher(this).openGoogleChat();break;
            case "DIAL": openDialer();break;
            case "SMS": composeSms();break;
            case "WHATSAPP": composeWhatsApp("com.whatsapp");break;
            case "SECURITY": refresh();break;
        }
    }

    // Original ENGAGE startup cue. No sampled or franchise soundtrack audio.
    private android.media.AudioTrack startupTrack;
    private final android.os.Handler startupHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable releaseStartup = this::stopStartupCue;
    private void stopStartupCue() {
        startupHandler.removeCallbacks(releaseStartup);
        android.media.AudioTrack track = startupTrack;
        startupTrack = null;
        if (track != null) {
            try { track.stop(); } catch (RuntimeException ignored) {}
            try { track.release(); } catch (RuntimeException ignored) {}
        }
    }
    private void playStartupCue() {
        stopStartupCue();
        try {
            if (!getPreferences(MODE_PRIVATE).getBoolean("startupCue", true)) return;
            android.media.AudioManager am = getSystemService(android.media.AudioManager.class);
            android.app.NotificationManager nm = getSystemService(android.app.NotificationManager.class);
            if (am == null || nm == null ||
                am.getRingerMode() != android.media.AudioManager.RINGER_MODE_NORMAL ||
                nm.getCurrentInterruptionFilter() != android.app.NotificationManager.INTERRUPTION_FILTER_ALL ||
                am.getStreamVolume(android.media.AudioManager.STREAM_SYSTEM) == 0) return;
            final int rate = 24000;
            // Original short retro sci-fi sting, not the Star Trek recording or score.
            short[] pcm = new short[rate * 3 / 4];
            double phase = 0.0;
            for (int i = 0; i < pcm.length; i++) {
                double t = (double)i / rate;
                double frequency = 390.0 + 770.0 * Math.exp(-t * 11.0);
                phase += 2.0 * Math.PI * frequency / rate;
                double envelope = Math.min(1.0, t / 0.012) * Math.exp(-t * 6.5)
                    * Math.min(1.0, (0.75 - t) / 0.08);
                double sample = envelope * (0.68 * Math.sin(phase)
                    + 0.22 * Math.sin(phase * 1.43) + 0.10 * Math.sin(phase * 2.08));
                pcm[i] = (short)Math.round(sample * 0.18 * 32767);
            }
            startupTrack = new android.media.AudioTrack.Builder()
                .setAudioAttributes(new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                .setAudioFormat(new android.media.AudioFormat.Builder()
                    .setSampleRate(rate).setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(pcm.length * 2)
                .setTransferMode(android.media.AudioTrack.MODE_STATIC).build();
            if (startupTrack.getState() != android.media.AudioTrack.STATE_INITIALIZED ||
                startupTrack.write(pcm, 0, pcm.length) != pcm.length) { stopStartupCue(); return; }
            startupTrack.setVolume(0.55f);
            startupTrack.play();
            startupHandler.postDelayed(releaseStartup, 950L);
        } catch (RuntimeException ignored) { stopStartupCue(); }
    }


    // Calibration is a local recognition check, not speaker authentication or model retraining.
    private TextView calibrationPrompt;
    private int calibrationStep;
    private boolean pendingCalibration;
    private static final String[] CALIBRATION = {
        "Engage", "Spock", "Curious Captain", "Vulcan",
        "Logic dictates", "Live long and prosper", "Open workspace", "Open Google Chat"
    };
    private static final String[] VOICE_LOCALES = {"en-US","en-GB","en-PH","en-AU","en-IN","en-CA"};
    private String voiceLocale(){return getPreferences(MODE_PRIVATE).getString("voiceLocale","en-US");}
    private void showVoiceLocale(){
        if(listening)return;
        String[] labels={"US English","British English","Philippine English","Australian English","Indian English","Canadian English"};
        int current=java.util.Arrays.asList(VOICE_LOCALES).indexOf(voiceLocale());
        new android.app.AlertDialog.Builder(this).setTitle("Choose recognition language")
            .setSingleChoiceItems(labels,current,(d,w)->{
                getPreferences(MODE_PRIVATE).edit().putString("voiceLocale",VOICE_LOCALES[w]).remove("calibrationPassed").apply();
                calibrationStep=0;updateCalibrationPrompt("");d.dismiss();
            }).setNegativeButton("Cancel",null).show();
    }
    private void updateCalibrationPrompt(String status){
        if(calibrationPrompt==null)return;
        boolean complete=calibrationStep>=CALIBRATION.length;
        String next=complete?"Calibration complete.":("Phrase "+(calibrationStep+1)+" of "+CALIBRATION.length+": "+CALIBRATION[calibrationStep]);
        calibrationPrompt.setText("Voice language: "+voiceLocale()+"\n"+(status.isEmpty()?"":status+"\n")+next);
    }
    private void startCalibration(){
        if(listening)return;
        if(calibrationStep>=CALIBRATION.length)calibrationStep=0;
        updateCalibrationPrompt("Read the phrase naturally.");
        listen(false,true);
    }

    private boolean pendingDictation;
    private void listen(){listen(false,false);}
    private void listen(boolean dictation){listen(dictation,false);}
    private void listen(boolean dictation,boolean calibrate){
        stopStartupCue();if(listening)return;
        pendingDictation=dictation;pendingCalibration=calibrate;
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},4);return;}
        if(!SpeechRecognizer.isOnDeviceRecognitionAvailable(this)){
            voiceStatus.setText("On-device speech unavailable. Open ChatGPT manually.");
            if(calibrate)updateCalibrationPrompt("On-device recognizer unavailable.");
            return;
        }
        stop();
        try{
            final SpeechRecognizer session=SpeechRecognizer.createOnDeviceSpeechRecognizer(this);
            recognizer=session;
            session.setRecognitionListener(new RecognitionListener(){
                public void onReadyForSpeech(Bundle b){voiceStatus.setText(calibrate?"Calibration listening…":dictation?"Listening for message…":"Listening for command…");if(calibrate)updateCalibrationPrompt("Listening…");}
                public void onBeginningOfSpeech(){}
                public void onRmsChanged(float f){if(calibrate&&recognizer==session)updateCalibrationPrompt("Microphone signal: "+String.format(Locale.US,"%.1f dB (relative)",f));}
                public void onBufferReceived(byte[] b){}
                public void onEndOfSpeech(){if(recognizer==session)voiceStatus.setText("Checking phrase…");}
                public void onPartialResults(Bundle b){}
                public void onEvent(int a,Bundle b){}
                public void onError(int code){if(recognizer!=session)return;stop();voiceStatus.setText("Recognition ended ("+code+"). Tap Listen once to retry.");if(calibrate)updateCalibrationPrompt("Recognition ended ("+code+"). Retry or choose another speech language.");}
                public void onResults(Bundle b){
                    if(recognizer!=session)return;
                    ArrayList<String> results=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String spoken=results==null||results.isEmpty()?"":results.get(0);
                    float[] scores=b.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
                    stop();
                    if(calibrate){
                        if(calibrationStep<CALIBRATION.length&&CommandRules.normalized(spoken).equals(CommandRules.normalized(CALIBRATION[calibrationStep]))){
                            calibrationStep++;
                            if(calibrationStep==CALIBRATION.length)getPreferences(MODE_PRIVATE).edit().putBoolean("calibrationPassed",true).apply();
                            updateCalibrationPrompt("Recognized correctly: "+spoken);
                        }else updateCalibrationPrompt("Heard: "+(spoken.isEmpty()?"(nothing)":spoken)+". Retry the same phrase.");
                        return;
                    }
                    if(dictation){
                        if(!spoken.isEmpty()){String current=smsBody.getText().toString();smsBody.setText(current.isEmpty()?spoken:current+" "+spoken);}
                        voiceStatus.setText(spoken.isEmpty()?"No message recognized.":"Message dictated. Review before sending.");return;
                    }
                    String route=CommandRules.route(spoken,getPreferences(MODE_PRIVATE).getString("phrase","Mr. Spock, analysis"));
                    if(scores!=null&&scores.length>0&&scores[0]>=0&&scores[0]<0.60f){voiceStatus.setText("Uncertain recognition: "+spoken+". Please repeat.");return;}
                    voiceStatus.setText(route.equals("NONE")?"No supported command recognized. Heard: "+spoken:"Command recognized; review destination before confirming.");
                    routeVoice(route);
                }
            });
            Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,voiceLocale());
            i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);
            if(Build.VERSION.SDK_INT>=33&&!calibrate&&!dictation){
                ArrayList<String> bias=new ArrayList<>(java.util.Arrays.asList("Engage","Spock","Vulcan","Captain","open ChatGPT","open workspace","open Google Chat","open dialer","prepare SMS","prepare WhatsApp","security analysis"));
                bias.add(getPreferences(MODE_PRIVATE).getString("phrase","Mr. Spock, analysis"));
                i.putStringArrayListExtra(RecognizerIntent.EXTRA_BIASING_STRINGS,bias);
            }
            listening=true;session.startListening(i);
        }catch(Exception ex){stop();voiceStatus.setText("Speech recognition unavailable.");if(calibrate)updateCalibrationPrompt("Speech recognition unavailable.");}
    }
    @Override public void onRequestPermissionsResult(int request,String[] perms,int[] results){
        super.onRequestPermissionsResult(request,perms,results);
        if(request==4&&results.length>0){
            if(results[0]==PackageManager.PERMISSION_GRANTED)listen(pendingDictation,pendingCalibration);
            else {voiceStatus.setText("Microphone permission denied. All manual controls remain available.");if(pendingCalibration)updateCalibrationPrompt("Microphone permission denied.");}
        }
    }
    private void stop(){listening=false;SpeechRecognizer old=recognizer;recognizer=null;if(old!=null){old.cancel();old.destroy();}if(voiceStatus!=null)voiceStatus.setText("Microphone idle.");}
    @Override protected void onPause(){stopStartupCue();super.onPause();}
    @Override protected void onStop(){stop();super.onStop();}
}
