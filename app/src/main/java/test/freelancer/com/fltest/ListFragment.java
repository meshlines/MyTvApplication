package test.freelancer.com.fltest;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * List that displays the TV Programmes
 */
public class ListFragment extends Fragment {

    JSONObject json;
    ListView view;
    int channelCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = (ListView) inflater.inflate(R.layout.fragment_list, container, false);

        // eurgh, damn android.os.NeworkOnMainThreadException - so pesky!
        // stackoverflow told me to do this:
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // download the program guide
        //String JsonResponse = connect("http://whatsbeef.net/wabz/guide.php?start=0");
        new LoadTvDetails().execute("0");

        /*try {
            Log.d("MESHA", "insideTry");
            //JSONObject json = new JSONObject(JsonResponse);
            json = new JSONObject(latestContent);
            view.setAdapter(new ListAdapter(json.getJSONArray("results")));

        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        return view;
    }

    public static String connect(String url) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                String result = convertStreamToString(instream);
                instream.close();
                return result;
            }
        } catch (IOException e) {
        }
        return null;
    }

    public class LoadTvDetails extends AsyncTask<String, Void, Void> {
        String myUrl = "http://whatsbeef.net/wabz/guide.php?start=";
        FileOutputStream outputStream;
        //JSONObject dummyObj;


        @Override
        protected Void doInBackground(String... strings) {
            myUrl = myUrl + strings[0];
            String latestContent = connect(myUrl);
            try {
                outputStream = getActivity().openFileOutput("details.txt", Context.MODE_PRIVATE);
                json = new JSONObject(latestContent);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result){
            super.onPostExecute(result);
            try {
                view.setAdapter(new ListAdapter(json.getJSONArray("results")));
                channelCount = Integer.parseInt(json.getString("count"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }

    private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public class ListAdapter extends BaseAdapter {

        JSONArray array;
        private LayoutInflater inflater;

        public ListAdapter(JSONArray response) {
            inflater = getActivity().getWindow().getLayoutInflater();
            array = response;
        }

        @Override
        public int getCount() {
            return channelCount;
        }

        @Override
        public JSONObject getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row;
            row = inflater.inflate(R.layout.tv_details_layout, parent, false);

            //LinearLayout layout = new LinearLayout(getActivity());
            //layout.setOrientation(LinearLayout.VERTICAL);

            try {
                TextView name = (TextView) row.findViewById(R.id.lbl_name);
                name.setText(array.getJSONObject(position).getString("name"));
                TextView start = (TextView) row.findViewById(R.id.lbl_start);
                start.setText(array.getJSONObject(position).getString("start_time"));
                TextView end = (TextView) row.findViewById(R.id.lbl_end);
                end.setText(array.getJSONObject(position).getString("end_time"));
                TextView channel = (TextView) row.findViewById(R.id.lbl_channel);
                channel.setText(array.getJSONObject(position).getString("channel"));
                TextView rating = (TextView) row.findViewById(R.id.lbl_rating);
                rating.setText(array.getJSONObject(position).getString("rating"));

                //layout.addView(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return row;
        }
    }
}
