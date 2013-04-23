package com.softskills.opengl2dgraph;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

	private static final int SAMPLES_IN_SCREEN = 1000;
	private GLSurfaceView mGraphSurfaceView;
	private FrameLayout mControlLayer;
	private GraphRenderer mGraphRenderer;
	private SamplesBuffer mSamplesBuffer[];
	private Button mStartStopButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mControlLayer = (FrameLayout)findViewById(R.id.control_layer);
		
		mGraphSurfaceView = new GLSurfaceView(this);
		
		mGraphSurfaceView.setEGLConfigChooser(false);
		
		mControlLayer.addView(mGraphSurfaceView);
		
		mSamplesBuffer=new SamplesBuffer[5];
		
		
		for(int i=0;i<5;i++)
		{
			mSamplesBuffer[i] = new SamplesBuffer(SAMPLES_IN_SCREEN, true);
		}
		
		mGraphRenderer = new GraphRenderer(this,mSamplesBuffer);
		
		mGraphSurfaceView.setRenderer(mGraphRenderer);
		
		mStartStopButton = (Button)findViewById(R.id.button1);
		
		mStartStopButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startStopData();
				
			}

		
		});
	}

	boolean mDataStarted = false;
	
	private void startStopData() {
		if(mDataStarted)
		{
			mStartStopButton.setBackgroundColor(Color.GREEN);
			mStartStopButton.setText("Start");
			mDataStarted = false;
		}
		else
		{
			mStartStopButton.setBackgroundColor(Color.RED);
			mDataStarted = true;
			mStartStopButton.setText("Stop");
			FakeDataGenerator fdg = new FakeDataGenerator();
			fdg.start();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	Handler mUpdateHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			mGraphRenderer.updateData(currentTime);
			mGraphSurfaceView.invalidate();
			mGraphSurfaceView.requestRender();
			super.handleMessage(msg);
		}
		
	};
	
	long currentTime = 0;
	void updateData()
	{
		for(int j=0;j<5;j++)
		{
				double sinValue = Math.sin((double)(((currentTime/4)%360+j*45)*Math.PI/180.));
			
				//Log.i("Arik","SinValue:"+sinValue);
				short sampleValue = (short)(80.*sinValue);
				mSamplesBuffer[j].addSample(sampleValue, currentTime);
				mUpdateHandler.sendEmptyMessage(0);
		}
		currentTime+=1;
	}

	class FakeDataGenerator extends Thread
	{

		@Override
		public void run() {
			while(mDataStarted)
			{
				updateData();
				
				try {
					sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			super.run();
		}
		
	}
	
}
