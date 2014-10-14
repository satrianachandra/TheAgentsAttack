/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentcoordinator;

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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import messageclasses.SmithParameter;
import utils.Terminal;

import com.jcraft.jsch.*;

/**
 *
 * @author Ethan_Hunt
 */
public class AgentCoordinator extends GuiAgent {

    public static final int MESSAGE_RECEIVED = 1;
    public static final int MESSAGE_SENT = 2;
    public static final int MESSAGE_LAUNCH_AGENT = 3;
    //an example of adding 1 remote platforms
    public AID remoteDF;
    private AgentCoordinatorUI agentUI;
    
    protected void setup() {
            /** Registration with the DF */
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("CoordinatorAgent");
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
        if (type == MESSAGE_LAUNCH_AGENT){
            SmithParameter sp = (SmithParameter)ge.getParameter(0);
            //send message to the subcoordinator to launch agent
            System.out.println(sp.numberOfAgent);
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("SC_Platform-"+0, AID.ISLOCALNAME));
            msg.setLanguage("English");
            try {
                msg.setContentObject(sp);
            } catch (IOException ex) {
                Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
            //msg.setContent("launch agents");
            send(msg);
            
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
        
        ProfileImpl mProfile = new ProfileImpl(null,1099,null);
        jade.wrapper.AgentContainer mainContainer = rt.createMainContainer(mProfile);
        
        ProfileImpl pContainer = new ProfileImpl();//null, startingPort+i,null);
        jade.wrapper.AgentContainer agentContainer = rt.createAgentContainer(pContainer);
        
        //Start the Agent Coordinator
        AgentController agentCoordinator;
        Object[] coordinatorArgs = new Object[1];
        coordinatorArgs[0]="0";
        try {
            agentCoordinator = agentContainer.createNewAgent("Platform-"+0+"_Coordinator-"+0,
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
            agentSubCoodinator = agentContainer.createNewAgent("SC_Platform-"+0,
                    "agentsubcoordinator.AgentSubCoordinator", subCoordArgs);
            agentSubCoodinator.start();
        } catch (StaleProxyException ex) {
            Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Start the platform (and main container) in a remote machine
        JSch jsch=new JSch();
        Session session = null;
        try {
            jsch.addIdentity("/home/ubuntu/14_LP1_KEY_D7001D_CHASAT-4.pem");
            jsch.setConfig("StrictHostKeyChecking", "no");

            //enter your own EC2 instance IP here
            session=jsch.getSession("ubuntu", "54.171.91.143", 22);
            session.connect();
        } catch (JSchException ex) {
            Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //run stuff
        String command = "java jade.Boot -gui";//;java jade.Boot -container SC:agentsubcoordinator.AgentSubCoordinator";
        Channel channel = null;
        try {
            channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            ((ChannelExec)channel).setErrStream(System.err);
            channel.connect();
        } catch (JSchException ex) {
            Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }   
    
}
