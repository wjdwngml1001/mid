// MainActivity.java
public class MainActivity extends AppCompatActivity {
    TextView textView;
    String site_url = "http://10.0.2.2:8000";
    LoadImages taskDownload;

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
    }

    public void onClickDownload(View v){
        if (taskDownload != null && taskDownload.getStatus()==AsyncTask.Status.RUNNING){
            taskDownload.cancel(true);
        }
        taskDownload = new LoadImages();
        taskDownload.execute(site_url + "/api_root/Post/");
        Toast.makeText(this,"Download",Toast.LENGTH_LONG).show();
    }

    public void onClickUpload(View v){
        // (7.1) Hard coding: 로컬의 예시 이미지 파일 경로/이름 하드코딩 업로드
        // 실제 단말에서는 앱 내부 리소스나 Downloads의 샘플로 교체
        new Thread(() -> {
            try {
                String boundary = "----AndroidFormBoundary" + System.currentTimeMillis();
                URL url = new URL(site_url + "/api_root/Post/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);

                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                // 텍스트 필드(title, text, author)
                writeFormField(out, boundary, "title", "테스트업로드");
                writeFormField(out, boundary, "text", "하드코딩 업로드");
                writeFormField(out, boundary, "author", "1");
                // 파일 필드(image)
                // ★ 단말 내 샘플사진 경로를 사용(예: /sdcard/Download/sample.jpg)
                File file = new File("/sdcard/Download/sample.jpg");
                writeFileField(out, boundary, "image", "sample.jpg", "image/jpeg", file);

                // 종료 boundary
                out.writeBytes("--" + boundary + "--\r\n");
                out.flush(); out.close();

                int code = conn.getResponseCode();
                runOnUiThread(() ->
                        Toast.makeText(this,"Upload resp: "+code,Toast.LENGTH_LONG).show()
                );
            } catch(Exception e){
                runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void writeFormField(DataOutputStream out, String boundary, String name, String value) throws IOException {
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        out.writeBytes(value + "\r\n");
    }

    private void writeFileField(DataOutputStream out, String boundary, String name, String filename, String mime, File file) throws IOException {
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n");
        out.writeBytes("Content-Type: " + mime + "\r\n\r\n");

        FileInputStream fis = new FileInputStream(file);
        byte[] buf = new byte[4096]; int len;
        while((len = fis.read(buf)) != -1){ out.write(buf, 0, len); }
        fis.close();
        out.writeBytes("\r\n");
    }

    private class LoadImages extends AsyncTask<String, Integer, List<Bitmap>> {
        @Override protected List<Bitmap> doInBackground(String... urls){
            List<Bitmap> images = new ArrayList<>();
            try{
                URL api = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) api.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000); conn.setReadTimeout(3000);
                if (conn.getResponseCode()==HttpURLConnection.HTTP_OK){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder(); String line;
                    while((line = br.readLine())!=null) sb.append(line);
                    JSONArray arr = new JSONArray(sb.toString());
                    for (int i=0;i<arr.length();i++){
                        JSONObject o = arr.getJSONObject(i);
                        String imageUrl = o.optString("image","");
                        if (!imageUrl.isEmpty()){
                            URL img = new URL(imageUrl);
                            HttpURLConnection ic = (HttpURLConnection) img.openConnection();
                            InputStream is = ic.getInputStream();
                            images.add(BitmapFactory.decodeStream(is));
                            is.close();
                        }
                    }
                }
            } catch(Exception e){ e.printStackTrace(); }
            return images;
        }
        @Override protected void onPostExecute(List<Bitmap> imgs){
            if (imgs.isEmpty()){ textView.setText("불러올 이미지가 없습니다."); }
            else{
                textView.setText("이미지 로드 성공!");
                RecyclerView rv = findViewById(R.id.recyclerView);
                rv.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                rv.setAdapter(new ImageAdapter(imgs));
            }
        }
    }
}
