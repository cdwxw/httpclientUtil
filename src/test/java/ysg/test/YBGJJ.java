package ysg.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.httpclient.HttpClientUtil;
import com.httpclient.common.HttpConfig;
import com.httpclient.common.HttpHeader;
import com.httpclient.exception.HttpProcessException;

/** 
 * 测试携带cookie的操作
 * 
 * @author arron
 * @date 2016年1月7日 上午10:09:53 
 * @version 1.0 
 */
public class YBGJJ {
	public static final String TO_LOGIN_URL = "http://cx.ybgjj.com:5566/search.asp";// 登录页面url
	public static final String LOGIN_URL = "http://cx.ybgjj.com:5566/search.asp?s=login";// 登录请求url
	public static final String TO_INDEX_URL = "http://cx.ybgjj.com:5566/search.asp";// 登录成功后进入主页url
	public static final String CHARSET = "gb2312";// 字符编码设置

	public static void main(String[] args) throws HttpProcessException {
		
		//定义cookie存储
		HttpClientContext context = new HttpClientContext();
		CookieStore cookieStore = new BasicCookieStore();
		context.setCookieStore(cookieStore);
		HttpConfig config =HttpConfig.custom().url(TO_LOGIN_URL).encoding(CHARSET).context(context);
		//获取参数
		String loginform = HttpClientUtil.get(config);//可以用.send(config)代替，但是推荐使用明确的get方法
//		System.out.println(loginform);
		System.out.println("获取登录所需参数");
		
		//组装参数
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("sfz", "511502198612130656");
		map.put("pass", "wangkaizhi");
		map.put("gjj", "");
		map.put("B1", "公积金查询");
		map.put("s", "login");

		//发送登录请求
		String result = HttpClientUtil.post(config.map(map));//可以用.send(config.method(HttpMethods.POST).map(map))代替，但是推荐使用明确的post方法
		System.out.println(result);
		if(result.contains("不正确")){//如果有帐号登录，则说明未登录成功
			Document doc = Jsoup.parse(result);
			String errmsg = doc.select("script").html();
			System.err.println("登录失败："+errmsg);
			return;
		}
		System.out.println("----登录成功----");
		
		//打印参数，可以看到cookie里已经有值了。
		cookieStore = context.getCookieStore();
		for (Cookie cookie : cookieStore.getCookies()) {
			System.out.println(cookie.getName()+"--"+cookie.getValue());
		}
		
		//访问主页
		Header[] headers = HttpHeader.custom().userAgent("User-Agent: Mozilla/5.0").build();
		result = HttpClientUtil.get(config.url(TO_INDEX_URL).headers(headers));//可以用.send(config.url(scoreUrl).headers(headers))代替，但是推荐使用明确的post方法
		//获取页面
		System.out.println("html："+result);
		
	}
	
}
