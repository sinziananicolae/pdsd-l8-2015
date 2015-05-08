package ro.pub.cs.systems.pdsd.lab08.googlecloudmessaging.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ro.pub.cs.systems.pdsd.lab08.googlecloudmessaging.R;
import ro.pub.cs.systems.pdsd.lab08.googlecloudmessaging.controller.RegisteredDevicesAdapter;
import ro.pub.cs.systems.pdsd.lab08.googlecloudmessaging.general.Constants;
import ro.pub.cs.systems.pdsd.lab08.googlecloudmessaging.model.RegisteredDevice;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class RegisteredFragment extends Fragment {
	
	private Spinner registeredDevicesSpinner;
	private EditText messageEditText;
	
	private SendMessageButtonClickListener sendMessageButtonClickListener = new SendMessageButtonClickListener();
	private class SendMessageButtonClickListener implements Button.OnClickListener {
		
		@Override
		public void onClick(View view) {
			int position = registeredDevicesSpinner.getSelectedItemPosition();
			RegisteredDevice registeredDevice = (RegisteredDevice)registeredDevicesSpinner.getAdapter().getItem(position);
			
			if (registeredDevice == null || messageEditText.getText().toString().isEmpty()) {
				return;
			}
			
			new MessagePushExecutor(
					registeredDevice.getRegistrationId(),
					messageEditText.getText().toString()
					).start();
		}
		
	}
	
	private RefreshRegisteredDevicesListButtonClickListener refreshRegisteredDevicesListButtonClickListener = new RefreshRegisteredDevicesListButtonClickListener();
	private class RefreshRegisteredDevicesListButtonClickListener implements Button.OnClickListener {
		
		@Override
		public void onClick(View view) {
			new RegisteredDevicesFetcher().start();
		}
		
	}
	
	private class MessagePushExecutor extends Thread {
		
		private String registrationId;
		private String message;
		
		public MessagePushExecutor(String registrationId, String message) {
			this.registrationId = registrationId;
			this.message = message;
		}
		
		@Override
		public void run() {
			
			// TODO: exercise 8b
			// - create a HttpClient instance
			// - create a HttpPost instance according to the address of the message push service
			// - create a the list of request parameters
			//  * Constants.REGISTRATION_ID
			//  * Constants.MESSAGE
			// - encode the list of the request parameters (into an UrlEncodedFormEntity object), according to UTF-8
			// - attach the list of parameters to the HttpPost object
			// - create a ResponseHandler<String> instance
			// - execute the HttpPost request on the HttpClient and ResponseHandler and get response
			// - do nothing with the response
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(Constants.MESSAGE_PUSH_SERVICE_ADDRESS);
			List<NameValuePair> requestParameters = new ArrayList<NameValuePair>();        
			requestParameters.add(new BasicNameValuePair(Constants.REGISTRATION_ID, registrationId));
			requestParameters.add(new BasicNameValuePair(Constants.MESSAGE, message));
			UrlEncodedFormEntity encodedForm = null;
			try {
				encodedForm = new UrlEncodedFormEntity(requestParameters, HTTP.UTF_8);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			httpPost.setEntity(encodedForm);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			try {
				httpClient.execute(httpPost, responseHandler);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	private class RegisteredDevicesFetcher extends Thread {
		
		@Override
		public void run() {
			
			// TODO: exercise 8a
			// - create a HttpClient instance
			// - create a HttpGet instance according to the address of the registered devices service
			// - create a ResponseHandler<String> instance
			// - execute the HttpGet request on the HttpClient and ResponseHandler and get response
			// - instantiate the result (an ArrayList<RegisteredDevice> object)
			// - parse the result into a JSONArray object
			// - iterate over the records (instances of JSONObject) and create RegisteredDevice objects with the following information
			//  * Constants.ID
			//  * Constants.REGISTRATION_ID
			//  * Constants.USERNAME
			//  * Constants.EMAIL
			//  * Constants.TIMESTAMP
			// - add the registered device object to the result
			// - create an adapter (of type RegisteredDevicesAdapter) for the Spinner containing the registered devices
			// - attach the adapter to the Spinner
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(Constants.REGISTERED_DEVICES_SERVICE_ADDRESS);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			
			String response;
			try {
				response = httpClient.execute(httpGet, responseHandler);
				ArrayList<RegisteredDevice> list = new ArrayList<RegisteredDevice>();
				JSONArray jsonArray = new JSONArray(response);
				for (int i = 0; i < jsonArray.length(); i++){
					JSONObject currentObj = jsonArray.getJSONObject(i);
					list.add(new RegisteredDevice(currentObj.getInt(Constants.ID),
							currentObj.getString(Constants.REGISTRATION_ID),
							currentObj.getString(Constants.USERNAME),
							currentObj.getString(Constants.EMAIL),
							currentObj.getString(Constants.TIMESTAMP)));
				}
				final RegisteredDevicesAdapter myAdapter = new RegisteredDevicesAdapter(getActivity(), list);
				registeredDevicesSpinner.post(new Runnable() {
					
					@Override
					public void run() {
						registeredDevicesSpinner.setAdapter(myAdapter);
						
					}
				});
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_registered, parent, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registeredDevicesSpinner = (Spinner)getActivity().findViewById(R.id.registered_devices_spinner);
		messageEditText = (EditText)getActivity().findViewById(R.id.message_edit_text);
		
		Button sendMessageButton = (Button)getActivity().findViewById(R.id.send_message_button);
		sendMessageButton.setOnClickListener(sendMessageButtonClickListener);
		
		Button refreshRegisteredDevicesListButton = (Button)getActivity().findViewById(R.id.refresh_registered_devices_list_button);
		refreshRegisteredDevicesListButton.setOnClickListener(refreshRegisteredDevicesListButtonClickListener);
		new RegisteredDevicesFetcher().start();
	}

}
