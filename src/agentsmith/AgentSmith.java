/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentsmith;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ethan_Hunt
 */
public class AgentSmith extends Agent {
    
    private long interval;
    
    private String serverAddress;
    private int serverPort;
    private AgentSmith theAgent;
    private AID coordinatorAID;
    
    private Socket tcpClientSocket;
    private PrintWriter out ;
    private BufferedReader in ;
            
    @Override
    protected void setup() {
        theAgent = this;
        
        Object[] args = getArguments();
        if (args != null){
            interval = (long) args[0];
            serverAddress = (String)args[1];
            serverPort = (int)args[2];
            coordinatorAID = (AID)args[3];
            /*
            interval = Long.decode(args[0].toString());
            serverAddress = args[1].toString();
            serverPort = Integer.decode(args[2].toString());
            coordinatorAID = new AID(args[3].toString(), AID.ISGUID);
            coordinatorAID.addAddresses(args[4].toString());
            */
        }
        
        addBehaviour(new TickerBehaviour(this, interval) {
            
            private void informCoordinator(String content){
                //send confirmation to the agent coordinator
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                // msg.addReceiver(new AID(receiver, AID.ISLOCALNAME)); //here the name of the agent is already known
                msg.addReceiver(coordinatorAID);
                msg.setLanguage("English");
                msg.setContent(content);
                
                SendMessage smithSM = new SendMessage(msg);
                theAgent.addBehaviour(smithSM);
                
            }
            
            protected void onTick() {
                try{
                if ((tcpClientSocket == null)||tcpClientSocket.isClosed()){
                    tcpClientSocket = new Socket(serverAddress, serverPort);
                    out = new PrintWriter(tcpClientSocket.getOutputStream(), true);
                    in = new BufferedReader(
                    new InputStreamReader(tcpClientSocket.getInputStream()));
                }
                //out = new PrintWriter(tcpClientSocket.getOutputStream(), true);
                //in = new BufferedReader(new InputStreamReader(tcpClientSocket.getInputStream()));
                out.println("100");
                
                String result="";
                result = in.readLine();
                System.out.println("result "+result);
                //inform the Coordinator
                informCoordinator("fibo result: "+result);
                //in.close();
                //out.close();
                }catch(UnknownHostException e){
                    System.err.println("Don't know about host " + serverAddress);
                    //System.exit(1);
                } catch (IOException ex) {
                    Logger.getLogger(AgentSmith.class.getName()).log(Level.SEVERE, null, ex);
                    //inform the Coordinator
                    informCoordinator("Failed opening TCP socket to server");
                
                    //System.exit(1);
                }catch(Exception ex){
                    Logger.getLogger(AgentSmith.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        } );
        
        addBehaviour(new ReceiveMessage());
    }
    
    @Override
    protected void takeDown(){
        try {
            if (in !=null){
                in.close();
                out.close();
                tcpClientSocket.close();    
            }
        } catch (IOException ex) {
            Logger.getLogger(AgentSmith.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public class SendMessage extends OneShotBehaviour {
    
    private ACLMessage msg;
    
    public SendMessage(ACLMessage msg){
        super();
        this.msg = msg;
    }

    @Override
    public void action() {
        myAgent.send(msg);
        System.out.println("****I Sent Message to::>  *****"+"\n"+
                            "The Content of My Message is::>"+ msg.getContent());

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
        
        }

    } 
}

    
}
