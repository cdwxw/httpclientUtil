package com.httpclient.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
 * 测试启用http连接池
 * 
 * @author wxw
 * @date 2016年11月7日 下午1:08:07
 * @version 1.0
 */
public class TestHttpPool {

	// 设置header信息
	private static final Header[] headers = 
			HttpHeader.custom().userAgent("Mozilla/5.0").from("http://blog.csdn.net/newest.html").build();

	// URL列表数组，GET请求
	private static final String[] urls = { 
		"http://blog.csdn.net/xiaoxian8023/article/details/49883113", 
		"http://blog.csdn.net/xiaoxian8023/article/details/49909359", 
		"http://blog.csdn.net/xiaoxian8023/article/details/49910127", 
		"http://www.baidu.com/", 
		"http://126.com", 
		};

	// 图片URL列表数组，Down操作
	private static final String[] imgurls = { 
		"http://119.6.84.89:7001/scwssb/CaptchaImg", 
		"http://59.41.9.91/GZCX/WebUI/Content/Handler/ValidateCode.ashx?0.3271647585525703", 
		};

    private static StringBuffer buf1=new StringBuffer();  
    private static StringBuffer buf2=new StringBuffer();  

	// 多线程get请求
	public static void testMultiGet(HttpConfig cfg, int count) throws HttpProcessException {
		try {
			int pagecount = urls.length;
			ExecutorService executors = Executors.newFixedThreadPool(pagecount);
			CountDownLatch countDownLatch = new CountDownLatch(count);
			// 启动线程抓取
			for (int i = 0; i < count; i++) {
				executors.execute(new GetRunnable(
						countDownLatch, cfg, urls[i % pagecount]));
			}
			countDownLatch.await();
			executors.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// 多线程下载
	public static void testMultiDown(HttpConfig cfg, int count) throws HttpProcessException {
		try {
			int pagecount = imgurls.length;
			ExecutorService executors = Executors.newFixedThreadPool(pagecount);
			CountDownLatch countDownLatch = new CountDownLatch(count);
			// 启动线程抓取
			for (int i = 0; i < count; i++) {
				executors.execute(new GetRunnable(
						countDownLatch, cfg, imgurls[i % 2], new FileOutputStream(new File("d:/aaa/" + (i + 1) + ".png"))));
			}
			
			// 使用CountDownLatch来保证所有线程都运行完毕
			countDownLatch.await();
			executors.shutdown();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
  
	/**
	 * 测试不启用http连接池，get100次，down20次的执行时间（线程数=urls的元素个数）
	 * 
	 * @throws HttpProcessException
	 */  
    private static void testNoPool(int getCount, int downCount) throws HttpProcessException {  
        long start = System.currentTimeMillis();  

		HCB hcb = HCB.custom().timeout(10000).ssl();// 使用一个client对象
        if(getCount>0){  
            HttpConfig cfg1 = HttpConfig.custom().client(hcb.build()).headers(headers);  
            testMultiGet(cfg1, getCount);  
        }  
        if(downCount>0){  
            HttpConfig cfg2 = HttpConfig.custom().client(hcb.build());  
            testMultiDown(cfg2, downCount);  
        }  
          
        System.out.println("-----所有线程已完成！------");  
        long end = System.currentTimeMillis();  
        System.out.println("总耗时（毫秒）： -> " + (end - start));  
        buf1.append("\t").append((end-start));  
    }

	/**
	 * 测试启用http连接池，get100次，down20次的执行时间（线程数=urls的元素个数）
	 * 
	 * @throws HttpProcessException
	 */
	private static void testByPool(int getCount, int downCount) throws HttpProcessException {
		long start = System.currentTimeMillis();

		HCB hcb = HCB.custom().timeout(10000).pool(10, 10).ssl();// 使用一个client对象
		if (getCount > 0) {
			HttpConfig cfg3 = HttpConfig.custom().client(hcb.build()).headers(headers);
			testMultiGet(cfg3, getCount);
		}
		if (downCount > 0) {
			HttpConfig cfg4 = HttpConfig.custom().client(hcb.build());
			testMultiDown(cfg4, downCount);
		}

		System.out.println("-----所有线程已完成！------");
		long end = System.currentTimeMillis();
		System.out.println("总耗时（毫秒）： -> " + (end - start));
		buf2.append("\t").append((end - start));
	}

	private static void testMultiByPoolOrNot() throws HttpProcessException, InterruptedException {
		File file = new File("D:/aaa");
		if (!file.exists() && !file.isFile()) {
			file.mkdir();
		}

		int[][] times1 = { { 10, 0 }, { 20, 0 }, { 50, 0 }, { 0, 0 } };
		int[][] times2 = { { 0, 10 }, { 0, 20 }, { 0, 50 }, { 0, 0 } };
		int[][] times3 = { { 10, 10 }, { 20, 20 }, { 50, 50 }, { 0, 0 } };
		List<int[][]> list = Arrays.asList(times1, times2, times3);
		int n = 1;// 同一条件	测试次数

		int t = 0;
		// 测试未启用http连接池，
		for (int[][] time : list) {
			buf1.append("\n");
			for (int i = 0; i < time.length; i++) {
				for (int j = 0; j < n; j++) {
					testNoPool(time[i][0], time[i][1]);
					Thread.sleep(100);
					System.gc();
					Thread.sleep(100);
				}
				buf1.append("\n");
			}
			t++;
		}

		t = 0;
		// 测试启用http连接池
		for (int[][] time : list) {
			buf2.append("\n");
			for (int i = 0; i < time.length; i++) {
				for (int j = 0; j < n; j++) {
					testByPool(time[i][0], time[i][1]);
					Thread.sleep(100);
					System.gc();
					Thread.sleep(100);
				}
				buf2.append("\n");
			}
			t++;
		}

		// 把结果打印到Console中
		String[] results1 = buf1.toString().split("\n");
		String[] results2 = buf2.toString().split("\n");

		for (int i = 0; i < results1.length; i++) {
			System.out.println(results1[i]);
			System.out.println(results2[i]);
		}
	}
	
	public static void main(String[] args) throws Exception {

		// -------------------------------------------
		// 以下测试通过使用线程池和未启用线程池2个方式测试http连接池（get次数，down次数）
		// {100,0},{200,0},{500,0},{1000,0}
		// {0,10},{0,20},{0,50},{0,100}
		// {100,10},{200,20},{500,50},{1000,100}
		// -------------------------------------------
//		testMultiByPoolOrNot();
		
		// 以下测试通过使用线程池和未启用线程池2个方式测试http连接池
		// 通过监控http链接查看连接池是否有效，脚本文件是httpConn.bat

		// 未启用线程池，直接启用100个线程，通过监控http连接数，查看连接池是否跟配置的一致
		testquickConcurrent();
	}

	/**
	 * 快速测试pool的应用(通过运行httpConn.bat监控http连接数，查看是否启用连接池)
	 * 
	 * @throws HttpProcessException
	 */
	private static void testquickConcurrent() throws HttpProcessException {
		// ---------------------------
		// --- 期望结果：
		// 由于urls中有5个域名，所以会为每个域名最多建立20个http链接，
		// 通过上面的监控，应该会看到http连接数会增加5*20=100个左右
		// ---------------------------
		HttpClient client = HCB.custom().pool(100, 20).timeout(10000).build();// 最多创建20个http链接
		final HttpConfig cfg = HttpConfig.custom().client(client).headers(headers);// 为每次请求创建一个实例化对象
		for (int i = 0; i < 500; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						int idx = ((int) (Math.random() * 10)) % 5;
						HttpClientUtil.get(cfg.url(urls[idx]));
						System.out.println("---idx=" + idx);
					} catch (HttpProcessException e) {
					}
				}
			}).start();
		}
	}
}