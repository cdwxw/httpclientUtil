package ysg.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.httpclient.HttpClientUtil;
import com.httpclient.common.HttpConfig;
import com.httpclient.common.HttpHeader;
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
	public static final String POST_URL = "http://119.6.84.89:7001/scwssb/j_spring_security_check";// 登录二次POST请求url
	public static final String TO_INDEX_URL = "http://119.6.84.89:7001/scwssb/loginSuccessAction.do";// 登录成功后url
	public static final String INDEX_URL = "http://119.6.84.89:7001/scwssb/welcome2.jsp";// 主页url
	public static final String CHARSET = "utf-8";// 字符编码设置

	public static void main(String[] args) throws HttpProcessException, FileNotFoundException {

		// 定义cookie存储
		HttpClientContext context = new HttpClientContext();
		CookieStore cookieStore = new BasicCookieStore();
		context.setCookieStore(cookieStore);
		HttpConfig config = HttpConfig.custom().url(TO_LOGIN_URL).encoding(CHARSET).context(context);

		// 获取登录所需参数
		String loginform = HttpClientUtil.get(config);// 可以用.send(config)代替，但是推荐使用明确的get方法
		// System.out.println(loginform);
		System.out.println("获取登录所需参数");
		// j_username : j_username,
		// j_password : j_password,
		// orgId : $("#orgId").val(),
		// checkCode : $("#checkCode").val(),
		// bz : 0,
		// tm : new Date().getTime()

		// 获取验证码图片
		File file = new File(IMAGE_SAVE_PATH);
		HttpClientUtil.down(HttpConfig.custom().url(CREATE_IMAGE_URL).out(new FileOutputStream(file)));
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
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("j_username", "w2300603");
		map.put("j_password", "pw182676");
		map.put("checkCode", checkCode);
		map.put("orgId", "");
		map.put("bz", "0");
		map.put("tm", Long.toString(new Date().getTime()));
		// for(Entry<String, Object> e:map.entrySet()){
		// System.out.println(e.getKey()+"--"+e.getValue());
		// }

		printCookieStore(context);

		// 发送登录POST请求
		String result = "";
		result = HttpClientUtil.post(config.url(LOGIN_URL).map(map));// 可以用.send(config.method(HttpMethods.POST).map(map))代替，但是推荐使用明确的post方法
		System.out.println(LOGIN_URL + result);

		printCookieStore(context);

		map.put("r", "'" + Math.random());

		// 二次发送登录POST请求
		// Header[] hs =
		// HttpHeader.custom().cookie("j_username=w2300603; JSESSIONID=VGfL4-PPEXvI5mjHUvylpLCfKhtKrmBxE-jr6TBssN7XLkiVlnmX!-475734474; SERVERID=s5").build();
		result = HttpClientUtil.post(config.url(POST_URL).map(map));// 可以用.send(config.method(HttpMethods.POST).map(map))代替，但是推荐使用明确的post方法
		System.out.println(POST_URL + result);
		if (result.contains("验证码输入错误")) {// 如果有帐号登录，则说明未登录成功
			Document doc = Jsoup.parse(result);
			// String errmsg = doc.select("script").html();
			System.err.println("登录失败：" + doc);
			return;
		}
		System.out.println("----登录成功----");

		cookieStore = context.getCookieStore();// 二次POST 新建cookieStore
//		 BasicClientCookie bcc = new BasicClientCookie("j_username", "w2300603");// 下发token至客户端cookie，登陆检查用
//		 cookieStore.addCookie(bcc);
		context.setCookieStore(cookieStore);
		printCookieStore(context);

		// 访问主页
		Header[] headers = HttpHeader.custom().userAgent("User-Agent: Mozilla/5.0").build();
		result = HttpClientUtil.get(config.url(TO_INDEX_URL).headers(headers).context(context));// 可以用.send(config.url(scoreUrl).headers(headers))代替，但是推荐使用明确的post方法
		System.out.println("跳转主页：" + TO_INDEX_URL + result);
		printCookieStore(context);

		result = HttpClientUtil.get(config.url(INDEX_URL).headers(headers).context(context));// 可以用.send(config.url(scoreUrl).headers(headers))代替，但是推荐使用明确的post方法
		// System.out.println("访问主页：" + INDEX_URL + result);
		printCookieStore(context);

	}

	public static void printCookieStore(HttpClientContext context) {
		CookieStore cookieStore;
		// 打印参数，可以看到cookie里已经有值了。
		cookieStore = context.getCookieStore();
		for (Cookie cookie : cookieStore.getCookies()) {
			System.out.println(cookie.getName() + "--" + cookie.getValue());
		}
	}

}
