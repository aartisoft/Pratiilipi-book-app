package com.pratilipi.android.pratilipi_and;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Locale;

public class LanguageSelectionActivity extends Activity {

    private static final String LOG_TAG = LanguageSelectionActivity.class.getSimpleName();

    private String mSelectedLanguage ;
    public static final String SELECTED_LANGUAGE = "selected_language";

    public static final String SELECTED_LANGUAGE_GUJARATI = "gu";
    public static final String SELECTED_LANGUAGE_HINDI = "hi";
    public static final String SELECTED_LANGUAGE_TAMIL = "ta";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);
        TextView myTextView=(TextView)findViewById(R.id.radio_gujrati);
        Typeface typeFace= Typeface.createFromAsset(getAssets(), "fonts/gujarati.ttf");
        myTextView.setTypeface(typeFace);
    }

    public void goSelected(View view) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SELECTED_LANGUAGE, mSelectedLanguage);
        editor.commit();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        Button goButton = (Button)findViewById(R.id.goButton);
        goButton.setVisibility(View.VISIBLE);

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_tamil: {
                if (checked)
                    Locale.setDefault(new Locale(SELECTED_LANGUAGE_HINDI));
                updateLanguage(this, SELECTED_LANGUAGE_TAMIL);
                getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                        .putString("selectedLanguage", "ta").commit();
                break;
            }
            case R.id.radio_hindi: {
                if (checked)
                    Locale.setDefault(new Locale(SELECTED_LANGUAGE_HINDI));
                updateLanguage(this, SELECTED_LANGUAGE_HINDI);
                getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                        .putString("selectedLanguage", "hi").commit();
                break;
            }
            case R.id.radio_gujrati: {
                if (checked)
                    Locale.setDefault(new Locale(SELECTED_LANGUAGE_GUJARATI));
                updateLanguage(this, SELECTED_LANGUAGE_GUJARATI);
                getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                        .putString("selectedLanguage", "gu").commit();
                break;
            }
        }
    }

    public void updateLanguage(Context context, String idioma) {
        mSelectedLanguage = idioma;
        if (!"".equals(idioma)) {
            Locale locale = new Locale(idioma);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            context.getResources().updateConfiguration(config, null);

            setDefaultFont(this,"DEFAULT","fonts/gujarati.ttf");
        }
    }

    // Typeface.createFromAsset(getAssets(), "fonts/gujarati.ttf")
    public static void setDefaultFont(Context context,
                                      String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(),
                fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    protected static void replaceFont(String staticTypefaceFieldName,
                                      final Typeface newTypeface) {
        try {
            Field staticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);

            staticField = Typeface.class
                    .getDeclaredField("DEFAULT_BOLD");
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);

            staticField = Typeface.class
                    .getDeclaredField("DEFAULT_BOLD");
            staticField.setAccessible(true);

            staticField.set(null, newTypeface);
            staticField = Typeface.class
                    .getDeclaredField("MONOSPACE");
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);

            staticField = Typeface.class
                    .getDeclaredField("SANS_SERIF");
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);

            staticField = Typeface.class
                    .getDeclaredField("SERIF");
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
