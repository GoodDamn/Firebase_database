package com.spok.database;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ActivityDatabase  extends AppCompatActivity {

    private CheckBox checkbox_male, checkBox_female;
    private Button btn_add_data, btn_show_info;
    private ProgressBar progressBar;
    private EditText editText_name, editText_age, editText_number;

    private boolean isConnectedToTheInternet() // Проверка подключения к интернету.
    {
        return (((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null
        && ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo().isConnected());
    }

    // Вывод сообщения.
    private void MessageToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private String GetGender()
    {
        if (checkbox_male.isChecked()) return "Муж";
        else return "Жен";
    }

    private void hideKeyboard() // Метод, скрывающий клавиатуру.
    {
        View view = getCurrentFocus();
        if (view != null)
        {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void StartPrevActivity() // Метод, стартующий экран с входом в систему.
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        ActivityCompat.finishAffinity(this);
    }

    private void Enable(boolean isEnabled) // Вкл.\Выкл. элементы интерфейса.
    {
        if (!isEnabled) progressBar.setVisibility(View.VISIBLE);
        else progressBar.setVisibility(View.INVISIBLE);

        editText_number.setEnabled(isEnabled);
        editText_name.setEnabled(isEnabled);
        editText_age.setEnabled(isEnabled);
        checkBox_female.setEnabled(isEnabled);
        checkbox_male.setEnabled(isEnabled);
        btn_add_data.setEnabled(isEnabled);
        btn_show_info.setEnabled(isEnabled);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        final ListView listView_info = findViewById(R.id.listView_info);
        final ArrayList<String> arrayList = new ArrayList<>(),
            arrayList_full = new ArrayList<>();

        // Инициализация компонентов - начало.
        RelativeLayout imageView_logout = findViewById(R.id.relativelayout_logout);
        Button btn_clear_fields = findViewById(R.id.btn_clear_fields);

        checkbox_male = findViewById(R.id.checkbox_male);
        checkBox_female = findViewById(R.id.checkbox_female);
        progressBar = findViewById(R.id.progressbar_database);
        btn_add_data = findViewById(R.id.btn_add_data);
        btn_show_info = findViewById(R.id.btn_show_information);

        editText_name = findViewById(R.id.editText_typeData_name);
        editText_age = findViewById(R.id.editText_typeData_age);
        editText_number = findViewById(R.id.editText_typeData_number);
        // Инициализация компонентов - конец.

        btn_clear_fields.setOnClickListener(new View.OnClickListener() { // Кнопка "Очистить поля"
            @Override
            public void onClick(View v) {
                editText_age.setText("");
                editText_name.setText("");
                editText_number.setText("");
            }
        });

        ConstraintLayout constraintLayout = findViewById(R.id.database_constrLayout);
        constraintLayout.setOnClickListener(new View.OnClickListener() { // Если пользователь коснулся пустой области
            @Override
            public void onClick(View v) {
                hideKeyboard(); // Скрываем клавиатуру.
            }
        });

        checkBox_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Выбор мужского пола
                if (checkBox_female.isChecked()) checkBox_female.setChecked(true);
                else checkBox_female.setChecked(false);
                checkbox_male.setChecked(false);
            }
        });

        checkbox_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Выбор женского пола
                if (checkbox_male.isChecked()) checkbox_male.setChecked(true);
                else checkbox_male.setChecked(false);
                checkBox_female.setChecked(false);
            }
        });

        imageView_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartPrevActivity();
            }
        }); // Кнопка "Назад"

        btn_show_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Показать информацию о постояльцах
                Enable(false);
                // Проверка подключения к интернету.
                if (isConnectedToTheInternet())
                {
                    // Ссылка на базу данных в Firebase.
                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("nick"));
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long counter = Long.valueOf(snapshot.child("counter").getValue().toString()); //Последний id-шник пользователя.

                            arrayList.clear();
                            arrayList_full.clear();

                            for (long i = 1; i <= counter; i++)
                            {
                                // Заполняем ArrayList-ы данными.
                                arrayList.add(i + ". " + snapshot.child(i + "/name").getValue().toString());
                                arrayList_full.add(snapshot.child(i + "/name").getValue().toString()
                                + "|" + snapshot.child(i + "/age").getValue().toString()
                                + "|" + snapshot.child(i + "/number").getValue().toString()
                                + "|" + snapshot.child(i + "/gender").getValue().toString());
                            }

                            // Показываем в ListView.
                            listView_info.setAdapter(new ArrayAdapter<>(ActivityDatabase.this,
                                    android.R.layout.simple_list_item_1, arrayList));

                            Enable(true);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            MessageToast(error.getMessage());
                            Enable(true);
                        }
                    });
                }
                else
                {
                    Enable(true);
                    MessageToast("Нет подключения к интернету.");
                }
            }
        });


        listView_info.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { // Узнаём полную информацию о постояльце, выбрав его из списка.
                short i = 0;
                String word = "";
                // Парсим данные.
                for (char s : arrayList_full.get(position).toCharArray())
                {
                    if (s == '|')
                    {
                        String word2 = "";
                        for (char j : word.toCharArray())
                            if (j != '|') word2 += j;

                        switch (i)
                        {
                            case 0:
                                editText_name.setText(word2);
                                break;
                            case 1:
                                editText_age.setText(word2);
                                break;
                            case 2:
                                editText_number.setText(word2);
                                break;
                        }
                        word = "";
                        i++;
                    }
                    word += s;
                }
                if (word.equals("|Муж"))
                {
                    checkbox_male.setChecked(true);
                    checkBox_female.setChecked(false);
                }
                else
                {
                    checkbox_male.setChecked(false);
                    checkBox_female.setChecked(true);
                }

            }
        });



        btn_add_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Кнопка "Внести данные"
                Enable(false);
                // Проверка на пустые поля.
                if (!editText_name.getText().toString().isEmpty()
                && !editText_age.getText().toString().isEmpty()
                && !editText_number.getText().toString().isEmpty()
                && (checkBox_female.isChecked() || checkbox_male.isChecked()))
                {
                    // Проверка подключения к интернету.
                    if (isConnectedToTheInternet())
                    {
                        // Ссылка на базу данных в Firebase.
                        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("nick"));
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                long counter = Long.valueOf(snapshot.child("counter").getValue().toString());
                                databaseReference.child("counter").setValue(++counter); // Прибавляем счётчик постояльцев.
                                // К прибавленному значению, присваиваем информацию с полей ввода.
                                databaseReference.child(counter + "/name").setValue(editText_name.getText().toString());
                                databaseReference.child(counter + "/age").setValue(editText_age.getText().toString());
                                databaseReference.child(counter + "/number").setValue(editText_number.getText().toString());
                                databaseReference.child(counter + "/gender").setValue(GetGender());
                                Enable(true);
                                MessageToast("Данные внесены.");
                                editText_name.setText("");
                                editText_age.setText("");
                                editText_number.setText("");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                MessageToast(error.getMessage());
                                Enable(true);
                            }
                        });
                    }
                    else
                    {
                        Enable(true);
                        MessageToast("Нет подключения к интернету.");
                    }
                }
                else
                {
                    Enable(true);
                    MessageToast("Не все поля заполнены!");
                }
            }
        });
    }

    private long currentMillis;
    private Toast toast_exit;
    @Override
    public void onBackPressed() // Если пользователь нажимает на системную кнопку "Назад".
    {
        if (currentMillis + 2000 > System.currentTimeMillis())
        {
            toast_exit.cancel();
            moveTaskToBack(true);
            System.exit(1);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        else
        {
            toast_exit = Toast.makeText(this, "Нажмите ещё раз для выхода.", Toast.LENGTH_LONG);
            toast_exit.show();
        }
        currentMillis = System.currentTimeMillis();
    }
}
