/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentcoordinator;

import agentsubcoordinator.AgentSubCoordinatorData;
import jade.core.AID;
import jade.core.ProfileImpl;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import messageclasses.SmithParameter;
import utils.Terminal;



/**
 *
 * @author Ethan_Hunt
 */
public class AgentCoordinator extends GuiAgent {

    public static final int MESSAGE_RECEIVED = 1;
    public static final int MESSAGE_SENT = 2;
    public static final int MESSAGE_LAUNCH_AGENTS = 3;
    public static final int MESSAGE_KILL_AGENTS = 4;
    
    public static final String SEMICOLON = ";";
    //an example of adding 1 remote platforms
    public AID remoteDF;
    private AgentCoordinatorUI agentUI;
    public static List<AgentSubCoordinatorData>listRemoteSubCoordinators;
    
    protected void setup() {
        /** Registration with the DF */
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("AgentCoordinator");
        sd.setName(getName());
        sd.setOwnership("JADE");
        sd.addOntologies("JADEAgent");
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
        ReceiveMessage rm = new ReceiveMessage();
        addBehaviour(rm);
    }
    
    
    
    @Override
    protected void onGuiEvent(GuiEvent ge) {
        int type = ge.getType();
        if (type == MESSAGE_LAUNCH_AGENTS){
            SmithParameter sp = (SmithParameter)ge.getParameter(0);
            //send message to the subcoordinator to launch agent
            System.out.println(sp.numberOfAgent);
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("SC", AID.ISLOCALNAME));
            msg.setLanguage("English");
            try {
                msg.setContentObject(sp);
            } catch (IOException ex) {
                Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
            //msg.setContent("launch agents");
            send(msg);
            
            //Now I can tell the subcoordinator in the remote platform to start the agents smiths
            ACLMessage msg2 = new ACLMessage(ACLMessage.REQUEST);
            msg2.setLanguage("English");
            msg2.addReceiver(listRemoteSubCoordinators.get(0).getAID());
            System.out.println("remote: "+listRemoteSubCoordinators.get(0).getAID().toString());
            sp.numberOfAgent=0; //set the number of agents depending on the requirement
            try {
                msg2.setContentObject(sp);
            } catch (IOException ex) {
                Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
            send(msg2);
            
        }
    }
    
    private void startAgentSmiths(int numberOfAgents, long interval, String serverAddress, int serverPort ){
        
    }
    
    public class SendMessage extends OneShotBehaviour {
        
        private ACLMessage msg;
        
        public SendMessage(ACLMessage msg){
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
            //postGuiEvent(ge);
        }
    }


    
    public class ReceiveMessage extends CyclicBehaviour {
   // Variable to Hold the content of the received Message
        private String Message_Performative;
        private String Message_Content;
        private String SenderName;
        private String MyPlan;

        public void action() {
            ACLMessage msg = receive();
            if(msg != null) {
                Message_Performative = msg.getPerformative(msg.getPerformative());
                Message_Content = msg.getContent();
                SenderName = msg.getSender().getLocalName();
                System.out.println(" ****I Received a Message***" +"\n"+
                        "The Sender Name is::>"+ SenderName+"\n"+
                        "The Content of the Message is::> " + Message_Content + "\n"+
                        "::: And Performative is::> " + Message_Performative + "\n");
                System.out.println("ooooooooooooooooooooooooooooooooooooooo");

                if (Message_Performative.equals("REQUEST")&& Message_Content.equals("") ){
                    ///stub
                }

            }

        } 
    }

    
    public static void main(String[]args){
        // Get a hold on JADE runtime
        Runtime rt = Runtime.instance();
        // Exit the JVM when there are no more containers around
        rt.setCloseVM(true);
        System.out.print("runtime created\n");
        
        //start main container
        ProfileImpl mProfile = new ProfileImpl(null,1099,null);
        jade.wrapper.AgentContainer mainContainer = rt.createMainContainer(mProfile);
        
        
        //starting RMA agent for monitoring purposes
        try {
            AgentController agentRMA = mainContainer.createNewAgent("RMA","jade.tools.rma.rma", null);
            agentRMA.start();
        } catch (StaleProxyException ex) {
            Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //creating container for other agents
        ProfileImpl pContainer = new ProfileImpl();//null, startingPort+i,null);
        jade.wrapper.AgentContainer agentContainer = rt.createAgentContainer(pContainer);
        
        //Start the Agent Coordinator
        AgentController agentCoordinator;
        Object[] coordinatorArgs = new Object[1];
        coordinatorArgs[0]="0";
        try {
            agentCoordinator = agentContainer.createNewAgent("TheCoordinator",
                    "agentcoordinator.AgentCoordinator", coordinatorArgs);
            agentCoordinator.start();
        } catch (StaleProxyException ex) {
            Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Start the local Agent SubCoordinator
        AgentController agentSubCoodinator;
        Object[] subCoordArgs = new Object[1];
        coordinatorArgs[0]="0";
        try {
            agentSubCoodinator = agentContainer.createNewAgent("SC",
                    "agentsubcoordinator.AgentSubCoordinator", subCoordArgs);
            agentSubCoodinator.start();
        } catch (StaleProxyException ex) {
            Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //Add remote machines
        listRemoteSubCoordinators = new ArrayList<>();
        AID remoteSubCoordinator = new AID("SC", AID.ISGUID);
        remoteSubCoordinator.addAddresses("htp://ip-172-30-1-158.eu-west-1.compute.internal:7778/acc");
        listRemoteSubCoordinators.add(new AgentSubCoordinatorData("172.30.1.158", remoteSubCoordinator));
        
        //Start the JADE platform and Agent Subcoordinator in the remote machines
        String hostname = listRemoteSubCoordinators.get(0).getMachineIP();
        /*
        String setClasspath = "export CLASSPATH=/home/ubuntu/JADE-COURSE-2014/jade/lib/commons-codec/commons-codec-1.3.jar:$CLASSPATH;"
                + " export CLASSPATH=/home/ubuntu/JADE-COURSE-2014/jade/lib/jadeExamples.jar:$CLASSPATH;"
                + " export CLASSPATH=/home/ubuntu/JADE-COURSE-2014/jade/lib/jade.jar:$CLASSPATH;";
        */
        String setClasspath = "export CLASSPATH=:$CLASSPATH;";
        String createPlatformAndAgents= " cd /home/ubuntu/Codes/TheAgentsAttack/src&&java agentsubcoordinator.AgentSubCoordinator;";
        Terminal.execute("ssh -X -o StrictHostKeyChecking=no -i /home/ubuntu/aws_key_chasat.pem "+hostname+" "+"\""+setClasspath+createPlatformAndAgents+"\"");
           
    }
    
    public void killAllAgentSmith(){
        //send a kill message to the AgentSubCoordinator  
        
        //the local
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("SC", AID.ISLOCALNAME));
        msg.setLanguage("English");
        try {
            SmithParameter sp = new SmithParameter();
            sp.type=2; //KILL
            msg.setContentObject(sp);
        } catch (IOException ex) {
            Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        //msg.setContent("launch agents");
        send(msg);
        
        
        //the remotes
        msg.addReceiver(listRemoteSubCoordinators.get(0).getAID());
        send(msg);
        
        
    }
        
    
}
