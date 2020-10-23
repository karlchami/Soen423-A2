package Store;

import models.Customer;
import models.Manager;
import models.Store;

import StoreApp.DSMSPOA;
import org.omg.CORBA.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.net.SocketException;
import java.util.*;


public class StoreImpl extends DSMSPOA {
    private ORB orb;
    // Store item details in such order : Name, Quantity, Price
    private Map<String, String> itemStore = new HashMap<String, String>();
    private Map<String, PriorityQueue<String>> itemWaitList = new HashMap<String, PriorityQueue<String>>();
    private HashMap<String, Customer> Customers = new HashMap<String, Customer>();
    private HashMap<String, Manager> Managers = new HashMap<String, Manager>();
    private HashMap<String, Integer> ports = new HashMap<String, Integer>();
    private ArrayList<String> purchaseLog = new ArrayList<String>();
    private Store store;
    private Logger logger = null;
    
    public StoreImpl(Store store, ORB orb) throws IOException {
        super();
        this.orb = orb;
        this.store = store;
        this.ports.put("QC", 5555);
        this.ports.put("BC", 4444);
        this.ports.put("ON", 7777);
        this.logger = this.launchLogger();
        logger.info("Store server " + this.store.toString()+ " is now running.");
    }
    public Logger launchLogger() {
        Logger logger = Logger.getLogger("ServerLog");
        FileHandler fh;
        try {
            fh = new FileHandler("src/logs/server/"+this.store.toString()+"_server.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }
	@Override
	public boolean addItem(String managerID, String itemID, String itemName, int quantity, int price) {
		// If item already exists in store modify quantity
        if (this.itemStore.containsKey(itemID)) {
            String[] item_details = this.itemStore.get(itemID).split(",");
            // Index 1 to get quantity from itemStore
            int current_quantity = Integer.parseInt(item_details[1]);
            int new_quantity = current_quantity + quantity;
            item_details[1] = Integer.toString(new_quantity);
            itemStore.replace(itemID, String.join(",", item_details));
            logger.info("Manager " + managerID + " updated item " + itemID + "in " + this.store + " store.");

        } else {
        	// If item does not exist just add it to itemStore
        	itemStore.put(itemID, itemName + "," + quantity + "," + price);
            logger.info("Manager " + managerID + " created item " + itemID + "in " + this.store + " store.");

        }
        // If item is in waiting list automatically purchase item for wait-listed customers for the available quantity
        if(this.itemWaitList.containsKey(itemID)){
            PriorityQueue<String> client_queue = this.itemWaitList.get(itemID);
            for(String clientID : client_queue){
            	// If client in local store
                if(clientID.startsWith(this.store.toString())){
                    this.purchaseItem(clientID, itemID, new Date().toString());
                }
                // If client in another store
                else{
                    int port = this.ports.get(clientID.substring(0,2));
                    String cmd = "PURCHASE-ITEM,"+ clientID + "," +itemID+ ","+new Date().toString();
                    this.sendCommand(port, cmd);
                }
            }
        }
        return true;
	}
	@Override
	public boolean removeItem(String managerID, String itemID, int quantity) {
		// If item exists in itemStore
        if (this.itemStore.containsKey(itemID)) {
        	// If quantity set to 0 means remove item
            if(quantity == 0){
                this.itemStore.remove(itemID);
                logger.info("Manager " + managerID + " removed item " + itemID + "in " + this.store + " store.");
                return true;
            }
            // If quantity positive treat the request
            else if(quantity > 0) {
                String[] item_details = this.itemStore.get(itemID).split(",");
                int current_quantity = Integer.parseInt(item_details[1]);
                int new_quantity = current_quantity - quantity;
                // If resulting quantity is 0 or less remove item
                if(new_quantity <= 0) {
                	this.itemStore.remove(itemID);
                    logger.info("Manager " + managerID + " removed item " + itemID + "in " + this.store + " store.");
                    return true;
                }
                item_details[1] = Integer.toString(new_quantity);
                itemStore.replace(itemID, String.join(",", item_details));
                logger.info("Manager " + managerID + " removed (" + quantity + " items) " + itemID + "in " + this.store + " store.");
                return true;
            }            
            System.out.println("You cannot enter a negative quantity.");
            logger.info("Manager " + managerID + " entered a negative quantity to remove item " + itemID + "in " + this.store + " store.");
            return false;
        } else {
        	// If item does not exist in itemStore
            System.out.println("This item does not exist in the " + this.store + " store.");
            logger.info("Manager " + managerID + " attempted to remove no existing item:" + itemID + "in " + this.store + " store.");
            return false;
        }
	}
	@Override
	public String listItemAvailability(String managerID) {
        logger.info("Manager " + managerID + " requested a list of all items in " + this.store + " store.");
        String item_availability = "";
        for (Map.Entry<String,String> entry: this.itemStore.entrySet()){
        	item_availability += entry.getKey() + ":" +entry.getValue() +"\n";
        }
        return item_availability;
	}
	@Override
	public String purchaseItem(String customerID, String itemID, String dateOfPurchase) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String findItem(String customerID, String itemName) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean returnItem(String customerID, String itemID, String dateOfReturn) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean exchangeItem(String customerID, String newitemID, String oldItemID, String dateOfExchange) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void addCustomerWaitList(String customerID, String itemID) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addCustomer(String customerID) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addManager(String managerID) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
	private void sendCommand(int port, String message) {
		// TODO Auto-generated method stub
		
	}

}
