package com.httpclient.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;

import com.httpclient.HttpClientUtil;
import com.httpclient.builder.HCB;
import com.httpclient.common.HttpConfig;
import com.httpclient.common.HttpHeader;
import com.httpclient.exception.HttpProcessException;

/** 
 * 
 * @author wxw
 * @date 2015年11月1日 下午2:23:18 
 * @version 1.0 
 */
public class HttpClientTest {
	
	public static void main(String[] args) throws Exception {
		File file = new File("D:/aaa");
		if (!file.exists() && !file.isFile()) {
			file.mkdir();
		}
		
//		Utils.debug();
		
//		testOne();
		testMutilTask();
	}
	
	/**
	 * 单线程调用测试
	 * @throws HttpProcessException
	 */
	public static void testOne() throws HttpProcessException{
		
		System.out.println("--------简单方式调用（默认GET）--------");
		String url = "http://tool.oschina.net/";
		HttpConfig config = HttpConfig.custom();
		//简单调用
		String resp = HttpClientUtil.get(config.url(url));

		System.out.println("请求结果内容长度："+ resp.length());
		
		System.out.println("\n#################################\n");
		
		System.out.println("--------加入header设置--------");
		url="http://blog.csdn.net/xiaoxian8023";
		//设置header信息
		Header[] headers=HttpHeader.custom().userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0").build();
		//执行请求
		resp = HttpClientUtil.get(config.url(url).headers(headers));
		System.out.println("请求结果内容长度："+ resp.length());

		System.out.println("\n#################################\n");
		
		System.out.println("--------代理设置（绕过证书验证）-------");
		url = "https://www.facebook.com/";
//		url = "https://kyfw.12306.cn/otn/";
//		url = "https://gr.cdhrss.gov.cn:442/cdwsjb/login.jsp";
		HttpClient client= HCB.custom()
								.timeout(10000)
								.proxy("127.0.0.1", 1080)	//代理
								.ssl()						//采用默认方式（绕过证书验证）
								.build()
								;
		//执行请求
		resp = HttpClientUtil.get(config.url(url).client(client));
		System.out.println("请求结果内容长度："+ resp.length());

//		System.out.println("--------代理设置（自签名证书验证）+header+get方式-------");
//		url = "https://sso.tgb.com:8443/cas/login";
//		client= HCB.custom().timeout(10000).ssl("D:\\keys\\wsriakey","tomcat").build();
//		headers=HttpHeader.custom().keepAlive("false").connection("close").contentType(Headers.APP_FORM_URLENCODED).build();
//		//执行请求
//		resp = CopyOfHttpClientUtil.get(config.method(HttpMethods.GET));
//		System.out.println("请求结果内容长度："+ resp.length());

		System.out.println("\n#################################\n");

		try {
			System.out.println("--------下载测试-------");
			url="http://ss.bdimg.com/static/superman/img/logo/logo_white_fe6da1ec.png";
			FileOutputStream out = new FileOutputStream(new File("D:/aaa/000.png"));
			HttpClientUtil.down(config.url(url).out(out));
			out.flush();
			out.close();
			
			System.out.println("--------下载测试+代理-------");
			out = new FileOutputStream(new File("d:/aaa/001.png"));
			HttpClientUtil.down(config.client(client).url(url).out(out));
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 多线程调用测试
	 * @throws HttpProcessException
	 */
	public static void testMutilTask() throws HttpProcessException{
		// URL列表数组
		String[] urls = {
				"http://blog.csdn.net/xiaoxian8023/article/details/49883113",
				"http://blog.csdn.net/xiaoxian8023/article/details/49909359",
				"http://blog.csdn.net/xiaoxian8023/article/details/49910127",
				"http://blog.csdn.net/xiaoxian8023/article/details/49910885",
				
				"http://blog.csdn.net/xiaoxian8023/article/details/49862725",
				"http://blog.csdn.net/xiaoxian8023/article/details/49834643",
				"http://blog.csdn.net/xiaoxian8023/article/details/49834615",
				"http://blog.csdn.net/xiaoxian8023/article/details/49834589",
				"http://blog.csdn.net/xiaoxian8023/article/details/49785417",
				
				"http://blog.csdn.net/xiaoxian8023/article/details/48679609",
				"http://blog.csdn.net/xiaoxian8023/article/details/48681987",
				"http://blog.csdn.net/xiaoxian8023/article/details/48710653",
				"http://blog.csdn.net/xiaoxian8023/article/details/48729479",
				"http://blog.csdn.net/xiaoxian8023/article/details/48733249",

				"http://blog.csdn.net/xiaoxian8023/article/details/48806871",
				"http://blog.csdn.net/xiaoxian8023/article/details/48826857",
				"http://blog.csdn.net/xiaoxian8023/article/details/49663643",
				"http://blog.csdn.net/xiaoxian8023/article/details/49619777",
				"http://blog.csdn.net/xiaoxian8023/article/details/47335659",

				"http://blog.csdn.net/xiaoxian8023/article/details/47301245",
				"http://blog.csdn.net/xiaoxian8023/article/details/47057573",
				"http://blog.csdn.net/xiaoxian8023/article/details/45601347",
				"http://blog.csdn.net/xiaoxian8023/article/details/45569441",
				"http://blog.csdn.net/xiaoxian8023/article/details/43312929", 
				};
		String[] imgurls ={
				"http://119.6.84.89:7001/scwssb/CaptchaImg", 
				"http://59.41.9.91/GZCX/WebUI/Content/Handler/ValidateCode.ashx?0.3271647585525703", 
				};
		// 设置header信息
		Header[] headers = HttpHeader.custom()
									.userAgent("Mozilla/5.0")
									.from("user@email.com")
									.build()
									;
		HttpClient client = HCB.custom()							//使用一个client对象
									.timeout(10000)				//超时
//									.proxy("127.0.0.1", 1080)	//代理
									.ssl()						//采用默认方式（绕过证书验证）
									.build()
									;
        HttpConfig cfg1 = HttpConfig.custom().client(client).headers(headers);
        HttpConfig cfg2 = HttpConfig.custom().client(client);  
        
		long start = System.currentTimeMillis();
		try {
			int pagecount = urls.length;
			System.out.println(pagecount);
			ExecutorService executors = Executors.newFixedThreadPool(pagecount);
			
			//因为执行2个execute，所以执行2次run()方法，执行2次countDownLatch.countDown();
			//所以new CountDownLatch(count * 2);	参数要乘以2
			int count = 50;
			CountDownLatch countDownLatch = new CountDownLatch(count * 2);
			for (int i = 0; i < count; i++) {
				// 启动线程抓取
				executors.execute(new GetRunnable(
						countDownLatch, cfg1, urls[i % pagecount]));
				executors.execute(new GetRunnable(
						countDownLatch, cfg2, imgurls[i % 2], new FileOutputStream(new File("d:/aaa/" + (i + 1) + ".png"))));
			}

			// 使用CountDownLatch来保证所有线程都运行完毕
			countDownLatch.await();
			executors.shutdown();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("线程" + Thread.currentThread().getName() + ", 所有线程已完成，开始进入下一步！");
		}

		long end = System.currentTimeMillis();
		System.out.println("总耗时（毫秒）： -> " + (end - start));
		// (7715+7705+7616)/3= 23 036/3= 7 678.66---150=51.2
		// (9564+8250+8038+7604+8401)/5=41 857/5=8 371.4--150
		// (9803+8244+8188+8378+8188)/5=42 801/5= 8 560.2---150
	}

	static class GetRunnable implements Runnable {
		private CountDownLatch countDownLatch;
		private HttpConfig config = null;
		private FileOutputStream out = null;
		private String url = null;
		public GetRunnable(CountDownLatch countDownLatch, HttpConfig config, String url) {
			this(countDownLatch, config, url, null);
		}
		public GetRunnable(CountDownLatch countDownLatch, HttpConfig config, String url, FileOutputStream out) {
			this.countDownLatch = countDownLatch;
			this.config = config;
			this.out = out;
			this.url = url;
		}
		@Override
		public void run() {
			try {
				config.out(out);
				config.url(url);
				if (config.out() == null) {
					String response = null;
					response = HttpClientUtil.get(config);
					System.out.println(Thread.currentThread().getName() + "--获取内容长度：" + response.length() + "|" + config.url());
					response = null;
				} else {
					HttpClientUtil.down(config);
					try {
						config.out().flush();
						config.out().close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println(Thread.currentThread().getName() + "--下载图片地址：" + config.url());
				}
			} catch (HttpProcessException e) {
				e.printStackTrace();
			} finally {
				countDownLatch.countDown();
			}
		}
	}
}