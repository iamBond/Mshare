package com.readboy.multshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.sso.UMSsoHandler;
import com.readboy.multshare.Share;


public class MainActivity extends Activity {

	
	private Share share = null;
	UMSocialService mController = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//初始化友盟分享平台控制器
		mController = UMServiceFactory.getUMSocialService("myshare");
		//初始化分享平台
		share = new Share(mController, MainActivity.this);
		//设置qq，qqzone，sina的分享内容
		mController.setShareContent("这是文字分享内容");
		//设置微信分享的内容
		share.setWXShareContent("这是微信分享的内容");
		//弹出分享面板
		findViewById(R.id.button).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				share.showCustomUI(v);
			}
		});
		
		
		
	}

	
		
	
		

	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(
				resultCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	

}