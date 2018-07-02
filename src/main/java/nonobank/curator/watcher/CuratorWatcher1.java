package nonobank.curator.watcher;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

public class CuratorWatcher1 {
	
	/** zookeeper地址 */
	static final String CONNECT_ADDR = "127.0.0.1:2181,127.0.0.1:2183,127.0.0.1:2182";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	public static void main(String[] args) throws Exception {
		
		//1 重试策略：初试时间为1s 重试10次
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2 通过工厂创建连接
		CuratorFramework cf = CuratorFrameworkFactory.builder()
					.connectString(CONNECT_ADDR)
					.sessionTimeoutMs(SESSION_OUTTIME)
					.retryPolicy(retryPolicy)
					.build();
		
		//3 建立连接
		cf.start();
		
		//4 建立一个cache缓存,在客户端实例中注册一个监听缓存，然后实现对应的监听方法即可。性能也是最好的。
		// 设计思想，只要有事件的变化，client的cache和服务端对比，有变化就直接将服务端拉下来。最大的亮点
		//监控当前节点的一切状态变化
		//NodeCache不仅可以用于监听数据节点内容变更，也能监听指定节点是否存在。如果原本节点不存在，那么cache就会在节点创建后触发NodeCacheListener,但是如果该节点被删除，那么Curator就无法触发NodeCacheListener了。
		//false 标识不压缩
		//不常用
		final NodeCache cache = new NodeCache(cf, "/super", false);
		cache.start(true);
		cache.getListenable().addListener(new NodeCacheListener() {
			/**
			 * <B>方法名称：</B>nodeChanged<BR>
			 * <B>概要说明：</B>触发事件为创建节点和更新节点，在删除节点的时候并不触发此操作。<BR>
			 * @see org.apache.curator.framework.recipes.cache.NodeCacheListener#nodeChanged()
			 */
			public void nodeChanged() throws Exception {
				System.out.println("路径为：" + cache.getCurrentData().getPath());
				System.out.println("数据为：" + new String(cache.getCurrentData().getData()));
				System.out.println("状态为：" + cache.getCurrentData().getStat());
				System.out.println("---------------------------------------");
			}
		});

		Thread.sleep(1000);
		cf.create().forPath("/super", "123".getBytes());
		
		Thread.sleep(1000);
		cf.setData().forPath("/super", "456".getBytes());
		
		Thread.sleep(1000);
		cf.delete().forPath("/super");
		
		Thread.sleep(Integer.MAX_VALUE);
		
		

	}
}
