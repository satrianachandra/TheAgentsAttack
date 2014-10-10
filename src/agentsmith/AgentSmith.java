/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentsmith;

import jade.core.AID;
import jade.core.Agent;
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
    
    @Override
    protected void setup() {
        theAgent = this;
        
        Object[] args = getArguments();
        if (args != null){
            interval = (long) args[0];
            serverAddress = (String)args[1];
            serverPort = (int)args[2];
            coordinatorAID = (AID)args[3];
        }
        
        addBehaviour(new TickerBehaviour(this, interval) {
            
            private void informCoordinator(String content){
                //send confirmation to the agent coordinator
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                // msg.addReceiver(new AID(receiver, AID.ISLOCALNAME)); //here the name of the agent is already known
                msg.addReceiver(coordinatorAID);
                msg.setLanguage("English");
                msg.setContent(content);
                
                SendMessage smithSM = new SendMessage(msg, theAgent);
                theAgent.addBehaviour(smithSM);
                
            }
            
            protected void onTick() {
                try{
                Socket tcpClientSocket = new Socket(serverAddress, serverPort);
                PrintWriter out =
                new PrintWriter(tcpClientSocket.getOutputStream(), true);
                BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(tcpClientSocket.getInputStream()));
                out.println("100");
                boolean stop=false;
                String result="";
                result = in.readLine();
                System.out.println("result "+result);
                /*
                while (!stop){
                    result = in.readLine();
                    if (result!=null){
                        stop = true;
                    }
                }*/
                //inform the Coordinator
                informCoordinator("fibo result: "+result);
                in.close();
                out.close();
                tcpClientSocket.close();
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
        
        addBehaviour(new ReceiveMessage(this));
    }
    
    
}
