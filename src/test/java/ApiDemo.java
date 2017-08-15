

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.httpclient.HttpClientUtil;
import com.httpclient.common.HttpConfig;
import com.httpclient.common.HttpCookies;
import com.httpclient.exception.HttpProcessException;

/** 
 * 测试用例 Demo
 * 
 * @author wxw
 * @date 2017-08-07
 * @version 1.1.2 
 */
public class ApiDemo {
	private static final String RPC_URL = "http://ysg.139mm.cn/rpc";//
	private static final String CAPTCHA_URL = "http://ysg.139mm.cn/PzhYsCaptcha";
	private static final String CAPTCHA_FILE_PATH = "d:/valiCode.png";
	
	//攀枝花
	private static final String GJJ_LOGIN_CODE = "51040001";//公积金登录
	private static final String GJJ_DATA_CODE = "51040002";//公积金数据
	private static final String YS_LOGIN_CODE = "51040011";//医社登录
	private static final String YS_INDEX_CODE = "51040012";//医社主页信息
	private static final String YS_INSURANCE_CODE = "51040013";//医社参保信息
	private static final String YS_COST_CODE = "51040014";//医社缴费信息
	private static final String YS_ACCOUNT_CODE = "51040015";//医社个人账户
	private static final String API_KEY = "";
	
	public static void main(String[] args) throws HttpProcessException, IOException {
		// 登录前每个用户生成自己的HttpCookies，保持各自会话用
		HttpConfig config = HttpConfig.custom();
		HttpCookies cookies = HttpCookies.custom();
		
		/*
		 * 公积金查询
		 */
		gjjLogin(config, cookies);
		
		getGjjData(config, cookies);

		
		/*
		 * 医社保查询
		 */
		String captchaCode = getCaptcha(config, cookies);
		
		ysLogin(config, cookies, captchaCode);

		getYsData(config, cookies, YS_INDEX_CODE, null);

		getYsData(config, cookies, YS_INSURANCE_CODE, null);

		getYsData(config, cookies, YS_COST_CODE, "310");

		getYsData(config, cookies, YS_ACCOUNT_CODE, "310");
		
	}

	/**
	 * 获取医社保数据
	 * @param config
	 * @param cookies
	 * @param reqCode
	 * @param type 
					110职工基本养老保险
					120机关事业养老保险
					210失业保险
					310职工基本医疗保险
					410 工伤保险
					510 生育保险
	 * @throws HttpProcessException
	 */
	public static void getYsData(HttpConfig config, HttpCookies cookies, String reqCode, String type) throws HttpProcessException {
		System.out.println("--------------------获取医社保数据--------------------");
		String jsonStr = "{" +
				"\"f_telephone\":\"110\"," +
				"\"type\":\""+type+"\"," +
				"}"; 
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("apiKey", API_KEY);
		map.put("reqCode", reqCode);
		map.put("reqData", jsonStr);

		String res = HttpClientUtil.post(config.url(RPC_URL).map(map).context(cookies.getContext()));
		System.out.println(res);
	}

	/**
	 * 医社保登录
	 * @param config
	 * @param cookies
	 * @param captchaCode
	 * @throws HttpProcessException
	 */
	public static void ysLogin(HttpConfig config, HttpCookies cookies, String captchaCode) throws HttpProcessException {
		System.out.println("--------------------医社保登录--------------------");
		String jsonStr = "{" +
				"\"f_telephone\":\"110\"," +
				"\"f_token\":null," +
				"\"f_ys_uid\":\"510683199102270015\"," +
				"\"f_ys_pwd\":\"mowenqiang1991\"," +
				"\"f_captcha\":\""+captchaCode+"\"" +
				"}"; 
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("apiKey", API_KEY);
		map.put("reqCode", YS_LOGIN_CODE);
		map.put("reqData", jsonStr);

		String res = HttpClientUtil.post(config.url(RPC_URL).map(map).context(cookies.getContext()));
		System.out.println(res);
	}

	/**
	 * 获取验证码
	 * @param config
	 * @param cookies
	 * @return
	 * @throws HttpProcessException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String getCaptcha(HttpConfig config, HttpCookies cookies) throws HttpProcessException, FileNotFoundException, IOException {
		System.out.println("--------------------获取验证码--------------------");
		String checkCode = "";
		File file = new File(CAPTCHA_FILE_PATH);
		HttpClientUtil.down(config.url(CAPTCHA_URL).out(new FileOutputStream(file)).context(cookies.getContext()));
		if (file.exists()) {
			System.out.println("验证码图片下载成功了！存放在：" + file.getPath());
			// 验证码通过输入获取
			System.out.println("请输入验证码：");
			BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
			checkCode = buff.readLine();
		}
		return checkCode;
	}

	/**
	 * 获取公积金数据
	 * @param config
	 * @param cookies
	 * @throws HttpProcessException
	 */
	public static void getGjjData(HttpConfig config, HttpCookies cookies) throws HttpProcessException {
		System.out.println("--------------------获取公积金数据--------------------");
		String jsonStr = "{"+
				"\"f_telephone\":\"110\"," +
				"}"; 
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("apiKey", API_KEY);
		map.put("reqCode", GJJ_DATA_CODE);
		map.put("reqData", jsonStr);

		String res = HttpClientUtil.post(config.url(RPC_URL).map(map).context(cookies.getContext()));
		System.out.println(res);
	}

	/**
	 * 公积金登录
	 * @param config
	 * @param cookies
	 * @throws HttpProcessException
	 */
	public static void gjjLogin(HttpConfig config, HttpCookies cookies) throws HttpProcessException {
		System.out.println("--------------------公积金登录--------------------");
		String jsonStr = "{"+
				"\"f_telephone\":\"110\"," +
//				"\"f_token\":null," +
				"\"f_gjj_uid\":\"510683199102270015\"," +
				"\"f_gjj_pwd\":\"mowenqiang19\"," +
//				"\"f_captcha\":\"\"" +
				"}"; 
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("apiKey", API_KEY);
		map.put("reqCode", GJJ_LOGIN_CODE);
		map.put("reqData", jsonStr);

		String res = HttpClientUtil.post(config.url(RPC_URL).map(map).context(cookies.getContext()));
		System.out.println(res);
	}

}
