package com.readboy.multshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.readboy.multshare.*;
import com.tencent.a.b.q;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;


public class MainActivity extends Activity {

	// 微信appid
	private final static String APP_ID = "wx5c734d1f6b8e6db3";

	private IWXAPI api = null;

	private View viewLayout = null;
	private PopupWindow popWin = null;
	private ImageButton buttonWeibo = null;
	private ImageButton buttonQQ = null;
	private ImageButton buttonQQZone = null;
	private ImageButton buttonWeichat = null;

	// 待拼接的三张图片
	private Bitmap bitmap_up = null;
	private Bitmap bitmap_down = null;
	private Bitmap bitmap_middle = null;

	private SHARE_MEDIA platform = null;

	/**
	 * 
	 */
	View mShareButton;

	/**
	 * 
	 */
	UMSocialService mController = UMServiceFactory
			.getUMSocialService("myshare");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//弹出分享面板
		findViewById(R.id.button).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showCustomUI(true, v);
			}
		});
		initSocialSDK();
		initPopupWindow();//
	}

	/**
	 * 初始化SDK，添加一些平�?
	 * 
	 * @功能描述 : 添加QQ平台支持 QQ分享的内容， 包含四种类型， 即单纯的文字、图片、音乐、视频. 参数说明 : title, summary,
	 *       image url中必须至少设置一个, targetUrl必须设置,网页地址必须以"http://"开头 . title :
	 *       要分享标题 summary : 要分享的文字概述 image url : 图片地址 [以上三个参数至少填写一个] targetUrl
	 *       : 用户点击该分享时跳转到的目标地址 [必填] ( 若不填写则默认设置为友盟主页 )
	 * @return
	 */
	private void initSocialSDK() {
		// 添加QQ平台
		UMQQSsoHandler qqHandler = new UMQQSsoHandler(MainActivity.this,
				"100424468", "c7394704798a158208a74ab60104f0ba");
		qqHandler.addToSocialSDK();

		// 添加QQ空间平台
		QZoneSsoHandler qzoneHandler = new QZoneSsoHandler(MainActivity.this,
				"100424468", "c7394704798a158208a74ab60104f0ba");
		qzoneHandler.addToSocialSDK();

		// // 添加短信
		// SmsHandler smsHandler = new SmsHandler();
		// smsHandler.addToSocialSDK();

		// 设置文字分享内容
		mController.setShareContent("这是文字分享内容");
		// 图片分享内容
		mController.setShareMedia(new UMImage(MainActivity.this,
				R.drawable.umeng_socialize_qq_on));

	}

	/**
	 * 显示您的自定义界面，当用户点击一个平台时，直接调用directShare或�?postShare来分�?
	 */
	private void showCustomUI(final boolean isDirectShare, View v) {
		// 响应按钮，弹出分享框

		popWin.showAsDropDown(v.findViewById(R.id.button),
				dip2px(MainActivity.this, -120), 0);
		buttonWeibo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				platform = SHARE_MEDIA.SINA;
				mController.postShare(MainActivity.this, platform,
						mShareListener);
			}
		});

		buttonQQ.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				platform = SHARE_MEDIA.QQ;
				// 设置要分享的内容
				QQShareContent qqShareContent = new QQShareContent();
				qqShareContent.setTitle("来自读书郎平板的分享");
				qqShareContent.setShareContent("这是读书郎要分享的内容");
				qqShareContent.setTargetUrl("http://www.readboy.com");
				UMImage localImage = new UMImage(MainActivity.this,
						R.drawable.icon);// 取得要分享的图片
				qqShareContent.setShareImage(localImage);
				// 将分享内容添加到分享控制器
				mController.setShareMedia(qqShareContent);
				mController.setAppWebSite("http://www.readboy.com");
				// 分享出去
				mController.postShare(MainActivity.this, platform,
						mShareListener);
			}
		});

		buttonQQZone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				platform = SHARE_MEDIA.QZONE;
				mController.postShare(MainActivity.this, platform,
						mShareListener);
			}
		});

		buttonWeichat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(MainActivity.this, "分享中...", Toast.LENGTH_SHORT)
						.show();
				regToWx();
				sendToWx();
			}
		});

	}

	/**
	 * 分享监听�?
	 */
	SnsPostListener mShareListener = new SnsPostListener() {

		@Override
		public void onStart() {

		}

		@Override
		public void onComplete(SHARE_MEDIA platform, int stCode,
				SocializeEntity entity) {
			if (stCode == 200) {
				Toast.makeText(MainActivity.this, "分享成功", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(MainActivity.this,
						"分享失败 : error code : " + stCode, Toast.LENGTH_SHORT)
						.show();
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(
				resultCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * 初始化popupwindow
	 */
	private void initPopupWindow() {

		viewLayout = this.getLayoutInflater().inflate(R.layout.dialog, null);// 获得popupwindow布局view
		// viewLayout.setBackground(getBackground());

		buttonWeibo = (ImageButton) viewLayout.findViewById(R.id.button_weibo);
		buttonQQ = (ImageButton) viewLayout.findViewById(R.id.button_qq);
		buttonQQZone = (ImageButton) viewLayout
				.findViewById(R.id.button_qqzone);
		buttonWeichat = (ImageButton) viewLayout
				.findViewById(R.id.button_weichat);

		popWin = new PopupWindow(viewLayout,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, true);
		// popWin.setBackgroundDrawable(getResources().getDrawable(R.drawable.window));
		popWin.setBackgroundDrawable(getBackground());
		// popWin.setBackgroundDrawable(getResources().getColor(R.color.umeng_socialize_color_group));
		popWin.setOutsideTouchable(true);

	}

	/**
	 * 将dip或dp值转换为px值，保证尺寸大小不变
	 * 
	 * @param dipValue
	 * @param scale
	 *            （DisplayMetrics类中属性density）
	 * @return
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/*
	 * public static int dip2px(float dipValue, float scale) { return (int)
	 * (dipValue * scale + 0.5f); } public static DisplayMetrics
	 * getMetrics(Activity activity) { DisplayMetrics metrics = new
	 * DisplayMetrics(); Display display =
	 * activity.getWindowManager().getDefaultDisplay();
	 * display.getMetrics(metrics); return metrics; }
	 */

	// 将appid注册到手机微信进程
	protected void regToWx() {
		// 通过WXAPIFactory获得微信通信api
		api = WXAPIFactory.createWXAPI(this, APP_ID, true);
		api.registerApp(APP_ID);
	}

	protected void sendToWx() {
		// 初始化一个WXTextObject对象
		WXTextObject textObj = new WXTextObject();
		textObj.text = "这是要分享的内容";// 可以将便签的内容放入到这里

		// 用WXTextObject对象初始化一个WXMediaMessage对象
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		msg.description = "来自读书郎平板的分享";

		// 构造一个Req
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;

		// 调用api接口发送数据到微信
		api.sendReq(req);
	}

	public BitmapDrawable getBackground() {
		// 将图片转换成Bitmap类型
		bitmap_up = BitmapFactory.decodeResource(getResources(), R.drawable.up);
		bitmap_middle = BitmapFactory.decodeResource(getResources(),
				R.drawable.middle);
		bitmap_down = BitmapFactory.decodeResource(getResources(),
				R.drawable.down);

		// 拼接三张图片
		Bitmap tempBitmap = conBitmap(bitmap_up, bitmap_middle);
		Bitmap fianlbBitmap = conBitmap(tempBitmap, bitmap_down);
		System.out.println("宽=" + fianlbBitmap.getWidth() + "高="
				+ fianlbBitmap.getHeight());
		int width = fianlbBitmap.getWidth();
		int height = fianlbBitmap.getHeight();
		// 设置新的高度
		int newWidth = 290;
		int newHiegth = 290;
		// 计算缩放比例
		float scaleWidth = ((float) newWidth) / width;
		float scaleHigth = ((float) newHiegth) / height;
		// 取得想要的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHigth);
		// 得到新图片
		Bitmap newBitmap = Bitmap.createBitmap(fianlbBitmap, 0, 0, width,
				height, matrix, true);
		// 将Bitmap转换成drawable
		BitmapDrawable drawable = new BitmapDrawable(getResources(), newBitmap);

		return drawable;
	}

	// 拼接两张图片
	public Bitmap conBitmap(Bitmap one, Bitmap two) {
		int height = one.getHeight() + two.getHeight();
		int width = Math.max(one.getWidth(), two.getWidth());
		Bitmap resultBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(resultBitmap);
		// Bitmap：图片对象，left:偏移左边的位置，top： 偏移顶部的位置
		// drawBitmap(Bitmap bitmap, float left, float top, Paint paint)
		canvas.drawBitmap(one, 0, 0, null);
		canvas.drawBitmap(two, 0, one.getHeight(), null);
		return resultBitmap;
	}

}