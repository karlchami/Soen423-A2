package backend;

import Store.StoreImpl;
import StoreApp.DSMS;
import StoreApp.DSMSHelper;
import models.Store;

import java.text.ParseException;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


public class ONServer {
    public static void main(String[] args) {
        try{

            String[] arguments = new String[] {"-ORBInitialPort","1234","-ORBInitialHost","localhost"};
            ORB orb = ORB.init(arguments, null);
            POA rootpoa= POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
            StoreImpl server= new StoreImpl(Store.QC,orb);

            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(server);
            DSMS href = (DSMS) DSMSHelper.narrow(ref);

            org.omg.CORBA.Object objRef=
                    orb.resolve_initial_references("NameService");

            NamingContextExt ncRef= NamingContextExtHelper.narrow(objRef);

            String name = "ON";
            NameComponent path[] = ncRef.to_name( name );
            ncRef.rebind(path, href);
            System.out.println("ON Store running.");

            Runnable task = () -> {
				try {
					server.receive();
				} catch (NumberFormatException | ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
            Thread thread = new Thread(task);
            thread.start();
            orb.run();
        }
        catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
        System.out.println("ON Store closing.");
    }
}

