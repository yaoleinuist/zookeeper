package nonobank.zookeeper.cluster;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class ZKWatcher implements Watcher {

	/** zk变量 */
	private ZooKeeper zk = null;

	/** 父节点path */
	static final String PARENT_PATH = "/super";

	/** 信号量设置，用于等待zookeeper连接建立之后 通知阻塞程序继续向下执行 */
	private CountDownLatch connectedSemaphore = new CountDownLatch(1);
	/***
	 * 读多写少，并发读，读写分离
	 * 内存占用问题。因为CopyOnWrite的写时复制机制，所以在进行写操作的时候，内存里会同时驻扎两个对象的内存，旧的对象和新写入的对象（注意:在复制的时候只是复制容器里的引用，只是在写的时候会创建新对象添加到新容器里，而旧容器的对象还在使用，所以有两份对象内存）。如果这些对象占用的内存比较大，比如说200M左右，那么再写入100M数据进去，内存就会占用300M，那么这个时候很有可能造成频繁的Yong
	 * GC和Full GC。之前我们系统中使用了一个服务由于每晚使用CopyOnWrite机制更新大对象，造成了每晚15秒的Full
	 * GC，应用响应时间也随之变长。
	 * 针对内存占用问题，可以通过压缩容器中的元素的方法来减少大对象的内存消耗，比如，如果元素全是10进制的数字，可以考虑把它压缩成36进制或64进制。或者不使用CopyOnWrite容器，而使用其他的并发容器，如ConcurrentHashMap。
	 * 数据一致性问题。CopyOnWrite容器只能保证数据的最终一致性，不能保证数据的实时一致性。所以如果你希望写入的的数据，马上能读到，请不要使用CopyOnWrite容器。
	 */
	private List<String> cowaList = new CopyOnWriteArrayList<String>();

	/** zookeeper服务器地址 */
	public static final String CONNECTION_ADDR = "127.0.0.1:2181,127.0.0.1:2183,127.0.0.1:2182";
	/** 定义session失效时间 */
	public static final int SESSION_TIMEOUT = 30000;

	public ZKWatcher() throws Exception {
		zk = new ZooKeeper(CONNECTION_ADDR, SESSION_TIMEOUT, this);
		System.out.println("开始连接ZK服务器");
		connectedSemaphore.await();
	}

	public void process(WatchedEvent event) {
		// 连接状态
		KeeperState keeperState = event.getState();
		// 事件类型
		EventType eventType = event.getType();
		// 受影响的path
		String path = event.getPath();
		System.out.println("受影响的path : " + path);

		if (KeeperState.SyncConnected == keeperState) {
			// 成功连接上ZK服务器
			if (EventType.None == eventType) {
				System.out.println("成功连接上ZK服务器");
				connectedSemaphore.countDown();
				try {
					if (this.zk.exists(PARENT_PATH, false) == null) {
						this.zk.create(PARENT_PATH, "root".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}
					List<String> paths = this.zk.getChildren(PARENT_PATH, true);
					for (String p : paths) {
						System.out.println(p);
						this.zk.exists(PARENT_PATH + "/" + p, true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 创建节点
			else if (EventType.NodeCreated == eventType) {
				System.out.println("节点创建");
				try {
					this.zk.exists(path, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 更新节点
			else if (EventType.NodeDataChanged == eventType) {
				System.out.println("节点数据更新");
				try {
					// update nodes call function
					this.zk.exists(path, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 更新子节点
			else if (EventType.NodeChildrenChanged == eventType) {
				System.out.println("子节点 ... 变更");
				try {
					List<String> paths = this.zk.getChildren(path, true);
					if (paths.size() >= cowaList.size()) {
						paths.removeAll(cowaList);
						for (String p : paths) {
							this.zk.exists(path + "/" + p, true);
							// this.zk.getChildren(path + "/" + p, true);
							System.out.println("这个是新增的子节点 : " + path + "/" + p);
							// add new nodes call function
						}
						cowaList.addAll(paths);
					} else {
						cowaList = paths;
					}
					System.out.println("cowaList: " + cowaList.toString());
					System.out.println("paths: " + paths.toString());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 删除节点
			else if (EventType.NodeDeleted == eventType) {
				System.out.println("节点 " + path + " 被删除");
				try {
					// delete nodes call function
					this.zk.exists(path, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else
				;
		} else if (KeeperState.Disconnected == keeperState) {
			System.out.println("与ZK服务器断开连接");
		} else if (KeeperState.Unknown == keeperState) {
			System.out.println("权限检查失败");
		} else if (KeeperState.Expired == keeperState) {
			System.out.println("会话失效");
		} else
			;

		System.out.println("--------------------------------------------");
	}

}
