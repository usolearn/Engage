package org.spock.companion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

/** User-initiated handoffs only. No cloud credentials, tokens, background access or automatic writes. */
final class WorkspaceActivity {
    static final class Launcher {
        private final Activity a;
        private static final String[][] APPS = {
            {"Outlook (primary mail/calendar)","com.microsoft.office.outlook","https://outlook.live.com/"},
            {"OneDrive (primary files)","com.microsoft.skydrive","https://onedrive.live.com/"},
            {"Microsoft 365","com.microsoft.office.officehubrow","https://www.microsoft365.com/"},
            {"Word","com.microsoft.office.word","https://www.microsoft365.com/launch/word"},
            {"Excel","com.microsoft.office.excel","https://www.microsoft365.com/launch/excel"},
            {"PowerPoint","com.microsoft.office.powerpoint","https://www.microsoft365.com/launch/powerpoint"},
            {"OneNote","com.microsoft.office.onenote","https://www.onenote.com/"},
            {"Teams","com.microsoft.teams","https://teams.microsoft.com/"},
            {"SharePoint / organization files","com.microsoft.sharepoint","https://www.microsoft365.com/"},
            {"Google Chat","com.google.android.apps.dynamite","https://chat.google.com/"},
            {"Gmail","com.google.android.gm","https://mail.google.com/"},
            {"Google Calendar","com.google.android.calendar","https://calendar.google.com/"},
            {"Google Drive","com.google.android.apps.docs","https://drive.google.com/"},
            {"Google Docs","com.google.android.apps.docs.editors.docs","https://docs.google.com/document/"},
            {"Google Sheets","com.google.android.apps.docs.editors.sheets","https://docs.google.com/spreadsheets/"},
            {"Google Slides","com.google.android.apps.docs.editors.slides","https://docs.google.com/presentation/"},
            {"Google Contacts","com.google.android.contacts","https://contacts.google.com/"},
            {"Google Meet","com.google.android.apps.tachyon","https://meet.google.com/"}
        };
        Launcher(Activity a){this.a=a;}
        void show(){String[] choices=new String[APPS.length+3]; choices[0]="Prepare email — Outlook / Gmail";choices[1]="Prepare calendar event — Outlook preferred";choices[2]="Share text with a document or storage app";for(int i=0;i<APPS.length;i++)choices[i+3]=APPS[i][0];new AlertDialog.Builder(a).setTitle("ENGAGE workspace").setItems(choices,(d,n)->{if(n==0)email();else if(n==1)calendar();else if(n==2)shareText();else open(APPS[n-3]);}).setNegativeButton("Close",null).show();}
        void openGoogleChat(){open(new String[]{"Google Chat","com.google.android.apps.dynamite","https://chat.google.com/"});}
        private void toast(String text){Toast.makeText(a,text,Toast.LENGTH_LONG).show();}
        private void open(String[] row){try{Intent i=a.getPackageManager().getLaunchIntentForPackage(row[1]);if(i!=null){a.startActivity(i);return;}}catch(RuntimeException ignored){}new AlertDialog.Builder(a).setTitle(row[0]).setMessage("App unavailable. Open its official website instead?").setNegativeButton("Cancel",null).setPositiveButton("Open website",(d,w)->launch(new Intent(Intent.ACTION_VIEW,Uri.parse(row[2])),"No browser available.")).show();}
        private boolean launch(Intent i,String failure){try{a.startActivity(i);return true;}catch(RuntimeException e){toast(failure);return false;}}
        private LinearLayout form(){LinearLayout l=new LinearLayout(a);l.setOrientation(LinearLayout.VERTICAL);int p=(int)(16*a.getResources().getDisplayMetrics().density);l.setPadding(p,0,p,0);return l;}
        private EditText field(LinearLayout l,String hint,int type){EditText e=new EditText(a);e.setHint(hint);e.setInputType(type);e.setSingleLine((type & InputType.TYPE_TEXT_FLAG_MULTI_LINE)==0);e.setSaveEnabled(false);l.addView(e,new LinearLayout.LayoutParams(-1,-2));return e;}
        private String value(EditText e){return e.getText().toString();}
        private void email(){LinearLayout l=form();EditText to=field(l,"Recipient email",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS),subject=field(l,"Subject",InputType.TYPE_CLASS_TEXT),body=field(l,"Message",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);AlertDialog d=new AlertDialog.Builder(a).setTitle("Prepare email — review before sending").setView(l).setNegativeButton("Cancel",null).setNeutralButton("Gmail",null).setPositiveButton("Outlook",null).create();d.setOnShowListener(x->{d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->composeMail(d,to,subject,body,"com.microsoft.office.outlook"));d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->composeMail(d,to,subject,body,"com.google.android.gm"));});d.show();}
        private void composeMail(AlertDialog d,EditText to,EditText subject,EditText body,String pkg){String address=value(to).trim();if(!android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches()||address.contains(",")||address.contains(";")||address.contains("\n")||address.contains("\r")){to.setError("Enter one valid email address");return;}String uri="mailto:"+Uri.encode(address,"@")+"?subject="+Uri.encode(value(subject))+"&body="+Uri.encode(value(body));Intent i=new Intent(Intent.ACTION_SENDTO,Uri.parse(uri)).setPackage(pkg);i.putExtra(Intent.EXTRA_EMAIL,new String[]{address});i.putExtra(Intent.EXTRA_SUBJECT,value(subject));i.putExtra(Intent.EXTRA_TEXT,value(body));if(launch(i,"Selected email app cannot open the draft. Open it manually; nothing was sent."))d.dismiss();}
        private void calendar(){LinearLayout l=form();EditText title=field(l,"Event title",InputType.TYPE_CLASS_TEXT),start=field(l,"Start (YYYY-MM-DD HH:mm)",InputType.TYPE_CLASS_TEXT),end=field(l,"End (YYYY-MM-DD HH:mm)",InputType.TYPE_CLASS_TEXT),place=field(l,"Location (optional)",InputType.TYPE_CLASS_TEXT),notes=field(l,"Notes (optional)",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);ZoneId zone=ZoneId.systemDefault();DateTimeFormatter fmt=DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm",Locale.US).withResolverStyle(ResolverStyle.STRICT);LocalDateTime next=LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);start.setText(next.format(fmt));end.setText(next.plusHours(1).format(fmt));AlertDialog d=new AlertDialog.Builder(a).setTitle("Event draft · "+zone.getId()).setMessage("Outlook is preferred. Verify date, timezone and calendar in the destination before saving.").setView(l).setNegativeButton("Cancel",null).setPositiveButton("Outlook",null).setNeutralButton("Google",null).create();d.setOnShowListener(x->{d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->event(d,title,start,end,place,notes,fmt,zone,"com.microsoft.office.outlook"));d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->event(d,title,start,end,place,notes,fmt,zone,"com.google.android.calendar"));});d.show();}
        private void event(AlertDialog d,EditText title,EditText start,EditText end,EditText place,EditText notes,DateTimeFormatter fmt,ZoneId zone,String pkg){if(value(title).trim().isEmpty()){title.setError("Enter a title");return;}long begin,finish;try{LocalDateTime b=LocalDateTime.parse(value(start).trim(),fmt),e=LocalDateTime.parse(value(end).trim(),fmt);if(zone.getRules().getValidOffsets(b).size()!=1||zone.getRules().getValidOffsets(e).size()!=1){toast("Ambiguous or nonexistent local time. Choose a different time.");return;}begin=b.atZone(zone).toInstant().toEpochMilli();finish=e.atZone(zone).toInstant().toEpochMilli();}catch(RuntimeException ex){toast("Use YYYY-MM-DD HH:mm (24-hour time).");return;}if(finish<=begin){end.setError("End must be after start");return;}Intent i=new Intent(Intent.ACTION_INSERT,CalendarContract.Events.CONTENT_URI).setPackage(pkg);i.putExtra(CalendarContract.Events.TITLE,value(title));i.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,begin);i.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,finish);i.putExtra(CalendarContract.Events.EVENT_LOCATION,value(place));i.putExtra(CalendarContract.Events.DESCRIPTION,value(notes));if(launch(i,"Selected calendar app does not support event handoff. Open its calendar manually; nothing saved."))d.dismiss();}
        private void shareText(){LinearLayout l=form();EditText t=field(l,"Text to share",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);AlertDialog d=new AlertDialog.Builder(a).setTitle("Share text — choose destination").setView(l).setNegativeButton("Cancel",null).setPositiveButton("Review share options",null).create();d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(value(t).trim().isEmpty()){t.setError("Enter text");return;}Intent i=new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,value(t));if(launch(Intent.createChooser(i,"Choose destination"),"No compatible destination."))d.dismiss();}));d.show();}
    }
}
