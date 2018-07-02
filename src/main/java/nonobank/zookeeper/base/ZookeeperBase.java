package nonobank.zookeeper.base;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.OpResult;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;

/**
 * @ClassName: ZookeeperBase
 * @Description: 分享
 * @author yaolei
 * @date 2018年5月27日
 *
 */
public class ZookeeperBase {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "127.0.0.1:2181,127.0.0.1:2183,127.0.0.1:2182";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;// ms
	/** 信号量，阻塞程序执行，用于等待zookeeper连接成功，发送成功信号 ,zk连接为异步的，如果接收到连接成功的信号事件，则主程序继续往下走 */
	static final CountDownLatch connectedSemaphore = new CountDownLatch(1);

	public static void main(String[] args) throws Exception {
		// Watcher一个观察者，watch的特性很重要，一次性的。
		ZooKeeper zk = new ZooKeeper(CONNECT_ADDR, SESSION_OUTTIME, new Watcher() {
			public void process(WatchedEvent event) {
				// 获取事件的状态
				KeeperState keeperState = event.getState();
				// 事件的类型
				EventType eventType = event.getType();
				// 如果是建立连接
				if (KeeperState.SyncConnected == keeperState) {
					// 刚连接，啥时间也没有，因此为none
					if (EventType.None == eventType) {
						// 如果建立连接成功，则发送信号量，让后续阻塞程序向下执行
						connectedSemaphore.countDown();
						System.out.println("zk 建立连接");
					}
				}
			}
		});

		// 进行阻塞
		connectedSemaphore.await();
//		zk.delete("/testRoot/children", -1, null, "a");
//		zk.delete("/testRoot",-1);
		
		List<OpResult> list = zk.transaction().create("/testRoot", "testRoot".getBytes(),
				Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT).delete("/testRoot", -1).commit();
		System.out.println(list);
		String res = zk.create("/testRoot", "testRoot".getBytes(),
				Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		System.out.println("..");
		System.out.println(res);
		// 创建父节点,CreateMode.PERSISTENT持久化, Ids.OPEN_ACL_UNSAFE，开放权限，不允许递归创建节点。
		Stat f = zk.exists("/testRoot", null);
		System.out.println(new Date(f.getCtime()));
		//int rc 是状态码
		VoidCallback callBack = new AsyncCallback.VoidCallback() {
			public void processResult(int rc, String path, Object ctx) {
				System.out.println("-->" + rc);
				System.out.println(path);
				System.out.println(ctx);
			}
		};
		// 创建子节点,临时节点
		res = zk.create("/testRoot/children", "children data".getBytes(), Ids.OPEN_ACL_UNSAFE,
				CreateMode.PERSISTENT_SEQUENTIAL);
		// Thread.sleep(20000);
		System.out.println(res);
		zk.sync("/testRoot/children", callBack, "b");
		// "a" callback ctx带回去的参数
		zk.delete("/testRoot/children0000000000", -1, callBack, "a");
	//	zk.delete("/testRoot",-1);
		 System.out.println("删除成功");
		// Thread.sleep(11111);
		// System.out.println("删除成功");
		// 获取节点洗信息
//		 byte[] data = zk.getData("/testRoot", false, null);
//		 System.out.println(new String(data));
//		 System.out.println(zk.getChildren("/testRoot", false));

		// 修改节点的值
		 zk.setData("/testRoot", "modify data root".getBytes(), -1);
		 byte[] data = zk.getData("/testRoot", false, null);
		 System.out.println(new String(data));

		// 判断节点是否存在
		// System.out.println(zk.exists("/testRoot/children", false));
		// 删除节点
		// zk.delete("/testRoot/children", -1);
		// System.out.println(zk.exists("/testRoot/children", false));

		zk.close();

	}

}
