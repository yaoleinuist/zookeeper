package nonobank.curator.select;


/**
* @ClassName: Client1  
* @Description: 选主
* @author yaolei  
* @date 2018年6月30日  
*
 */
public class Client1 {

	public static void main(String[] args) throws Exception{
		
		SelectMaster master = new SelectMaster("client1");
		Thread.sleep(Integer.MAX_VALUE);
	}
}
