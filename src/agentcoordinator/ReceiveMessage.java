/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentcoordinator;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author Ethan_Hunt
 */
public class ReceiveMessage extends CyclicBehaviour {
   // Variable to Hold the content of the received Message
    private String Message_Performative;
    private String Message_Content;
    private String SenderName;
    private String MyPlan;
    private AgentCoordinator myAgent;

    public ReceiveMessage(AgentCoordinator myAgent){
        super();
        this.myAgent = myAgent;
    }
    
    public void action() {
        ACLMessage msg = myAgent.receive();
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
