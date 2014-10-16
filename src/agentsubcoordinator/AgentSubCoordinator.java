/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentsubcoordinator;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import java.util.List;
import jade.core.Runtime;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.KillContainer;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import messageclasses.SmithParameter;
/**
 *
 * @author Ethan_Hunt
 */
public class AgentSubCoordinator extends Agent {
    
    private List<jade.wrapper.AgentContainer> mainContainersList;
    private List<AgentController> agentsList;
    private int platformNumber=0;
    public static jade.wrapper.ContainerController agentContainer;
    
    @Override
    protected void setup() {
        ReceiveMessage rm = new ReceiveMessage();
        addBehaviour(rm);
    }
    
    //@Override
    //protected void takeDown(){
        
    //}
    
    private void startAgentSmiths(int numberOfAgents, long interval, String serverAddress, int serverPort ){
        
        // Get a hold on JADE runtime
        Runtime rt = Runtime.instance();
        // Exit the JVM when there are no more containers around
        rt.setCloseVM(true);
        System.out.print("runtime created\n");
        
        mainContainersList = new ArrayList<>();
        agentsList = new ArrayList<>();
        //listOfProcesses = new ArrayList<>();
        AgentController agentSmith;
        //Profile mProfile = new ProfileImpl("192.168.0.102", startingPort+i,"Platform-"+i+":"+(startingPort+i),false);
        //Profile mProfile = new ProfileImpl("192.168.0.102", startingPort+i,null);
        //jade.wrapper.AgentContainer mainContainer = rt.createMainContainer(mProfile);
        //System.out.println("main container created "+mainContainer);
        //mainContainersList.add(mainContainer);

        ProfileImpl pContainer = new ProfileImpl();//null, startingPort+i,null);
        jade.wrapper.AgentContainer agentContainer = rt.createAgentContainer(pContainer);
        mainContainersList.add(agentContainer);
        System.out.println("containers created "+pContainer);
        for (int j=0;j<numberOfAgents;j++){
            try {
                Object[] smithArgs = new Object[4];
                smithArgs[0] = interval;
                smithArgs[1] = serverAddress;
                smithArgs[2] = serverPort;
                smithArgs[3] = getAID(); //the coordinator's aid
                agentSmith = agentContainer.createNewAgent("Platform-"+platformNumber+"_Smith-"+j,
                        "agentsmith.AgentSmith", smithArgs);
                agentSmith.start();
                agentsList.add(agentSmith);
            } catch (StaleProxyException ex) {
                Logger.getLogger(AgentSubCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    private void killAllAgentSmith() throws StaleProxyException{
        for (int i=0;i<mainContainersList.size();i++){
            mainContainersList.get(i).kill();
        }
        
    }
    
    private List<AID> findAgents(AID dfAID) throws FIPAException{
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

    
    public class SendMessage extends OneShotBehaviour {
        
        private ACLMessage msg;
        
        private AgentSubCoordinator myAgent;
        
        public SendMessage(ACLMessage msg,AgentSubCoordinator myAgent){
            super();
            this.msg = msg;
        }
        
        @Override
        public void action() {
            send(msg);
            System.out.println("****I Sent Message to::> R1 *****"+"\n"+
                                "The Content of My Message is::>"+ msg.getContent());
            
            //GuiEvent ge = new GuiEvent(this, AgentCommGUI.MESSAGE_SENT);
            //ge.addParameter(msg);
            //myAgent.postGuiEvent(ge);
        }
    }
    
    public class ReceiveMessage extends CyclicBehaviour {
   // Variable to Hold the content of the received Message
    private String Message_Performative;
    private String Message_Content;
    private String SenderName;
    private String MyPlan;
    private SmithParameter sp;
    
    public void action() {
        ACLMessage msg = receive();
        if(msg != null) {

            Message_Performative = msg.getPerformative(msg.getPerformative());
            Message_Content = msg.getContent();
            SenderName = msg.getSender().getLocalName();
            
            if (msg.hasByteSequenceContent()){
            try {
                
                if (msg.getContentObject()!=null){
                    sp = (SmithParameter)msg.getContentObject();
                    if (Message_Performative.equals("REQUEST") && sp.type==1 && sp.numberOfAgent>0 ){
                        startAgentSmiths(sp.numberOfAgent,sp.interval, sp.serverAddress, sp.serverPort );
                    }else if (Message_Performative.equals("REQUEST") && sp.type==2){
                        killAgents();
                    }
                }
            } catch (UnreadableException ex) {
                Logger.getLogger(AgentSubCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
            System.out.println(" ****I Received a Message***" +"\n"+
                    "The Sender Name is::>"+ SenderName+"\n"+
                    "The Content of the Message is::> " + sp.toString() + "\n"+
                    "::: And Performative is::> " + Message_Performative + "\n");
            System.out.println("ooooooooooooooooooooooooooooooooooooooo");

            

        }

    }
    
    
    }
    
    public static void main(String[]args){
        // Get a hold on JADE runtime
        Runtime rt = Runtime.instance();
        // Exit the JVM when there are no more containers around
        rt.setCloseVM(true);
        System.out.print("runtime created\n");
        
        ProfileImpl mProfile = new ProfileImpl();
        jade.wrapper.AgentContainer mainContainer = rt.createMainContainer(mProfile);
        
        //start the RMA agent
        //starting RMA agent for monitoring purposes
        try {
            AgentController agentRMA = mainContainer.createNewAgent("RMA","jade.tools.rma.rma", null);
            agentRMA.start();
        } catch (StaleProxyException ex) {
            Logger.getLogger(AgentSubCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        ProfileImpl pContainer = new ProfileImpl();//null, startingPort+i,null);
        agentContainer = rt.createAgentContainer(pContainer);        
        
        //Start the local Agent SubCoordinator
        AgentController agentSubCoodinator;
        Object[] subCoordArgs = new Object[1];
        try {
            agentSubCoodinator = agentContainer.createNewAgent("SC",
                    "agentsubcoordinator.AgentSubCoordinator", subCoordArgs);
            agentSubCoodinator.start();
        } catch (StaleProxyException ex) {
            Logger.getLogger(AgentSubCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public void killAContainer(ContainerID cid) throws Codec.CodecException, OntologyException {
        //creating the request
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.setLanguage(new SLCodec().getName());
        request.setOntology(JADEManagementOntology.getInstance().getName());
        
        //create the action
        KillContainer kill = new KillContainer();
        kill.setContainer(cid);
        
        //sending the request and action to AMS
        
        getContentManager().fillContent(request, new Action(getAMS(), kill));
        request.addReceiver(getAMS());
        send(request);

    }
    
    private void killAgents(){
        try {
            /*
            try {
            killAContainer((ContainerID)here());
            } catch (Codec.CodecException ex) {
            Logger.getLogger(AgentSubCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
            Logger.getLogger(AgentSubCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }*/
            agentContainer.kill();
        } catch (StaleProxyException ex) {
            Logger.getLogger(AgentSubCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
