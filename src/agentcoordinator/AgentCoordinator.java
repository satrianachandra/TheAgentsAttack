/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentcoordinator;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Ethan_Hunt
 */
public class AgentCoordinator extends GuiAgent {

    
    public static final int MESSAGE_RECEIVED = 1;
    public static final int MESSAGE_SENT = 2;
    //an example of adding 1 remote platforms
    public AID remoteDF;
    
    private AgentCoordinatorUI agentUI;
    private List<AgentController> agentsList;
    
    protected void setup() {
            /** Registration with the DF */
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("CoordinatorAgent");
            sd.setName(getName());
            sd.setOwnership("JADE");
            sd.addOntologies("CoordinatorAgent");
            dfd.setName(getAID());

            dfd.addServices(sd);
            try {
            DFService.register(this,dfd);
            } catch (FIPAException e) {
            System.err.println(getLocalName()+" registration with DF unsucceeded. Reason: "+e.getMessage());
            //doDelete();
            }
            /*
            AID aDF = new AID("df@Platform2",AID.ISGUID);
            aDF.addAddresses("http://sakuragi:54960/acc");
            */
            agentUI = new AgentCoordinatorUI();
            agentUI.setAgent(this);
            agentUI.setTitle("Coordinator Agent " + this.getName());
            //try {
                //RefetchAgentsList();
                //RA1.populateAgentsListOnGUI();
            //} catch (FIPAException ex) {
                //Logger.getLogger(AgentCommGUI.class.getName()).log(Level.SEVERE, null, ex);
            //}
            ReceiveMessage rm = new ReceiveMessage(this);
            addBehaviour(rm);
        }
    
    public List<AID> findAgents(AID dfAID) throws FIPAException{
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType("AgentSmith");
        template.addServices(templateSd);
        SearchConstraints sc = new SearchConstraints();
        // We want to receive 10 results at most
        //sc.setMaxResults(new Long(20));
        sc.setMaxDepth(1L);
        DFAgentDescription[] results = DFService.search(this,dfAID, template, sc);
        
        List<AID> myAgentsList = new ArrayList<AID>();
        if (results.length>0){
            for(int i=0;i<results.length;i++){
                DFAgentDescription agentDesc = results[i];
                AID provider = agentDesc.getName();
                myAgentsList.add(provider);
           }   
        }
        return myAgentsList;
    }

    
    @Override
    protected void onGuiEvent(GuiEvent ge) {
        //
    }
    
    public void startAgentSmiths(int numberOfAgents, long interval, String serverAddress, int serverPort ){
        // Get a hold on JADE runtime
        Runtime rt = Runtime.instance();

        // Exit the JVM when there are no more containers around
        rt.setCloseVM(true);
        System.out.print("runtime created\n");

        // Create a default profile
        /*
        Profile profile = new ProfileImpl(null, 1200, null);
        System.out.print("profile created\n");

        System.out.println("Launching a whole in-process platform..."+profile);
        jade.wrapper.AgentContainer mainContainer = rt.createMainContainer(profile);
        */
        
        // now set the default Profile to start a container
        ProfileImpl pContainer = new ProfileImpl(null, 1099, null);  
        jade.wrapper.AgentContainer cont = rt.createAgentContainer(pContainer);
        System.out.println("Launching the agent container after ..."+pContainer);

        System.out.println("containers created");
        System.out.println("Launching Agent Smith on the main container ...");
        
        agentsList = new ArrayList<AgentController>();
        AgentController agentSmith;
        for (int i=0;i<numberOfAgents;i++){
            try {
                Object[] smithArgs = new Object[4];
                smithArgs[0] = interval;
                smithArgs[1] = serverAddress;
                smithArgs[2] = serverPort;
                smithArgs[3] = getAID(); //the coordinator's aid
                agentSmith = cont.createNewAgent("Smith-"+i,
                        "agentsmith.AgentSmith", smithArgs);
                agentSmith.start();
                agentsList.add(agentSmith);
            } catch (StaleProxyException ex) {
                Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }   
        }
        
    }
    
    public void killAllAgentSmith() throws StaleProxyException{
        for (int i=0;i<agentsList.size();i++){
            agentsList.get(i).kill();
        }
    }
    
}
