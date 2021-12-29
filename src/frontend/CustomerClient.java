package frontend;

import StoreApp.DSMS;
import StoreApp.DSMSHelper;
import models.Store;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CustomerClient {
    static DSMS dsms;
    private String customerID;
    private Store store;
    private Logger logger = null;

    public CustomerClient(String customerID, Store store) {
        this.customerID = customerID;
        this.store = store;
        this.logger = startLogger();
        logger.info("Customer " + this.customerID + " created.");
    }

    public Logger startLogger() {
        Logger logger = Logger.getLogger("CustomerLog");
        FileHandler fh;
        try {
            fh = new FileHandler("C:\\Users\\karlc\\Desktop\\soen423-a2\\src\\logs\\client\\" + this.customerID + ".log");
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

    public static void main(String[] args) throws ParseException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Store location (QC, ON, BC): ");
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
        System.out.println("Enter Client ID:");
        String IDNumber = scanner.next();
        String clientID = null;
        clientID = store.toString() + "U" + IDNumber;
        System.out.println("Client ID:" + clientID);
        CustomerClient customer = new CustomerClient(clientID, store);
        try{
            String[] arguments = new String[] {"-ORBInitialPort","1234","-ORBInitialHost","localhost"};
            ORB orb = ORB.init(arguments, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            dsms = (DSMS) DSMSHelper.narrow(ncRef.resolve_str(customer.store.toString()));
            dsms.addCustomer(customer.customerID);
            int customerOption;
            String itemID;
            String inputDate;
            String itemName;
            String newItemID;
            while (true) {
                System.out.println("What would you like to do?");
                System.out.println("1. Purchase Item");
                System.out.println("2. Find Item ");
                System.out.println("3. Return Item ");
                System.out.println("4. Exchange Item ");
                customerOption = scanner.nextInt();
                switch(customerOption){
                    case 1:
                        System.out.println("----PURCHASE ITEM----");
                        System.out.println("Enter item ID:");
                        itemID = scanner.next();
                        scanner.nextLine();
                        System.out.println("Enter the date of purchase (MMMM dd, yyyy):");
                        inputDate = scanner.nextLine();
                        String result = dsms.purchaseItem(customer.customerID,itemID,inputDate);
                        System.out.println(result);
                        switch (result) {
                            case "Purchased":
                                System.out.println("Item purchased successfully");
                                break;
                            case "Insufficient funds":
                                System.out.println("Insufficient funds");
                                break;
                            case "Out of stock":
                                System.out.println("Item is out of stock, waitlist?");
                                String option = scanner.next();
                                if (option.equals("y")) {
                                    dsms.addCustomerWaitList(customer.customerID, itemID);
                                    System.out.println("Successfully waitlisted. Item will be automatically bought when available.");
                                }
                                break;
                            case "Does not exist":
                                System.out.println("Does not exist");

                                break;
                            default:
                                System.out.println(result);
                                break;
                        }
                        break;
                    case 2:
                        System.out.println("----FIND ITEM----");
                        System.out.println("Enter item name:");
                        itemName = scanner.next();
                        System.out.println(dsms.findItem(customer.customerID,itemName));
                        break;
                    case 3:
                        System.out.println("----RETURN ITEM----");
                        System.out.println("Enter item ID:");
                        itemID = scanner.next();
                        scanner.nextLine();
                        System.out.println("Enter the return date (MMMM dd, yyyy):");
                        inputDate = scanner.nextLine();
                        boolean returnResult = dsms.returnItem(customer.customerID,itemID,inputDate);
                        if (returnResult){
                            System.out.println("Successfully returned and refunded");
                        }
                        else{
                            System.out.println("Return failed");
                        }
                        break;
                    case 4:
                        System.out.println("----EXCHANGE ITEM ----");
                        System.out.println("Enter old item ID:");
                        itemID = scanner.next();
                        scanner.nextLine();
                        System.out.println("Enter new item ID:");
                        newItemID = scanner.next();
                        scanner.nextLine();
                        System.out.println("Enter return date (MMMM dd, yyyy):");
                        inputDate = scanner.nextLine();
                        boolean exchangeResult = dsms.exchangeItem(customer.customerID, newItemID, itemID, inputDate);
                        if (exchangeResult){
                            System.out.println("Successfully exchanged");
                        }
                        else{
                            System.out.println("Exchange failed");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("ERROR : " + e) ;
                e.printStackTrace(System.out);
            }
    }
}

