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
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Terminal;


/**
 *
 * @author Ethan_Hunt
 */
public class AgentCoordinator extends GuiAgent {

    private static final String COMMA=",";
    public static final int MESSAGE_RECEIVED = 1;
    public static final int MESSAGE_SENT = 2;
    //an example of adding 1 remote platforms
    public AID remoteDF;
    private List<jade.wrapper.AgentContainer> mainContainersList;
    private List<Process>listOfProcesses;
    
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
        //Runtime rt = Runtime.instance();
        // Exit the JVM when there are no more containers around
        //rt.setCloseVM(true);
        //System.out.print("runtime created\n");
        
        int numberOfAgentPerPlatform = 1000;
        int numberOfPlatforms = numberOfAgents/numberOfAgentPerPlatform;
        int remainderAgents = numberOfAgents%numberOfAgentPerPlatform;
        int startingPort=1100;
        mainContainersList = new ArrayList<>();
        agentsList = new ArrayList<>();
        listOfProcesses = new ArrayList<>();
        AgentController agentSmith;
        createPlatformContainerAgents(numberOfPlatforms,startingPort,
            numberOfAgentPerPlatform,interval,serverAddress,serverPort);
        
        //start the remaining agents on another container
        if (remainderAgents!=0){
            createPlatformContainerAgents(1,startingPort+numberOfPlatforms,
                remainderAgents,interval,serverAddress,serverPort);
        }
    }
    
    public void killAllAgentSmith() throws StaleProxyException{
        for (int i=0;i<listOfProcesses.size();i++){
            listOfProcesses.get(i).destroy();
        }
        
    }
    private void createPlatformContainerAgents(int numberOfPlatforms,int startingPort,
            int numberOfAgentPerContainers,long interval, String serverAddress, int serverPort){
        AgentController agentSmith;
        for (int i=0;i<numberOfPlatforms;i++){
            //Start a platform
            String createPlatformString = "java jade.Boot -gui -port "+(startingPort+i)+" -platform-id "+"Platform-"+i;
            System.out.println(createPlatformString);
            Process p = Terminal.executeNoError(createPlatformString);
            
            //listOfProcesses.add(p);
            /*
            //Profile mProfile = new ProfileImpl("192.168.0.102", startingPort+i,"Platform-"+i+":"+(startingPort+i),false);
            Profile mProfile = new ProfileImpl("192.168.0.102", startingPort+i,null);
            jade.wrapper.AgentContainer mainContainer = rt.createMainContainer(mProfile);
            System.out.println("main container created "+mainContainer);
            mainContainersList.add(mainContainer);
            */
            
            
            
            //ProfileImpl pContainer = new ProfileImpl(null, startingPort+i,null);
            //jade.wrapper.AgentContainer agentContainer = rt.createAgentContainer(pContainer);
            //System.out.println("containers created "+pContainer);
            StringBuffer agentsListString = new StringBuffer();
            for (int j=0;j<numberOfAgentPerContainers;j++){
                /*
                try {
                    Object[] smithArgs = new Object[4];
                    smithArgs[0] = interval;
                    smithArgs[1] = serverAddress;
                    smithArgs[2] = serverPort;
                    smithArgs[3] = getAID(); //the coordinator's aid
                    agentSmith = agentContainer.createNewAgent("Platform-"+i+"_Smith-"+j,
                            "agentsmith.AgentSmith", smithArgs);
                    agentSmith.start();
                    agentsList.add(agentSmith);
                } catch (StaleProxyException ex) {
                    Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
                } */
                String coordinatorName = getAID().getName();
                String coordinatorAddress = getAID().getAddressesArray()[0];
                agentsListString.append("Platform-"+i+"_Smith-"+j+":agentsmith.AgentSmith("+interval
                        +COMMA+serverAddress+COMMA+serverPort+COMMA+coordinatorName
                        +COMMA+coordinatorAddress+");");
            }
            //String createAgentsString = "cd C:\\Users\\Ethan_Hunt\\Documents\\NetBeansProjects\\TheAgentsAttack\\src\njava jade.Boot -port "+(startingPort+i)+" -container \""+agentsListString.toString()+"\"";
            String createAgentsString = "cd /home/ubuntu/Codes/TheAgentsAttack/src && java jade.Boot -port "+(startingPort+i)+" -container \""+agentsListString.toString()+"\"";
            System.out.println(createAgentsString);
            Process p2 = Terminal.executeNoError(createAgentsString);
            //System.out.println("java jade.Boot -port "+(startingPort+i)+" -container \""+agentsListString.toString()+"\"");
            /*
            final File file = new File("createAgents.bat");
            try {
                file.createNewFile();
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                writer.println(createAgentsString);
                writer.close();
                Terminal.executeNoError("start createAgents.bat");
            } catch (IOException ex) {
                Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
            */
            
            
        }
    }
    
}
