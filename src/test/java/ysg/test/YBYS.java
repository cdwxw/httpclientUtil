package ysg.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.httpclient.HttpClientUtil;
import com.httpclient.common.HttpConfig;
import com.httpclient.exception.HttpProcessException;

/**
 * 测试携带cookie的操作
 * 
 * @author wxw
 * @date 2017年5月3日 上午10:09:53
 * @version 1.0
 */
public class YBYS {

	public static final String CREATE_IMAGE_URL = "http://119.6.84.89:7001/scwssb/CaptchaImg";// 验证码图片请求页面
	public static final String IMAGE_SAVE_PATH = "d://valiCode.png";
	public static final String TO_LOGIN_URL = "http://119.6.84.89:7001/scwssb/login.jsp";// 进入登录页面url
	public static final String LOGIN_URL = "http://119.6.84.89:7001/scwssb/userYzAction!check.do";// 登录POST请求url
	// public static final String POST_URL =
	// "http://119.6.84.89:7001/scwssb/j_spring_security_check";// 登录二次POST请求url
	public static final String POST_REDIRECT_URL = "http://119.6.84.89:7001/scwssb/j_spring_security_check?r=%27+Math.random()";// 二次POST
																																// 重定向
	public static final String LOGIN_SUCCESS_URL = "http://119.6.84.89:7001/scwssb/loginSuccessAction.do";// 登录成功后url
	public static final String GET_REDIRECT_URL = "http://119.6.84.89:7001/scwssb/indexAction.do";// GET
																									// 重定向
	public static final String INDEX_URL = "http://119.6.84.89:7001/scwssb/welcome2.jsp";// 主页url
	public static final String CHARSET = "utf-8";// 字符编码设置

	public static void main(String[] args) throws HttpProcessException, FileNotFoundException {

		// 定义cookie存储
		HttpClientContext context = new HttpClientContext();
		CookieStore cookieStore = new BasicCookieStore();
		context.setCookieStore(cookieStore);
		HttpConfig config = HttpConfig.custom().url(TO_LOGIN_URL).encoding(CHARSET).context(context);

		// 获取登录所需参数
		HttpClientUtil.get(config);// 可以用.send(config)代替，但是推荐使用明确的get方法
		System.out.println("获取登录所需参数");

		// 获取验证码图片
		File file = new File(IMAGE_SAVE_PATH);
		HttpClientUtil.down(config.url(CREATE_IMAGE_URL).out(new FileOutputStream(file)));
		if (file.exists()) {
			System.out.println("图片下载成功了！存放在：" + file.getPath());
		}

		// 验证码通过输入获取
		System.out.println("请输入验证码：");
		String checkCode = "";
		BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
		try {
			checkCode = buff.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 组装参数
		// j_username : j_username,
		// j_password : j_password,
		// orgId : $("#orgId").val(),
		// checkCode : $("#checkCode").val(),
		// bz : 0,
		// tm : new Date().getTime()
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("j_username", "w2300603");
		map.put("j_password", "pw182676");
		map.put("checkCode", checkCode);
		map.put("orgId", "undefined");
		map.put("bz", "0");
		map.put("tm", new Date().getTime() + "");
		// for (Entry<String, Object> e : map.entrySet()) {
		// System.out.println(e.getKey() + "--" + e.getValue());
		// }

		// 发送登录POST请求
		HttpClientUtil.post(config.url(LOGIN_URL).map(map));// 可以用.send(config.method(HttpMethods.POST).map(map))代替，但是推荐使用明确的post方法

		// 二次发送登录POST请求
		HttpClientUtil.post(config.url(POST_REDIRECT_URL).map(map));// 可以用.send(config.method(HttpMethods.POST).map(map))代替，但是推荐使用明确的post方法
		String result = HttpClientUtil.get(config.url(LOGIN_SUCCESS_URL).context(context));// 可以用.send(config.url(scoreUrl).headers(headers))代替，但是推荐使用明确的post方法
		if (result.contains("false")) {// 如果有帐号登录，则说明未登录成功
			Document doc = Jsoup.parse(result);
			// String errmsg = doc.select("script").html();
			System.err.println("登录失败：" + doc);
			return;
		}
		System.out.println("----登录成功----");

		result = HttpClientUtil.get(config.url("http://119.6.84.89:7001/scwssb/index_nethall/privateHome.jsp").context(context));// 可以用.send(config.url(scoreUrl).headers(headers))代替，但是推荐使用明确的post方法
		 System.out.println("访问主页：" + result);

	}

	public static void printCookieStore(HttpClientContext context) {
		CookieStore cookieStore;
		// 打印参数，可以看到cookie里已经有值了。
		cookieStore = context.getCookieStore();
		for (Cookie cookie : cookieStore.getCookies()) {
			System.out.println("Cookies:" + cookie.getName() + "--" + cookie.getValue());
		}
	}

}
