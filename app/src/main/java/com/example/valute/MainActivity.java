package com.example.valute;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.service.controls.templates.ControlButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText editableText, notEditableText;
    private Document doc;       //Сюда считываем веб-страницу
    private Thread secThread;   //Второстепенный поток
    private Runnable runnable;
    private Spinner firstValute;
    private String[] valutesNames;
    private double[] valutesDosh;
    private int[] valutesMultiple;
    private ArrayAdapter<String> valutesAdapter;
    private int valuteId;

    public MainActivity() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        editableText = findViewById(R.id.editableText);
        notEditableText = findViewById(R.id.notEditableText);
        notEditableText.setFocusable(false);
        notEditableText.setLongClickable(false);
        firstValute = findViewById(R.id.spinner);

        init();

        try {
            Thread.sleep(1000);

        }catch (Exception e){
            e.printStackTrace();
        }

        //Адаптер данных для связывания содержимого из набора данных для каждого списка
        valutesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valutesNames);
        valutesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        firstValute.setAdapter(valutesAdapter);

        editableText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editableText.getText().length() == 0) {
                    return;
                }
                double result = Double.parseDouble(editableText.getText().toString())
                        * valutesMultiple[valuteId] / valutesDosh[valuteId];    //Считаем курс
                notEditableText.setText("" + Math.round(result*100.0)/100.0);   //Вывод в поле Not Editable Text
            }
        });

                //Получаем объект и его содержимое
                firstValute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    //Если выбрали другую валюту, то пересчитываем курс и выводим на экран
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (editableText.getText().length() == 0)
                            return;
                        double result = Double.parseDouble(editableText.getText().toString()) * valutesMultiple[i] / valutesDosh[i];
                        notEditableText.setText("" + Math.round(result*100.0)/100.0);
                        valuteId = i;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
    }

    protected void getWeb(){
        try {
            //Загружаем страницу по ссылке в переменную doc
            doc = Jsoup.connect("https://cbr.ru/currency_base/daily/").get();
            Elements tables = doc.getElementsByTag("tbody");    //Курсы валют находятся в "этикетке" tbody
            Element our_table = tables.get(0);  //Первая из таблиц с тэгом tbody
            Elements all_valutes = our_table.children();    //Достаем элементы из таблицы
            valutesNames = new String[all_valutes.size()-1];
            valutesDosh = new double[all_valutes.size()-1];
            valutesMultiple = new int[all_valutes.size()-1];
            for (int i = 1;i < all_valutes.size(); i++) {   //Считываем элементы таблицы (валюты) и их содержимое (курс)
                Element dollar = all_valutes.get(i);
                Elements dollar_elements = dollar.children();
                String parsing = dollar_elements.get(4).text();
                parsing = parsing.replace(',', '.');
                valutesDosh[i-1] = Math.round(Double.parseDouble(parsing)*100.0)/100.0;
                valutesNames[i-1] = dollar_elements.get(3).text();
                valutesMultiple[i-1] = Integer.parseInt(dollar_elements.get(2).text().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private  void init() {  //В этой функции запускается второстепенный поток
        runnable = new Runnable() {
            @Override
            public void run() {
                getWeb();
            }
        };
        secThread = new Thread(runnable);
        secThread.start();
    }
}