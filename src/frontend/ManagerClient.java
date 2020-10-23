package frontend;

import StoreApp.*;
import models.Store;
import models.Manager;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ManagerClient{
    static DSMS dsms;
    private String managerID;
    private Store store;
    private static Logger logger = null;
    public void Manager(String managerID, Store store) throws Exception {
        this.managerID = managerID;
        this.store = store;
        this.logger = this.launchLogger();
    }
    public Logger launchLogger() {
        Logger logger = Logger.getLogger("ManagerLog");
        FileHandler fh;
        try {
            fh = new FileHandler("src/logs/client/" + this.managerID + ".log");
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

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose store location:");
        String input = scanner.next();
        Store store = null;
        switch (input) {
	        case "BC":
	        	store = Store.BC;
	        	break;
            case "ON":
            	store = Store.ON;
                break;
            case "QC":
            	store = Store.QC;
                break;

        }
        System.out.println("Enter Manager ID: ");
        String IDNumber = scanner.next();
        String clientID = store.toString() + "M" + IDNumber;
        System.out.println("Manager ID: " + clientID);
        Manager manager = new Manager(clientID, store);
        try{
            String[] arguments = new String[] {"-ORBInitialPort","1234","-ORBInitialHost","localhost"};
            ORB orb = ORB.init(arguments, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            dsms = (DSMS) DSMSHelper.narrow(ncRef.resolve_str(manager.getStore().toString()));
            dsms.addCustomer(manager.getManagerID());
            int customerOption;
            String itemID;
            String itemName;
            int price;
            int quantity;
            while (true) {
                System.out.println("What would you like to do?");
                System.out.println("1. Add Item");
                System.out.println("2. Remove Item ");
                System.out.println("3. List Available Items ");
                customerOption = scanner.nextInt();
                switch(customerOption){
                    case 1:
                        System.out.println("----ADD ITEM----");
                        System.out.println("Enter ID:");
                        itemID = scanner.next();
                       System.out.println("Enter name:");
                        itemName = scanner.next();
                        System.out.println("Enter price:");
                        price = scanner.nextInt();
                        System.out.println("Enter quantity:");
                        quantity = scanner.nextInt();
                        logger.info("Manager client with ID: "+ manager.getManagerID() +" " + "attempt to add item: "+itemID);
                        dsms.addItem(manager.getManagerID(), itemID, itemName, quantity, price);
                        System.out.println("Added item successfully");
                        break;
                    case 2:
                        System.out.println("----REMOVE ITEM----");
                        System.out.println("Enter ID:");
                        itemID = scanner.next();
                        System.out.println("Enter quantity:");
                        quantity = scanner.nextInt();
                        logger.info("Manager ID "+ manager.getManagerID() + " " + "attempt to remove: "+itemID);
                        dsms.removeItem(manager.getManagerID(), itemID, quantity);
                        System.out.println("Removed item successfully");
                        break;
                    case 3:
                        System.out.println("----LIST AVAILABE ITEM----");
                        logger.info("Manager "+ manager.getManagerID() + " " + "attempt to view available items.");
                        System.out.println(dsms.listItemAvailability(manager.getManagerID()));
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
        }
    }
}
