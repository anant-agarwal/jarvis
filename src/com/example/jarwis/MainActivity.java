package com.example.jarwis;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
public class MainActivity extends Activity implements OnInitListener{

	protected static final int RESULT_SPEECH = 1;
    private ImageButton btnSpeak;
    private TextView txtText;
    private static final int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech tts;
	private int TtsInit = 0;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		
		txtText = (TextView) findViewById(R.id.txtText);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
           	@Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    txtText.setText("");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),"Opps! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	protected String PostReq(String s){
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://122.167.251.52:8080/wolf.php");
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add( new BasicNameValuePair("t",s) );
		String Ans = "I didnot understand";
		try {
			post.setEntity(new UrlEncodedFormEntity( pairs ));
			HttpResponse response = client.execute( post );
			Ans = EntityUtils.toString(response.getEntity() );
		} catch (UnsupportedEncodingException e) {
		//	Toast t = Toast.makeText(getApplicationContext(), "unable to encode url",Toast.LENGTH_SHORT);
		//	t.show();
			Log.w("encoding e",e.getMessage());
			e.printStackTrace();
	//	} catch(ClientProtocolException c){
	//		Log.w("clientprotocol e",c.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.w("Io e",e.getMessage());
			e.printStackTrace();
		}
		//String Ans = "blah";
		return Ans;
	}
	protected String Wolf(String s)
	{
		HttpClient client = new DefaultHttpClient();
		s = URLEncoder.encode(s);
		Log.w("param",s);
		String Url = "http://api.wolframalpha.com/v2/query?appid=5R6UAG-4XKKTX7WV6&input="+s;
		
		Log.w("httpurl",Url);
		String Ans="";
		HttpGet get = new HttpGet(Url);
		try
		{	HttpResponse response = client.execute( get );
			Ans = EntityUtils.toString(response.getEntity());
		}catch(IOException e){}
		
		System.out.println(Ans);
		//String pattern = "(<plaintext.*?>)(.+?)(</plaintext>)";
		//String updated = Ans.replaceAll(pattern, "$2"); 
		
		/*String[] updated = Ans.split("<|>");
		int len = updated.length;
		for(int i=0; i<len;i++)
		{
			if(updated[i]=="plaintext")
				return updated[i+1];
		}
		return updated[0];*/
		XPathFactory xpathFactory = XPathFactory.newInstance();
	    XPath xpath = xpathFactory.newXPath();
	    InputSource source = new InputSource(new StringReader(Ans));
	    InputSource source1 = new InputSource(new StringReader(Ans));
	    String msg = "";
	    try {
			
	    	NodeList nl = (NodeList) xpath.compile("//plaintext").evaluate(source, XPathConstants.NODESET);
	    	int ln=nl.getLength();
	    	msg = xpath.evaluate("//pod[@title='Result']/subpod/plaintext", source1);
	    	if(msg!=""){ln=0; System.out.print(msg);}
	    	for(int i=0;i<ln && i<4;i++)
	    	{
	    		String tmp="";
	    		if(nl.item(i)!=null && nl.item(i).getFirstChild()!=null)
	    		{
	    			tmp= nl.item(i).getFirstChild().getNodeValue();
	    			msg+=" ... ... "+tmp;
	    			System.out.print(tmp);
	    		}
	    	}
	    } catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    msg =  msg.replaceAll("\\|", " .. ");
	    Log.w("final",msg);
	    if(msg=="")msg="Sorry";
	    return msg;
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        switch (requestCode) {
        case RESULT_SPEECH: {
            if (resultCode == RESULT_OK && null != data) {
 
                ArrayList<String> text = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                txtText.setText(text.get(0));
                String Ans = Wolf( text.get(0) );
                txtText.setText(Ans);
                if(TtsInit == 1)
                tts.speak(Ans, TextToSpeech.QUEUE_ADD, null);
            }
            break;
        }
        case MY_DATA_CHECK_CODE: { 
        	if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
        		tts = new TextToSpeech(this, this);
        	}
        	else {
        		Intent installIntent = new Intent();
        		installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        		startActivity(installIntent);
        	}
        	break;
        } 
 
        }
    }
	 @Override
	 public void onInit(int status) {       
		if (status == TextToSpeech.SUCCESS) {
			TtsInit = 1;
			Toast.makeText(MainActivity.this,"Text-To-Speech engine is initialized", Toast.LENGTH_LONG).show();
	       
		}
	        else if (status == TextToSpeech.ERROR) {
	             Toast.makeText(MainActivity.this,"Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
	        }
	     }
/*
	 public void onPause()
	 {
		 tts.shutdown();
	 }
	 public void onResume()
	 {
		tts = new TextToSpeech(this,this); 
	 } */
}
