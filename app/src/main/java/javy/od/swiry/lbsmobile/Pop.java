package javy.od.swiry.lbsmobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;

public class Pop extends Activity {
    private ListView poplist;
    private ArrayList<String> categories;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_category);
        DisplayMetrics dm =new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*.9),(int)(height*0.8));
        poplist = findViewById(R.id.poplist);
        poplist.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        categories = new ArrayList<>
                (Arrays.asList(getResources().getStringArray(R.array.Categories)));
        categories.add(0,"Wszystkie kategorie");
        CustomAdapter customAdapter = new CustomAdapter();
        poplist.setAdapter(customAdapter);
        popHandler();

        TextView cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenuActivity.searchCategory = null;
                finish();
            }
        });
    }
    public void popHandler()
    {
        poplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int i, long l) {
                TextView textView = v.findViewById(R.id.text);
                String category = (String)textView.getText();
                if(category.equals("Wszystkie kategorie")) {
                    MainMenuActivity.searchCategory = null;
                } else {
                    MainMenuActivity.searchCategory = category;
                }
                finish();
            }
        });
    }

    public class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.category_adapter, null);
            TextView text = convertView.findViewById(R.id.text);
            ImageView image = convertView.findViewById(R.id.picked);
            ImageView background = convertView.findViewById(R.id.background);

            text.setText(categories.get(position));
            if(MainMenuActivity.searchCategory != null ) {
                if (categories.get(position).equals(MainMenuActivity.searchCategory)) {
                    image.setVisibility(View.VISIBLE);
                    background.setVisibility(View.VISIBLE);
                }
            } else if (categories.get(position).equals("Wszystkie kategorie")){
                image.setVisibility(View.VISIBLE);
                background.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }
}
