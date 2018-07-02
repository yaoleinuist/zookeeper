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
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

public class CuratorWatcher2 {
	
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
		
		//4 建立一个PathChildrenCache缓存,true：缓存数据 第三个参数为是否接受节点数据内容 如果为false则不接受，一般都是要接受变更的数据
		//5 子节点的增删改 
		PathChildrenCache cache = new PathChildrenCache(cf, "/super", true);
		//5 在初始化的时候就进行缓存监听
		cache.start(StartMode.POST_INITIALIZED_EVENT);
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			/**
			 * <B>方法名称：</B>监听子节点变更<BR>
			 * <B>概要说明：</B>新建、修改、删除<BR>
			 * 主要包括新增子节点（CHILD_ADDED），子节点数据变更（CHILD_UPDATE）和子节点删除（CHILD_REMOVED）三类。
			 * 和其他ZooKeeper客户端产品一样，Curator也无法对二级子节点进行事件监听。也就是说，如果使用PathChildrenCache对/zk-book进行监听，那么当/zk-book/c1/c2节点被创建或删除的时候，是无法触发子节点变更事件的
			 * 
			 * @see org.apache.curator.framework.recipes.cache.PathChildrenCacheListener#childEvent(org.apache.curator.framework.CuratorFramework, org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent)
			 */
			public void childEvent(CuratorFramework cf, PathChildrenCacheEvent event) throws Exception {
				switch (event.getType()) {
				case CHILD_ADDED:
					System.out.println("CHILD_ADDED :" + event.getData().getPath());
					break;
				case CHILD_UPDATED:
					System.out.println("CHILD_UPDATED :" + event.getData().getPath());
					break;
				case CHILD_REMOVED:
					System.out.println("CHILD_REMOVED :" + event.getData().getPath());
					break;
				default:
					break;
				}
			}
		});

		//创建本身节点不发生变化
		cf.create().forPath("/super", "init".getBytes());
		
		//添加子节点
		Thread.sleep(1000);
		cf.create().forPath("/super/c1", "c1内容".getBytes());
		Thread.sleep(1000);
		cf.create().forPath("/super/c2", "c2内容".getBytes());
		
		//修改子节点
		Thread.sleep(1000);
		cf.setData().forPath("/super/c1", "c1更新内容".getBytes());
		
		//删除子节点
		Thread.sleep(1000);
		cf.delete().forPath("/super/c2");		
		
		//删除本身节点
		Thread.sleep(1000);
		cf.delete().deletingChildrenIfNeeded().forPath("/super");
		
		Thread.sleep(Integer.MAX_VALUE);
		

	}
}
