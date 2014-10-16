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



/**
 *
 * @author Ethan_Hunt
 */
public class AgentCoordinator extends GuiAgent {

    public static final int MESSAGE_RECEIVED = 1;
    public static final int MESSAGE_SENT = 2;
    public static final int MESSAGE_LAUNCH_AGENT = 3;
    public static final String SEMICOLON = ";";
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
            AID remoteSubCoordinator = new AID("SC", AID.ISGUID);
            remoteSubCoordinator.addAddresses("htp://ip-172-30-1-158.eu-west-1.compute.internal:7778/acc");
            msg.addReceiver(remoteSubCoordinator);
            System.out.println(msg.getAllReceiver());
            msg.setLanguage("English");
            
            sp.numberOfAgent=500;
            try {
                msg.setContentObject(sp);
            } catch (IOException ex) {
                Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
            //msg.setContent("launch agents");
            send(msg);
 
            ///
            
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
        
        String hostname = "54.171.91.143";
        String setClasspath = "export CLASSPATH=/home/ubuntu/JADE-COURSE-2014/jade/lib/commons-codec/commons-codec-1.3.jar:$CLASSPATH;"
                + " export CLASSPATH=/home/ubuntu/JADE-COURSE-2014/jade/lib/jadeExamples.jar:$CLASSPATH;"
                + " export CLASSPATH=/home/ubuntu/JADE-COURSE-2014/jade/lib/jade.jar:$CLASSPATH;";
String createPlatform= " cd /home/ubuntu/Codes/TheAgentsAttack/src&&java agentsubcoordinator.AgentSubCoordinator;";
        Terminal.execute("ssh -o StrictHostKeyChecking=no -i /home/ubuntu/aws_key_chasat.pem "+hostname+" "+"\""+setClasspath+createPlatform+"\"");
        
        //Start the platform (and main container) in a remote machine
        /*
        JSch jsch=new JSch();
        Session session = null;
        try {
            jsch.addIdentity("/home/ubuntu/aws_key_chasat.pem");
            jsch.setConfig("StrictHostKeyChecking", "no");

            //enter your own EC2 instance IP here
            session=jsch.getSession("ubuntu", "54.171.91.143", 22);
            session.connect();
        } catch (JSchException ex) {
            Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //run stuff
        String commandPlatform = "java -cp /home/ubuntu/JADE-COURSE-2014/jade/lib/jade.jar jade.Boot -port 1099";
        String commandSCAgent = "cd /home/ubuntu/Codes/TheAgentsAttack/src&&java jade.Boot -container SC:agentsubcoordinator.AgentSubCoordinator";
        Channel channel1 = null;
        Channel channel2 = null;
        try {
            channel1 = session.openChannel("exec");
            ((ChannelExec)channel1).setCommand(commandPlatform);
            ((ChannelExec)channel1).setErrStream(System.err);
            channel1.connect();
            channel2 = session.openChannel("exec");
            ((ChannelExec)channel2).setCommand(commandSCAgent);
            ((ChannelExec)channel2).setErrStream(System.err);
            channel2.connect();
        } catch (JSchException ex) {
            Logger.getLogger(AgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
           
    }   
    
}
