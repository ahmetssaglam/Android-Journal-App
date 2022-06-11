package com.example.journal;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import data_classes.Journal;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {

    ArrayList<Journal> journalList;
    Context context;
    private SharedPreferences sharedPreferences;

    // PDF
    int pdfPageHeight = 1120;
    int pdfPagewidth = 792;
    private static final int PERMISSION_REQUEST_CODE = 200;
    // PDF

    public JournalAdapter(ArrayList<Journal> journalList, Context context) {
        this.journalList = journalList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.journal_item, parent, false);
        return new JournalAdapter.ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(JournalAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.itemView.setTag(position);
        Journal journal = journalList.get(position);

        holder.journal_title.setText(journal.getHeading());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (journal.getPassword() == null) {
                    JournalActivity.position = position;
                    Intent intent = new Intent(context, JournalActivity.class);
                    context.startActivity(intent);
                }
                else {
                    UnlockActivity.current_password = journal.getPassword();
                    UnlockActivity.pos = position;
                    Intent intent = new Intent(context, UnlockActivity.class);
                    context.startActivity(intent);
                }

            }
        });


    }


    @Override
    public int getItemCount() {
        return journalList.size();
    }


    private File generatePDF(Journal currentJournal) {
        // creating an object variable
        // for our PDF document.
        PdfDocument pdfDocument = new PdfDocument();

        // two variables for paint "paint" is used
        // for drawing shapes and we will use "title"
        // for adding text in our PDF file.
        Paint paint = new Paint();
        Paint title = new Paint();
        Paint content = new Paint();

        // we are adding page info to our PDF file
        // in which we will be passing our pageWidth,
        // pageHeight and number of pages and after that
        // we are calling it to create our PDF.
        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pdfPagewidth, pdfPageHeight, 1).create();

        // below line is used for setting
        // start page for our PDF file.
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

        // creating a variable for canvas
        // from our page of PDF.
        Canvas canvas = myPage.getCanvas();


        // below line is used for adding typeface for
        // our text which we will be adding in our PDF file.
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        content.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        // below line is used for setting text size
        // which we will be displaying in our PDF file.
        title.setTextSize(15);
        content.setTextSize(13);

        // below line is sued for setting color
        // of our text inside our PDF file.
        title.setColor(ContextCompat.getColor(context, R.color.dark_orange));

        // below line is used to draw text in our PDF file.
        // the first parameter is our text, second parameter
        // is position from start, third parameter is position from top
        // and then we are passing our variable of paint which is title.


        canvas.drawText(currentJournal.getHeading(), 210, 80, title);
        canvas.drawText(currentJournal.getDate(), 600, 80, content);


        // below line is used to draw our image on our PDF file.
        // the first parameter of our drawbitmap method is
        // our bitmap
        // second parameter is position from left
        // third parameter is position from top and last
        // one is our variable for paint.


        if (currentJournal.getImageArray() != null) {
            byte[] imageArray = currentJournal.getImageArray();
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
            Bitmap resized_bitmap = getResizedBitmap(imageBitmap, 300, 200);
            canvas.drawBitmap(resized_bitmap, 200, 150, paint);
        }


        // similarly we are creating another text and in this
        // we are aligning this text to center of our PDF file.
        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        title.setColor(ContextCompat.getColor(context, R.color.black));
        title.setTextSize(13);

        // below line is used for setting
        // our text to center of PDF.
        title.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Location => Latitude = " + currentJournal.getLatitude() + " Longitude = " + currentJournal.getLongitude(), 50, 400, title);
        canvas.drawText("Mood => " + currentJournal.getMood(), 50, 420, title);
//        System.out.println("Mood => " + currentJournal.getMood());
//        canvas.drawText(currentJournal.getMainContent(), 50, 460, title);


        TextPaint mTextPaint = new TextPaint();
        StaticLayout mTextLayout = new StaticLayout( currentJournal.getMainContent() ,mTextPaint, canvas.getWidth() - 100, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        canvas.save();
        int textX = 50;
        int textY = 460;
        canvas.translate(textX, textY);

        mTextLayout.draw(canvas);
        canvas.restore();
        // after adding all attributes to our
        // PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage);

        // below line is used to set the name of
        // our PDF file and its path.
        String pdf_name = currentJournal.getHeading().replaceAll("\\s+","_") + ".pdf";
        File file = new File(Environment.getExternalStorageDirectory(), pdf_name);
        pdfDocument.close();

        return file;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
//        bm.recycle();
        return resizedBitmap;
    }


    public void send_journal(String heading) {

//        String music_path = music.getAbsolutePath();
//        Uri uri = Uri.parse(music_path);

        String pdf_name = heading.replaceAll("\\s+","_") + ".pdf";
        @SuppressLint("SdCardPath") String path = "/sdcard/" + pdf_name;


        File pdfFile = new File(Environment.getExternalStorageDirectory(), pdf_name);
        System.out.println(pdfFile.getAbsolutePath());

        System.out.println("burda");
//        Uri uri = Uri.parse(pdfFile.getAbsolutePath());


//        Uri uri = Uri.fromFile(pdfFile);

        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", pdfFile);

        System.out.println(uri.toString());
        System.out.println("cikti");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(shareIntent, "Send Journal ..."));



    }

    public void delete_journal(int position) {
        String heading = journalList.get(position).getHeading();
        journalList.remove(position);
//        System.out.println("JOURNAL DELETED DELETED POSTION ==>" + position);
        save_list(journalList);
        Toast.makeText(context, "Journal " + heading + " Has Been Deleted !", Toast.LENGTH_SHORT).show();

    }

    public void save_list(ArrayList<Journal> journal_list) {

        sharedPreferences = context.getSharedPreferences(MainActivity.MyPrefs, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        journal_list.sort(Comparator.comparing(Journal::getDate));

        String json = gson.toJson(journal_list);
        editor.putString(MainActivity.UserKey, json);
        editor.apply();

        Toast.makeText(context, "Journal Has Removed !!", Toast.LENGTH_SHORT).show();

    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView journal_title;
        Button send_button;
        Button delete_button;
        ImageView journal_icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            journal_title = itemView.findViewById(R.id.tvJournalTitle);
            journal_title.setSelected(true);
            send_button = itemView.findViewById(R.id.btnSendJournal);
            delete_button = itemView.findViewById(R.id.btnDeleteJournal);
            journal_icon = itemView.findViewById(R.id.iwJournalIcon);


            send_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = (int) itemView.getTag();
                    String title = journal_title.getText().toString();
                    System.out.println(pos);

//                    Journal jnl = journalList.get(pos);
//                    File pdf = generatePDF(jnl);
                    send_journal(title);
                }
            });

            delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = (int) itemView.getTag();
                    delete_journal(pos);
                    notifyItemRemoved(pos);
                }
            });

        }
    }
}