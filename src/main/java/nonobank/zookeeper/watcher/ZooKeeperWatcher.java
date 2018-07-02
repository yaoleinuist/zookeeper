package nonobank.zookeeper.watcher;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * @ClassName: ZooKeeperWatcher
 * @Description: watcher
 * @author yaolei
 * @date 2018年5月27日
 *
 */
public class ZooKeeperWatcher implements Watcher {

	/** 定义原子变量 */
	AtomicInteger seq = new AtomicInteger();
	/** 定义session失效时间 */
	private static final int SESSION_TIMEOUT = 10000;
	/** zookeeper服务器地址 */
	private static final String CONNECTION_ADDR = "127.0.0.1:2181,127.0.0.1:2183,127.0.0.1:2182";
	/** zk父路径设置 */
	private static final String PARENT_PATH = "/testWatch";
	/** zk子路径设置 */
	private static final String CHILDREN_PATH = "/testWatch/children";
	/** 进入标识 */
	private static final String LOG_PREFIX_OF_MAIN = "【Main】";
	/** zk变量 */
	private ZooKeeper zk = null;
	/** 信号量设置，用于等待zookeeper连接建立之后 通知阻塞程序继续向下执行 */
	private CountDownLatch connectedSemaphore = new CountDownLatch(1);

	/**
	 * 创建ZK连接
	 * 
	 * @param connectAddr
	 *            ZK服务器地址列表
	 * @param sessionTimeout
	 *            Session超时时间
	 */
	public void createConnection(String connectAddr, int sessionTimeout) {
		// 有可能未释放
		this.releaseConnection();
		try {
			zk = new ZooKeeper(connectAddr, sessionTimeout, this);
			System.out.println(LOG_PREFIX_OF_MAIN + "开始连接ZK服务器");
			connectedSemaphore.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭ZK连接
	 */
	public void releaseConnection() {
		if (this.zk != null) {
			try {
				this.zk.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 创建节点
	 * 
	 * @param path
	 *            节点路径
	 * @param data
	 *            数据内容
	 * @return
	 */
	public boolean createPath(String path, String data) {
		try {
			// 设置监控(由于zookeeper的监控都是一次性的所以 每次必须设置监控),节点还没有的时候，就可以进行监控,提前监控
			this.zk.exists(path, true);
			System.out.println(LOG_PREFIX_OF_MAIN + "节点创建成功, Path: " + this.zk.create( /** 路径 */
					path,
					/** 数据 */
					data.getBytes(),
					/** 所有可见 */
					Ids.OPEN_ACL_UNSAFE,
					/** 永久存储 */
					CreateMode.PERSISTENT) + ", content: " + data);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 读取指定节点数据内容
	 * 
	 * @param path
	 *            节点路径
	 * @return
	 */
	public String readData(String path, boolean needWatch) {
		try {
			return new String(this.zk.getData(path, needWatch, null));
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 更新指定节点数据内容
	 * 
	 * @param path
	 *            节点路径
	 * @param data
	 *            数据内容
	 * @return
	 */
	public boolean writeData(String path, String data) {
		try {
			System.out.println(LOG_PREFIX_OF_MAIN + "更新数据成功，path：" + path + ", stat: "
					+ this.zk.setData(path, data.getBytes(), -1));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 删除指定节点
	 * 
	 * @param path
	 *            节点path
	 */
	public void deleteNode(String path) {
		try {
			this.zk.delete(path, -1);
			System.out.println(LOG_PREFIX_OF_MAIN + "删除节点成功，path：" + path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断指定节点是否存在
	 * 
	 * @param path
	 *            节点路径
	 */
	public Stat exists(String path, boolean needWatch) {
		try {
			return this.zk.exists(path, needWatch);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取子节点
	 * 
	 * @param path
	 *            节点路径
	 */
	private List<String> getChildren(String path, boolean needWatch) {
		try {
			return this.zk.getChildren(path, needWatch);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 删除所有节点
	 */
	public void deleteAllTestPath() {
		if (this.exists(CHILDREN_PATH, false) != null) {
			this.deleteNode(CHILDREN_PATH);
		}
		if (this.exists(PARENT_PATH, false) != null) {
			this.deleteNode(PARENT_PATH);
		}
	}

	/**
	 * 收到来自Server的Watcher通知的事件类型后的处理。
	 */
	public void process(WatchedEvent event) {

		System.out.println("进入 process 。。。。。event = " + event);

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (event == null) {
			return;
		}

		// 连接状态
		KeeperState keeperState = event.getState();
		// 事件类型
		EventType eventType = event.getType();
		// 受影响的path
		String path = event.getPath();
		// watch线程，触发了几次，计数
		String logPrefix = "【Watcher-" + this.seq.incrementAndGet() + "】";

		System.out.println(logPrefix + "收到Watcher通知");
		System.out.println(logPrefix + "连接状态:\t" + keeperState.toString());
		System.out.println(logPrefix + "事件类型:\t" + eventType.toString());

		if (KeeperState.SyncConnected == keeperState) {
			// 成功连接上ZK服务器
			if (EventType.None == eventType) {
				System.out.println(logPrefix + "成功连接上ZK服务器");
				connectedSemaphore.countDown();
			}
			// 创建节点
			else if (EventType.NodeCreated == eventType) {
				System.out.println(logPrefix + "节点创建");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 继续watch,需要每次都要监控
				this.exists(path, true);
			}
			// 更新节点
			else if (EventType.NodeDataChanged == eventType) {
				System.out.println(logPrefix + "节点数据更新");
				System.out.println("我看看走不走这里........");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(logPrefix + "数据内容: " + this.readData(PARENT_PATH, true));
			}
			// 更新子节点
			else if (EventType.NodeChildrenChanged == eventType) {
				System.out.println(logPrefix + "子节点变更");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(logPrefix + "子节点列表：" + this.getChildren(PARENT_PATH, true));
			}
			// 删除节点
			else if (EventType.NodeDeleted == eventType) {
				System.out.println(logPrefix + "节点 " + path + " 被删除");
			} else
				;
		} else if (KeeperState.Disconnected == keeperState) {
			System.out.println(logPrefix + "与ZK服务器断开连接");
		} else if (KeeperState.Unknown == keeperState) {
			System.out.println(logPrefix + "权限检查失败");
		} else if (KeeperState.Expired == keeperState) {
			System.out.println(logPrefix + "会话失效");
		} else
			;

		System.out.println("--------------------------------------------");

	}

	/**
	 * <B>方法名称：</B>测试zookeeper监控<BR>
	 * <B>概要说明：</B>主要测试watch功能<BR>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// 建立watcher
		ZooKeeperWatcher zkWatch = new ZooKeeperWatcher();
		// 创建连接
		zkWatch.createConnection(CONNECTION_ADDR, SESSION_TIMEOUT);
		// System.out.println(zkWatch.zk.toString());

		Thread.sleep(1000);

	    zkWatch.deleteAllTestPath();

		if (zkWatch.createPath(PARENT_PATH, System.currentTimeMillis() + "")) {

			Thread.sleep(1000);

			// 读取数据，在操作节点数据之前先调用zookeeper的getData()方法是为了可以watch到对节点的操作。watch是一次性的，
			// 也就是说，如果第二次又重新调用了setData()方法，在此之前需要重新调用一次。

			// 读取数据，
//			System.out.println("---------------------- 1----------------------------");
			zkWatch.readData(PARENT_PATH, true);
			zkWatch.writeData(PARENT_PATH, "测试");

			//zkWatch.exists(PARENT_PATH, true);
			/**
			 * 读取子节点，设置对子节点变化的watch，如果不写该方法，则在创建子节点是只会输出NodeCreated，而不会输出NodeChildrenChanged，
			 * 也就是说创建子节点时没有watch。 如果是递归的创建子节点，如path="/p/c1/c2"的话，getChildren(PARENT_PATH,
			 * ture)只会在创建c1时watch，输出c1的NodeChildrenChanged，
			 * 而不会输出创建c2时的NodeChildrenChanged，如果watch到c2的NodeChildrenChanged，则需要再调用一次getChildren(String
			 * path, true)方法， 其中path="/p/c1"
			 * getChildren()方法仅仅监控对应节点直接子目录的一次变化，但是只会监控直接子节点的增减情况，不会监控数据变化情况！若要每次对应节点发生增减变化都被监测到，那么每次都得先调用getChildren()方法获取一遍节点的子节点列表！
			 * getData,exist,getChildren
			 **/
			// 读取子节点，相对于父节点，父节点下面增删改，会有通知,true是否监听子节点
			// System.out.println("---------------------- 2 ----------------------------");
			//   zkWatch.getChildren(PARENT_PATH, true);
			// System.out.println("---------------------- 3 ----------------------------");
			//
			// // 更新数据,需要readData进行watch操作，不然没有watch
			//zkWatch.writeData(PARENT_PATH, System.currentTimeMillis() + "");
			// System.out.println("---------------------- 4 ----------------------------");
			//
			Thread.sleep(1000);

			// 创建子节点,zkWatch.getChildren(PARENT_PATH, true);也会受到通知
//			zkWatch.exists(CHILDREN_PATH, true);
//			zkWatch.createPath(CHILDREN_PATH, System.currentTimeMillis() + "");
//			zkWatch.getChildren(CHILDREN_PATH, true);
		//	zkWatch.createPath(CHILDREN_PATH + "/c1", System.currentTimeMillis() + "");
			// zkWatch.createPath(CHILDREN_PATH+"/c1/c2", System.currentTimeMillis() + "");

			// System.out.println("---------------------- 5h ----------------------------");
//			
//			 Thread.sleep(1000);
//			 zkWatch.readData(CHILDREN_PATH, true);
//			 zkWatch.writeData(CHILDREN_PATH, System.currentTimeMillis() + "");
//			 System.out.println("---------------------- 6 ----------------------------");

		}

		Thread.sleep(5000);
		// // 清理节点
	//	 zkWatch.deleteAllTestPath();
		 Thread.sleep(1000);
	 	zkWatch.releaseConnection();
	}

}